package com.surelogic.sierra.jdbc.run;

import static com.surelogic.sierra.jdbc.JDBCUtils.find;
import static com.surelogic.sierra.jdbc.JDBCUtils.insert;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.surelogic.sierra.jdbc.user.User;
import com.surelogic.sierra.tool.analyzer.ArtifactGenerator;
import com.surelogic.sierra.tool.analyzer.RunGenerator;

public class JDBCRunGenerator implements RunGenerator {

	private static final String PROJECT_SELECT = "SELECT ID FROM PROJECT WHERE NAME = ?";
	private static final String PROJECT_INSERT = "INSERT INTO PROJECT (NAME,REVISION) VALUES (?,0)";
	private static final String RUN_INSERT = "INSERT INTO RUN (USER_ID,PROJECT_ID,JAVA_VERSION,JAVA_VENDOR,RUN_DATE_TIME,STATUS) VALUES (?,?,?,?,?,?)";
	private static final String QUALIFIER_SELECT = "SELECT ID FROM QUALIFIER WHERE NAME = ?";
	private static final String RUN_QUALIFIER_INSERT = "INSERT INTO QUALIFIER_RUN_RELTN (QUALIFIER_ID,RUN_ID) VALUES(?,?)";
	private final Connection conn;
	private final PreparedStatement selectProject;
	private final PreparedStatement insertProject;
	private final PreparedStatement insertRun;
	private final PreparedStatement selectQualifier;
	private final PreparedStatement insertRunQualifierReltn;

	private String projectName;
	private String javaVendor;
	private String javaVersion;
	private List<String> qualifiers;

	private JDBCRunGenerator(Connection conn) {
		this.conn = conn;
		this.qualifiers = Collections.emptyList();
		try {
			insertRun = conn.prepareStatement(RUN_INSERT,
					Statement.RETURN_GENERATED_KEYS);
			selectProject = conn.prepareStatement(PROJECT_SELECT);
			insertProject = conn.prepareStatement(PROJECT_INSERT,
					Statement.RETURN_GENERATED_KEYS);
			selectQualifier = conn.prepareStatement(QUALIFIER_SELECT);
			insertRunQualifierReltn = conn
					.prepareStatement(RUN_QUALIFIER_INSERT);
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
			for (String s : qualifiers) {
				selectQualifier.setString(1, s);
				ResultSet set = selectQualifier.executeQuery();
				if (set.next()) {
					insertRunQualifierReltn.setLong(1, set.getLong(1));
					insertRunQualifierReltn.setLong(2, run.getId());
					insertRunQualifierReltn.executeUpdate();
				} else {
					throw new IllegalArgumentException(
							"Invalid qualifier name: " + s);
				}
			}
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

	public RunGenerator qualifiers(Collection<String> qualifiers) {
		if (qualifiers != null) {
			this.qualifiers = new ArrayList<String>(qualifiers);
		}
		return this;
	}

}
