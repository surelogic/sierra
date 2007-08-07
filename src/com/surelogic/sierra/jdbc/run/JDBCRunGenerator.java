package com.surelogic.sierra.jdbc.run;

import static com.surelogic.sierra.jdbc.JDBCUtils.find;
import static com.surelogic.sierra.jdbc.JDBCUtils.insert;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

import com.surelogic.sierra.jdbc.user.User;
import com.surelogic.sierra.tool.analyzer.ArtifactGenerator;
import com.surelogic.sierra.tool.analyzer.RunGenerator;

public class JDBCRunGenerator implements RunGenerator {

	private static final String PROJECT_SELECT = "SELECT ID FROM PROJECT WHERE NAME = ?";
	private static final String PROJECT_INSERT = "INSERT INTO PROJECT (NAME,REVISION) VALUES (?,0)";
	private static final String RUN_INSERT = "INSERT INTO RUN (USER_ID,PROJECT_ID,JAVA_VERSION,JAVA_VENDOR,RUN_DATE_TIME,STATUS) VALUES (?,?,?,?,?,?)";

	private final Connection conn;
	private final PreparedStatement selectProject;
	private final PreparedStatement insertProject;
	private final PreparedStatement insertRun;

	private String projectName;
	private String javaVendor;
	private String javaVersion;

	private JDBCRunGenerator(Connection conn) {
		this.conn = conn;
		try {
			insertRun = conn.prepareStatement(RUN_INSERT,
					Statement.RETURN_GENERATED_KEYS);
			selectProject = conn.prepareStatement(PROJECT_SELECT);
			insertProject = conn.prepareStatement(PROJECT_INSERT,
					Statement.RETURN_GENERATED_KEYS);
		} catch (SQLException e) {
			throw new RunPersistenceException(e);
		}
	}

	public ArtifactGenerator build() {
		ProjectRecord p = new ProjectRecord();
		p.setName(projectName);
		try {
			if (!find(selectProject, p)) {
				insert(insertProject, p);
			}

			RunRecord run = new RunRecord();
			run.setProjectId(p.getId());
			run.setTimestamp(new Date());
			run.setJavaVersion(javaVersion);
			run.setJavaVendor(javaVendor);
			run.setStatus(RunStatus.INPROGRESS);
			run.setUserId(User.getUser(conn).getId());
			insert(insertRun, run);
			conn.commit();
			return new JDBCArtifactGenerator(conn, run.getId());

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

}
