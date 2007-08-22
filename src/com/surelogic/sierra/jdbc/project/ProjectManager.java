package com.surelogic.sierra.jdbc.project;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;


import com.surelogic.sierra.jdbc.project.ProjectRecordFactory;
import com.surelogic.sierra.jdbc.record.ProjectRecord;

public class ProjectManager {

	@SuppressWarnings("unused")
	private final Connection conn;
	
	private static final String FIND_ALL = "SELECT NAME FROM PROJECT";
	private final PreparedStatement findAllprojectNames;

	private final ProjectRecordFactory projectFactory;
	
	private ProjectManager(Connection conn) throws SQLException {
		this.conn = conn;
		
		projectFactory = ProjectRecordFactory.getInstance(conn);
		
		findAllprojectNames = conn.prepareStatement(FIND_ALL);
	}

	public Collection<String> getprojectNames() throws SQLException {
		ResultSet rs = findAllprojectNames.executeQuery();
		Collection<String> projectNames = new ArrayList<String>();
		while(rs.next()) {
			projectNames.add(rs.getString(1));
		}
		return projectNames;
	}
	
	public Long newproject(String name) throws SQLException {
		ProjectRecord project = projectFactory.newProject();
		project.setName(name);
		
		if(project.select()) {
			// XXX Throw error
		}
		
		project.insert();
		
		return project.getId();
	}
	
	public static ProjectManager getInstance(Connection conn)
			throws SQLException {
		return new ProjectManager(conn);
	}
}
