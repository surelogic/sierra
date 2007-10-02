package com.surelogic.sierra.jdbc.project;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import com.surelogic.common.SLProgressMonitor;
import com.surelogic.sierra.jdbc.finding.FindingManager;
import com.surelogic.sierra.jdbc.record.ProjectRecord;
import com.surelogic.sierra.jdbc.scan.ScanManager;

public class ProjectManager {

	protected final Connection conn;
	protected final ProjectRecordFactory projectFactory;
	protected ScanManager scanManager;

	private final FindingManager findingManager;
	private final PreparedStatement findAllProjectNames;
	private final PreparedStatement findProjectRuns;

	protected ProjectManager(Connection conn) throws SQLException {
		this.conn = conn;
		projectFactory = ProjectRecordFactory.getInstance(conn);
		findAllProjectNames = conn.prepareStatement("SELECT NAME FROM PROJECT");
		findingManager = FindingManager.getInstance(conn);
		scanManager = ScanManager.getInstance(conn);
		findProjectRuns = conn
				.prepareStatement("SELECT S.UID FROM SCAN S WHERE S.PROJECT_ID = ?");
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

	public Long newProject(String name) throws SQLException {
		ProjectRecord project = projectFactory.newProject();
		project.setName(name);
		if (project.select()) {
			throw new IllegalArgumentException("Project with name " + name
					+ " already exists");
		}
		project.insert();
		return project.getId();
	}

	public void deleteProject(String projectName, SLProgressMonitor monitor)
			throws SQLException {
		ProjectRecord rec = projectFactory.newProject();
		rec.setName(projectName);
		if (rec.select()) {
			if (monitor != null)
				monitor.subTask("Deleting scans for project " + projectName);
			scanManager.deleteScans(getProjectScans(rec.getId()), monitor);
			findingManager.deleteFindings(projectName, monitor);
			rec.delete();
		}
	}

	protected Collection<String> getProjectScans(Long projectId)
			throws SQLException {
		Collection<String> runs = new ArrayList<String>();
		findProjectRuns.setLong(1, projectId);
		ResultSet set = findProjectRuns.executeQuery();
		try {
			while (set.next()) {
				runs.add(set.getString(1));
			}
		} finally {
			set.close();
		}
		return runs;
	}
}
