package com.surelogic.sierra.jdbc.project;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import com.surelogic.common.SLProgressMonitor;
import com.surelogic.sierra.jdbc.record.ProjectRecord;
import com.surelogic.sierra.jdbc.scan.ScanManager;
import com.surelogic.sierra.jdbc.settings.ClientSettingsManager;
import com.surelogic.sierra.tool.message.Merge;
import com.surelogic.sierra.tool.message.SettingsReply;
import com.surelogic.sierra.tool.message.SettingsRequest;
import com.surelogic.sierra.tool.message.TigerService;
import com.surelogic.sierra.tool.message.TigerServiceClient;

public class ProjectManager {

	@SuppressWarnings("unused")
	private final Connection conn;

	private final ProjectRecordFactory projectFactory;

	private final PreparedStatement deleteMatches;
	private final PreparedStatement deleteFindings;

	private final PreparedStatement findAllProjectNames;
	private final PreparedStatement findProjectRuns;

	private final PreparedStatement findNewFindings;
	private final PreparedStatement obsoleteTrail;
	private final PreparedStatement findNewAudits;

	private ProjectManager(Connection conn) throws SQLException {
		this.conn = conn;
		projectFactory = ProjectRecordFactory.getInstance(conn);
		findAllProjectNames = conn.prepareStatement("SELECT NAME FROM PROJECT");
		findProjectRuns = conn
				.prepareStatement("SELECT S.UID FROM SCAN S WHERE S.PROJECT_ID = ?");
		deleteMatches = conn
				.prepareStatement("DELETE FROM LOCATION_MATCH WHERE PROJECT_ID = ?");
		deleteFindings = conn
				.prepareStatement("DELETE FROM FINDING WHERE PROJECT_ID = ?");
		findNewFindings = null;
		obsoleteTrail = null;
		findNewAudits = null;
	}

	public Collection<String> getAllProjectNames() throws SQLException {
		ResultSet rs = findAllProjectNames.executeQuery();
		Collection<String> projectNames = new ArrayList<String>();
		while (rs.next()) {
			projectNames.add(rs.getString(1));
		}
		return projectNames;
	}

	private Collection<Merge> findNewFindings() {
		Collection<Merge> merges = new ArrayList<Merge>();

		return merges;
	}

	public void synchronizeProject(String server, String name,
			SLProgressMonitor monitor) throws SQLException {
		/*
		 * Synchronization consists of four steps. First, we need to find any
		 * findngs that have been created/merged locally, and merge them on the
		 * database. Second, we commit our audits. Third, we get our updates
		 * from the server and apply them locally. Finally, we also check to see
		 * if we have any updates to settings.
		 */
		TigerService service = new TigerServiceClient(server)
				.getTigerServicePort();

		// Update settings
		ClientSettingsManager settingsManager = ClientSettingsManager
				.getInstance(conn);
		SettingsRequest request = new SettingsRequest();
		request.setProject(name);
		Long revision = settingsManager.getSettingsRevision(name);
		request.setRevision(revision);
		SettingsReply reply = service.getSettings(request);
		Long serverRevision = reply.getRevision();
		if (serverRevision != null) {
			settingsManager.writeSettings(name, serverRevision, reply
					.getSettings());
		}
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
			ScanManager scanMan = ScanManager.getInstance(conn);
			Collection<String> scans = getProjectScans(rec.getId());
			if (monitor != null) {
				monitor.beginTask("Deleting Project '" + projectName + "'",
						scans.size() + 2);
			}
			for (String uid : scans) {
				scanMan.deleteScan(uid);
				if (monitor != null) {
					if (monitor.isCanceled())
						return;
					monitor.worked(1);
				}
			}
			deleteMatches.setLong(1, rec.getId());
			deleteMatches.executeUpdate();
			if (monitor != null) {
				if (monitor.isCanceled())
					return;
				monitor.worked(1);
			}
			deleteFindings.setLong(1, rec.getId());
			deleteFindings.executeUpdate();
			if (monitor != null) {
				if (monitor.isCanceled())
					return;
				monitor.worked(1);
			}
			rec.delete();
		}
		if (monitor != null) {
			monitor.done();
		}
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
