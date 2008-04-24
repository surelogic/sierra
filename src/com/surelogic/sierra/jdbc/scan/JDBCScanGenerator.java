package com.surelogic.sierra.jdbc.scan;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.surelogic.common.jdbc.JDBCUtils;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.jdbc.EmptyProgressMonitor;
import com.surelogic.sierra.jdbc.project.ProjectRecordFactory;
import com.surelogic.sierra.jdbc.record.ProjectRecord;
import com.surelogic.sierra.jdbc.record.RecordRelationRecord;
import com.surelogic.sierra.jdbc.record.ScanRecord;
import com.surelogic.sierra.jdbc.record.TimeseriesRecord;
import com.surelogic.sierra.jdbc.record.TimeseriesScanRecord;
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
	private final Set<String> timeseries;
	private ScanRecord scan;

	JDBCScanGenerator(Connection conn, ScanRecordFactory factory,
			ScanManager manager, FindingFilter filter) {
		this.conn = conn;
		this.factory = factory;
		this.manager = manager;
		timeseries = new TreeSet<String>();
		partial = false;
		this.filter = filter;
	}

	JDBCScanGenerator(Connection conn, ScanRecordFactory factory,
			ScanManager manager, FindingFilter filter, boolean partial) {
		this.conn = conn;
		this.factory = factory;
		this.manager = manager;
		timeseries = new TreeSet<String>();
		this.partial = partial;
		this.filter = filter;
	}

	public ArtifactGenerator build() {
		ProjectRecord p;
		try {
			p = ProjectRecordFactory.getInstance(conn).newProject();
		} catch (final SQLException e) {
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
			for (final String name : timeseries) {
				final TimeseriesRecord q = factory.newTimeseries();
				q.setName(name);
				if (q.select()) {
					final TimeseriesScanRecord rq = factory
							.newScanTimeseriesRelation();
					rq
							.setId(new RecordRelationRecord.PK<TimeseriesRecord, ScanRecord>(
									q, scan));
					rq.insert();
				} else {
					scan.delete();
					throw new IllegalArgumentException(
							"Invalid timeseries name: " + name);
				}
			}
			conn.commit();
			generator = new JDBCArtifactGenerator(conn, factory, manager,
					projectName, scan, filter);
			return generator;
		} catch (final SQLException e) {
			try {
				conn.rollback();
				manager.deleteScan(uid, null);
				conn.commit();
			} catch (final SQLException e1) {
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
		javaVendor = vendor;
		return this;
	}

	public ScanGenerator javaVersion(String version) {
		javaVersion = version;
		return this;
	}

	public ScanGenerator project(String projectName) {
		this.projectName = projectName;
		return this;
	}

	public ScanGenerator timeseries(Collection<String> timeseries) {
		if ((timeseries != null) && !timeseries.isEmpty()) {
			this.timeseries.addAll(timeseries);
		}
		return this;
	}

	public ScanGenerator user(String user) {
		this.user = user;
		return this;
	}

	public String finished() {
		generator.finished(EmptyProgressMonitor.instance());
		scan.setStatus(ScanStatus.GENERATED);
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
		} catch (final SQLException e) {
			throw new ScanPersistenceException(e);
		}
	}

}
