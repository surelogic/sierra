package com.surelogic.sierra.jdbc.run;

import static com.surelogic.sierra.jdbc.JDBCUtils.find;
import static com.surelogic.sierra.jdbc.JDBCUtils.insert;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

import com.surelogic.sierra.jdbc.user.User;

public class RunManager {

	private static final String PROJECT_SELECT = "SELECT ID FROM PROJECT WHERE NAME = ?";
	private static final String PROJECT_INSERT = "INSERT INTO PROJECT (NAME,REVISION) VALUES (?,0)";
	private static final String RUN_INSERT = "INSERT INTO RUN (USER_ID,PROJECT_ID,JAVA_VERSION,JAVA_VENDOR,RUN_DATE_TIME,STATUS) VALUES (?,?,?,?,?,?)";
	private static final String RUN_FINISH = "UPDATE RUN SET STATUS='FINISHED' WHERE ID = ?";

	private final Connection conn;
	private final PreparedStatement selectProject;
	private final PreparedStatement insertProject;
	private final PreparedStatement insertRun;
	private final PreparedStatement finishRun;

	private RunManager(Connection conn) throws SQLException {
		this.conn = conn;
		insertRun = conn.prepareStatement(RUN_INSERT,
				Statement.RETURN_GENERATED_KEYS);
		finishRun = conn.prepareStatement(RUN_FINISH);
		selectProject = conn.prepareStatement(PROJECT_SELECT);
		insertProject = conn.prepareStatement(PROJECT_INSERT,
				Statement.RETURN_GENERATED_KEYS);
	}

	public Long createRun(String projectName) throws SQLException {
		ProjectRecord p = new ProjectRecord();
		p.setName(projectName);
		if (!find(selectProject, p)) {
			insert(insertProject, p);
		}
		RunRecord run = new RunRecord();
		run.setProjectId(p.getId());
		run.setTimestamp(new Date());
		run.setStatus(RunStatus.INPROGRESS);
		run.setUserId(User.getUser(conn).getId());
		insert(insertRun, run);
		return run.getId();
	}

	public void finishRun(Long runId) throws SQLException {
		finishRun.setLong(1, runId);
		finishRun.executeUpdate();
	}

	public static RunManager getInstance(Connection conn) throws SQLException {
		return new RunManager(conn);
	}
}
