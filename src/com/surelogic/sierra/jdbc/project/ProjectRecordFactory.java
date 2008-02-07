package com.surelogic.sierra.jdbc.project;

import java.sql.Connection;
import java.sql.SQLException;

import com.surelogic.sierra.jdbc.record.ProjectRecord;
import com.surelogic.sierra.jdbc.record.UpdateBaseMapper;

public final class ProjectRecordFactory {

	private static final String PROJECT_SELECT = "SELECT ID FROM PROJECT WHERE NAME = ?";
	private static final String PROJECT_INSERT = "INSERT INTO PROJECT (NAME) VALUES (?)";
	private static final String PROJECT_DELETE = "DELETE FROM PROJECT WHERE ID = ?";
	private static final String PROJECT_UPDATE = "UPDATE PROJECT SET NAME = ? WHERE ID = ?";
	private final UpdateBaseMapper projectMapper;

	private ProjectRecordFactory(Connection conn) throws SQLException {
		projectMapper = new UpdateBaseMapper(conn, PROJECT_INSERT,
				PROJECT_SELECT, PROJECT_DELETE, PROJECT_UPDATE);
	}

	public static ProjectRecordFactory getInstance(Connection conn)
			throws SQLException {
		return new ProjectRecordFactory(conn);
	}

	public ProjectRecord newProject() {
		return new ProjectRecord(projectMapper);
	}

}
