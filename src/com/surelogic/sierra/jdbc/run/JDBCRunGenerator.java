package com.surelogic.sierra.jdbc.run;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import com.surelogic.sierra.jdbc.finding.FindingGenerator;
import com.surelogic.sierra.jdbc.record.ProjectRecord;
import com.surelogic.sierra.jdbc.record.QualifierRecord;
import com.surelogic.sierra.jdbc.record.RelationRecord;
import com.surelogic.sierra.jdbc.record.RunQualifierRecord;
import com.surelogic.sierra.jdbc.record.RunRecord;
import com.surelogic.sierra.jdbc.user.User;
import com.surelogic.sierra.tool.SierraLogger;
import com.surelogic.sierra.tool.analyzer.ArtifactGenerator;
import com.surelogic.sierra.tool.analyzer.RunGenerator;

public class JDBCRunGenerator implements RunGenerator {

	private static final Logger log = SierraLogger
			.getLogger(JDBCRunGenerator.class.getName());

	private static final String RUN_FINISH = "UPDATE RUN SET STATUS='FINISHED' WHERE ID = ?";

	private final Connection conn;
	private final RunRecordFactory factory;

	private String projectName;
	private String javaVendor;
	private String javaVersion;
	private List<String> qualifiers;

	private final PreparedStatement finishRun;

	private JDBCRunGenerator(Connection conn) {
		this.conn = conn;
		try {
			this.factory = RunRecordFactory.getInstance(conn);
			finishRun = conn.prepareStatement(RUN_FINISH);
		} catch (SQLException e) {
			throw new RunPersistenceException(e);
		}
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
			run.setUid(UUID.randomUUID().toString());
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
					rq.setId(new RelationRecord.PK<RunRecord, QualifierRecord>(
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
								finishRun.setLong(1, run.getId());
								finishRun.executeUpdate();
								conn.commit();
								finishRun.close();
							} catch (SQLException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							// TODO trigger finding generation here
							log
									.info("Run "
											+ run.getId()
											+ " persisted to database, starting finding generation.");
							new FindingGenerator(conn).generate(run);
						}
					});

		} catch (SQLException e) {
			throw new RunPersistenceException(e);
		}
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

	public static RunGenerator getInstance(Connection conn) {
		return new JDBCRunGenerator(conn);
	}

	public RunGenerator qualifiers(Collection<String> qualifiers) {
		if (qualifiers != null) {
			this.qualifiers = new ArrayList<String>(qualifiers);
		}
		return this;
	}

}
