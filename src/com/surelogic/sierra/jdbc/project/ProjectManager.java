package com.surelogic.sierra.jdbc.project;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

public class ProjectManager {

	protected final Connection conn;

	private final PreparedStatement findAllProjectNames;

	protected ProjectManager(Connection conn) throws SQLException {
		this.conn = conn;
		findAllProjectNames = conn.prepareStatement("SELECT NAME FROM PROJECT");
	}

	public static ProjectManager getInstance(Connection conn)
			throws SQLException {
		return new ProjectManager(conn);
	}

	public Collection<String> getAllProjectNames() throws SQLException {
		ResultSet rs = findAllProjectNames.executeQuery();
		Collection<String> projectNames = new ArrayList<String>();
		try {
			while (rs.next()) {
				projectNames.add(rs.getString(1));
			}
		} finally {
			rs.close();
		}
		return projectNames;
	}
}
