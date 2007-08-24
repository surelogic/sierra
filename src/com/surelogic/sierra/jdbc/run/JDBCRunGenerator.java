package com.surelogic.sierra.jdbc.run;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import com.surelogic.sierra.jdbc.finding.FindingGenerationException;
import com.surelogic.sierra.jdbc.finding.FindingManager;
import com.surelogic.sierra.jdbc.record.ProjectRecord;
import com.surelogic.sierra.jdbc.record.QualifierRecord;
import com.surelogic.sierra.jdbc.record.RecordRelationRecord;
import com.surelogic.sierra.jdbc.record.RunQualifierRecord;
import com.surelogic.sierra.jdbc.record.RunRecord;
import com.surelogic.sierra.jdbc.user.User;
import com.surelogic.sierra.tool.SierraLogger;
import com.surelogic.sierra.tool.analyzer.ArtifactGenerator;
import com.surelogic.sierra.tool.analyzer.RunGenerator;

class JDBCRunGenerator implements RunGenerator {

	private static final Logger log = SierraLogger
			.getLogger(JDBCRunGenerator.class.getName());

	private final Connection conn;
	private final RunRecordFactory factory;
	private String projectName;
	private String javaVendor;
	private String javaVersion;
	private String uid;
	private List<String> qualifiers;

	JDBCRunGenerator(Connection conn, RunRecordFactory factory) {
		this.conn = conn;
		this.factory = factory;
		this.qualifiers = Collections.emptyList();
	}

	public ArtifactGenerator build() {
		ProjectRecord p = factory.newProject();
		p.setName(projectName);
		try {
			if (!p.select()) {
				p.insert();
			}
			final RunRecord run = factory.newRun();
			run.setProjectId(p.getId());
			run.setUid(uid);
			run.setTimestamp(new Date());
			run.setJavaVersion(javaVersion);
			run.setJavaVendor(javaVendor);
			run.setStatus(RunStatus.INPROGRESS);
			run.setUserId(User.getUser(conn).getId());
			run.insert();
			for (String name : qualifiers) {
				QualifierRecord q = factory.newQualifier();
				q.setName(name);
				if (q.select()) {
					RunQualifierRecord rq = factory.newRunQualiferRelation();
					rq
							.setId(new RecordRelationRecord.PK<RunRecord, QualifierRecord>(
									run, q));
					rq.insert();
				} else {
					throw new IllegalArgumentException(
							"Invalid qualifier name: " + name);
				}
			}
			conn.commit();
			return new JDBCArtifactGenerator(conn, factory, run,
					new Runnable() {
						public void run() {
							try {
								run.setStatus(RunStatus.FINISHED);
								run.update();
							} catch (SQLException e) {
								throw new RunPersistenceException(e);
							}
							// TODO trigger finding generation here
							log
									.info("Run "
											+ run.getId()
											+ " persisted to database, starting finding generation.");
							try {
								if (qualifiers.isEmpty()) {
									// This is a client run
									FindingManager.getInstance(conn)
											.generateFindings(run.getUid());
								} else {
									for (String qualifier : qualifiers) {
										FindingManager.getInstance(conn,
												qualifier).generateFindings(
												run.getUid());
									}
								}
							} catch (SQLException e) {
								throw new FindingGenerationException(e);
							}
						}
					});
		} catch (SQLException e) {
			throw new RunPersistenceException(e);
		}
	}

	public RunGenerator uid(String uid) {
		this.uid = uid;
		return this;
	}

	public RunGenerator javaVendor(String vendor) {
		this.javaVendor = vendor;
		return this;
	}

	public RunGenerator javaVersion(String version) {
		this.javaVersion = version;
		return this;
	}

	public RunGenerator project(String projectName) {
		this.projectName = projectName;
		return this;
	}

	public RunGenerator qualifiers(Collection<String> qualifiers) {
		if (qualifiers != null) {
			this.qualifiers = new ArrayList<String>(qualifiers);
		}
		return this;
	}

}
