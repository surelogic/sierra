package com.surelogic.sierra.jdbc.project;

import java.sql.Connection;
import java.sql.SQLException;

import com.surelogic.sierra.jdbc.record.BaseMapper;
import com.surelogic.sierra.jdbc.record.ProjectRecord;

public class ProjectRecordFactory {

	private static final String PROJECT_SELECT = "SELECT ID FROM PROJECT WHERE NAME = ?";
	private static final String PROJECT_INSERT = "INSERT INTO PROJECT (NAME,SETTINGS_REVISION) VALUES (?,0)";
	private static final String PROJECT_DELETE = "DELETE FROM PROJECT WHERE ID = ?";

	private final BaseMapper projectMapper;

	private ProjectRecordFactory(Connection conn) throws SQLException {
		projectMapper = new BaseMapper(conn, PROJECT_INSERT, PROJECT_SELECT,
				PROJECT_DELETE);
	}

	public static ProjectRecordFactory getInstance(Connection conn)
			throws SQLException {
		return new ProjectRecordFactory(conn);
	}

	public ProjectRecord newProject() {
		return new ProjectRecord(projectMapper);
	}

}
