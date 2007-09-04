package com.surelogic.sierra.jdbc.scan;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.jdbc.finding.FindingGenerationException;
import com.surelogic.sierra.jdbc.finding.FindingManager;
import com.surelogic.sierra.jdbc.record.ProjectRecord;
import com.surelogic.sierra.jdbc.record.QualifierRecord;
import com.surelogic.sierra.jdbc.record.QualifierScanRecord;
import com.surelogic.sierra.jdbc.record.RecordRelationRecord;
import com.surelogic.sierra.jdbc.record.ScanRecord;
import com.surelogic.sierra.jdbc.tool.FindingTypeManager;
import com.surelogic.sierra.jdbc.tool.MessageFilter;
import com.surelogic.sierra.jdbc.user.User;
import com.surelogic.sierra.tool.analyzer.ArtifactGenerator;
import com.surelogic.sierra.tool.analyzer.ScanGenerator;
import com.surelogic.sierra.tool.message.Settings;

class JDBCScanGenerator implements ScanGenerator {

	private static final Logger log = SLLogger
			.getLoggerFor(JDBCScanGenerator.class);

	private final Connection conn;
	private final ScanRecordFactory factory;
	private final Settings settings;
	private String projectName;
	private String javaVendor;
	private String javaVersion;
	private String uid;
	private List<String> qualifiers;

	JDBCScanGenerator(Connection conn, ScanRecordFactory factory,
			Settings settings) {
		this.conn = conn;
		this.factory = factory;
		this.qualifiers = Collections.emptyList();
		this.settings = settings;
	}

	public ArtifactGenerator build() {
		ProjectRecord p = factory.newProject();
		p.setName(projectName);
		try {
			if (!p.select()) {
				p.insert();
			}
			final ScanRecord scan = factory.newScan();
			scan.setProjectId(p.getId());
			scan.setUid(uid);
			scan.setTimestamp(new Date());
			scan.setJavaVersion(javaVersion);
			scan.setJavaVendor(javaVendor);
			scan.setStatus(ScanStatus.LOADING);
			scan.setUserId(User.getUser(conn).getId());
			scan.insert();
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
					throw new IllegalArgumentException(
							"Invalid qualifier name: " + name);
				}
			}
			conn.commit();
			final MessageFilter filter = FindingTypeManager.getInstance(conn)
					.getMessageFilter(settings);
			return new JDBCArtifactGenerator(conn, factory, scan, filter,
			// This scannable is called after finish is called in
					// ArtifactGenerator
					new Runnable() {
						public void run() {
							try {
								scan.setStatus(ScanStatus.FINISHED);
								scan.update();
							} catch (SQLException e) {
								throw new ScanPersistenceException(e);
							}
							log
									.info("Scan "
											+ scan.getId()
											+ " persisted to database, starting finding generation.");
							try {
								FindingManager.getInstance(conn)
										.generateFindings(scan.getUid(), filter);
							} catch (SQLException e) {
								throw new FindingGenerationException(e);
							}
						}
					});
		} catch (SQLException e) {
			try {
				ScanManager.getInstance(conn).deleteScan(uid);
			} catch (SQLException e1) {
				//Quietly do nothing, we already have an exception
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
		if (qualifiers != null) {
			this.qualifiers = new ArrayList<String>(qualifiers);
		}
		return this;
	}

}
