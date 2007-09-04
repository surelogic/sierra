package com.surelogic.sierra.jdbc.project;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import com.surelogic.common.SLProgressMonitor;
import com.surelogic.sierra.jdbc.project.ProjectRecordFactory;
import com.surelogic.sierra.jdbc.record.ProjectRecord;
import com.surelogic.sierra.jdbc.scan.ScanManager;

public class ProjectManager {

	@SuppressWarnings("unused")
	private final Connection conn;

	private static final String FIND_ALL = "SELECT NAME FROM PROJECT";

	private final PreparedStatement findAllprojectNames;
	private final PreparedStatement findProjectRuns;
	private final ProjectRecordFactory projectFactory;

	private ProjectManager(Connection conn) throws SQLException {
		this.conn = conn;

		projectFactory = ProjectRecordFactory.getInstance(conn);

		findAllprojectNames = conn.prepareStatement(FIND_ALL);
		findProjectRuns = conn
				.prepareStatement("SELECT R.UID FROM RUN R WHERE R.PROJECT_ID = ?");
	}

	public Collection<String> getAllProjectNames() throws SQLException {
		ResultSet rs = findAllprojectNames.executeQuery();
		Collection<String> projectNames = new ArrayList<String>();
		while (rs.next()) {
			projectNames.add(rs.getString(1));
		}
		return projectNames;
	}

	public Long newProject(String name) throws SQLException {
		ProjectRecord project = projectFactory.newProject();
		project.setName(name);

		if (project.select()) {
			// XXX Throw error
		}

		project.insert();

		return project.getId();
	}

	public void deleteProject(String name, SLProgressMonitor monitor)
			throws SQLException {
		ProjectRecord rec = projectFactory.newProject();
		rec.setName(name);
		if (!rec.select()) {
			throw new IllegalArgumentException("No project with name " + name
					+ " exists.");
		}
		ScanManager scanMan = ScanManager.getInstance(conn);
		Collection<String> scans = getProjectScans(rec.getId());
		if (monitor != null) {
			monitor.beginTask("Deleting Project", scans.size() + 1);
		}
		for (String uid : scans) {
			scanMan.deleteScan(uid);
			if (monitor != null) {
				if (monitor.isCanceled())
					return;
				monitor.worked(1);
			}
		}
		rec.delete();
		monitor.done();
	}

	private Collection<String> getProjectScans(Long projectId)
			throws SQLException {
		Collection<String> runs = new ArrayList<String>();
		findProjectRuns.setLong(1, projectId);
		ResultSet set = findProjectRuns.executeQuery();
		while (set.next()) {
			runs.add(set.getString(1));
		}
		return runs;
	}

	public static ProjectManager getInstance(Connection conn)
			throws SQLException {
		return new ProjectManager(conn);
	}
}
