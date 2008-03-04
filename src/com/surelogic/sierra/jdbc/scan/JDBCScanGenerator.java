package com.surelogic.sierra.jdbc.scan;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.jdbc.EmptyProgressMonitor;
import com.surelogic.sierra.jdbc.JDBCUtils;
import com.surelogic.sierra.jdbc.project.ProjectRecordFactory;
import com.surelogic.sierra.jdbc.record.ProjectRecord;
import com.surelogic.sierra.jdbc.record.QualifierRecord;
import com.surelogic.sierra.jdbc.record.QualifierScanRecord;
import com.surelogic.sierra.jdbc.record.RecordRelationRecord;
import com.surelogic.sierra.jdbc.record.ScanRecord;
import com.surelogic.sierra.jdbc.timeseries.QualifierManager;
import com.surelogic.sierra.jdbc.tool.FindingFilter;
import com.surelogic.sierra.jdbc.user.ClientUser;
import com.surelogic.sierra.tool.message.ArtifactGenerator;
import com.surelogic.sierra.tool.message.ScanGenerator;

class JDBCScanGenerator implements ScanGenerator {

	private static final Logger log = SLLogger
			.getLoggerFor(JDBCScanGenerator.class);

	private final Connection conn;
	private final FindingFilter filter;
	private final ScanRecordFactory factory;
	private final ScanManager manager;
	private final boolean partial;
	private JDBCArtifactGenerator generator;
	private String projectName;
	private String javaVendor;
	private String javaVersion;
	private String uid;
	private String user;
	private Set<String> qualifiers;
	private ScanRecord scan;

	JDBCScanGenerator(Connection conn, ScanRecordFactory factory,
			ScanManager manager, FindingFilter filter) {
		this.conn = conn;
		this.factory = factory;
		this.manager = manager;
		this.qualifiers = new TreeSet<String>();
		this.partial = false;
		this.filter = filter;
	}

	JDBCScanGenerator(Connection conn, ScanRecordFactory factory,
			ScanManager manager, FindingFilter filter, boolean partial) {
		this.conn = conn;
		this.factory = factory;
		this.manager = manager;
		this.qualifiers = new TreeSet<String>();
		this.partial = partial;
		this.filter = filter;
	}

	public ArtifactGenerator build() {
		ProjectRecord p;
		try {
			p = ProjectRecordFactory.getInstance(conn).newProject();
		} catch (SQLException e) {
			throw new ScanPersistenceException(e);
		}
		p.setName(projectName);
		try {
			if (!p.select()) {
				p.insert();
			}
			scan = factory.newScan();
			scan.setProjectId(p.getId());
			scan.setUid(uid);
			scan.setTimestamp(JDBCUtils.now());
			scan.setJavaVersion(javaVersion);
			scan.setJavaVendor(javaVendor);
			scan.setStatus(ScanStatus.LOADING);
			scan.setPartial(partial);
			if (user != null) {
				scan.setUserId(ClientUser.getUser(user, conn).getId());
			}
			if (scan.select()) {
				manager.deleteScan(uid, null);
				conn.commit();
			}
			scan.insert();
			qualifiers.add(QualifierManager.ALL_SCANS);
			for (String name : qualifiers) {
				QualifierRecord q = factory.newQualifier();
				q.setName(name);
				if (q.select()) {
					QualifierScanRecord rq = factory.newScanQualifierRelation();
					rq
							.setId(new RecordRelationRecord.PK<QualifierRecord, ScanRecord>(
									q, scan));
					rq.insert();
				} else {
					scan.delete();
					throw new IllegalArgumentException(
							"Invalid qualifier name: " + name);
				}
			}
			conn.commit();
			generator = new JDBCArtifactGenerator(conn, factory, manager,
					projectName, scan, filter);
			return generator;
		} catch (SQLException e) {
			try {
				conn.rollback();
				manager.deleteScan(uid, null);
				conn.commit();
			} catch (SQLException e1) {
				// Quietly do nothing, we already have an exception
			}
			throw new ScanPersistenceException(e);
		}
	}

	public ScanGenerator uid(String uid) {
		this.uid = uid;
		return this;
	}

	public ScanGenerator javaVendor(String vendor) {
		this.javaVendor = vendor;
		return this;
	}

	public ScanGenerator javaVersion(String version) {
		this.javaVersion = version;
		return this;
	}

	public ScanGenerator project(String projectName) {
		this.projectName = projectName;
		return this;
	}

	public ScanGenerator qualifiers(Collection<String> qualifiers) {
		if (qualifiers != null && !qualifiers.isEmpty()) {
			qualifiers.addAll(qualifiers);
		}
		return this;
	}

	public ScanGenerator user(String user) {
		this.user = user;
		return this;
	}

	public String finished() {
		generator.finished(EmptyProgressMonitor.instance());
		scan.setStatus(ScanStatus.FINISHED);
		try {
			scan.update();
			if (log.isLoggable(Level.FINE)) {
				log
						.fine("Scan "
								+ scan.getUid()
								+ " for project "
								+ projectName
								+ " persisted to database, starting finding generation.");
			}
			conn.commit();
			return scan.getUid();
		} catch (SQLException e) {
			throw new ScanPersistenceException(e);
		}
	}

}
