package com.surelogic.sierra.jdbc.project;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.surelogic.common.SLProgressMonitor;
import com.surelogic.sierra.jdbc.finding.FindingManager;
import com.surelogic.sierra.jdbc.record.ProjectRecord;
import com.surelogic.sierra.jdbc.scan.ScanManager;
import com.surelogic.sierra.jdbc.settings.ClientSettingsManager;
import com.surelogic.sierra.tool.message.AuditTrailRequest;
import com.surelogic.sierra.tool.message.AuditTrailResponse;
import com.surelogic.sierra.tool.message.Merge;
import com.surelogic.sierra.tool.message.MergeAuditResponse;
import com.surelogic.sierra.tool.message.MergeAuditTrailRequest;
import com.surelogic.sierra.tool.message.SettingsReply;
import com.surelogic.sierra.tool.message.SettingsRequest;
import com.surelogic.sierra.tool.message.SierraServer;
import com.surelogic.sierra.tool.message.SierraService;
import com.surelogic.sierra.tool.message.SierraServiceClient;

public class ProjectManager {

	@SuppressWarnings("unused")
	private final Connection conn;

	private final ScanManager scanManager;
	private final FindingManager findingManager;
	private final ProjectRecordFactory projectFactory;

	private final PreparedStatement deleteMatches;
	private final PreparedStatement deleteFindings;

	private final PreparedStatement findAllProjectNames;
	private final PreparedStatement findProjectRuns;

	private ProjectManager(Connection conn) throws SQLException {
		this.conn = conn;
		this.scanManager = ScanManager.getInstance(conn);
		this.findingManager = FindingManager.getInstance(conn);
		projectFactory = ProjectRecordFactory.getInstance(conn);
		findAllProjectNames = conn.prepareStatement("SELECT NAME FROM PROJECT");
		findProjectRuns = conn
				.prepareStatement("SELECT S.UID FROM SCAN S WHERE S.PROJECT_ID = ?");
		deleteMatches = conn
				.prepareStatement("DELETE FROM LOCATION_MATCH WHERE PROJECT_ID = ?");
		deleteFindings = conn
				.prepareStatement("DELETE FROM FINDING WHERE PROJECT_ID = ?");

	}

	public Collection<String> getAllProjectNames() throws SQLException {
		ResultSet rs = findAllProjectNames.executeQuery();
		Collection<String> projectNames = new ArrayList<String>();
		while (rs.next()) {
			projectNames.add(rs.getString(1));
		}
		return projectNames;
	}

	public void synchronizeProject(SierraServer server, String projectName,
			SLProgressMonitor monitor) throws SQLException {
		/*
		 * Synchronization consists of four steps. First, we need to find any
		 * findngs that have been created/merged locally, and merge them on the
		 * database. Second, we commit our audits. Third, we get our updates
		 * from the server and apply them locally. Finally, we also check to see
		 * if we have any updates to settings.
		 */
		SierraService service = new SierraServiceClient(server)
				.getSierraServicePort();

		// Commit merges
		MergeAuditTrailRequest mergeRequest = new MergeAuditTrailRequest();
		mergeRequest.setProject(projectName);
		List<Merge> merges = findingManager.getNewLocalMerges(projectName,
				monitor);
		mergeRequest.setMerge(merges);
		MergeAuditResponse mergeResponse = service
				.mergeAuditTrails(mergeRequest);
		findingManager.updateLocalTrailUids(projectName, mergeResponse
				.getRevision(), mergeResponse.getTrail(), merges, monitor);

		// Commit Audits
		service.commitAuditTrails(findingManager.getNewLocalAudits(projectName,
				monitor));

		// Get updated audits from server
		AuditTrailRequest auditRequest = new AuditTrailRequest();
		auditRequest.setProject(projectName);
		auditRequest.setRevision(findingManager
				.getLatestAuditRevision(projectName));
		AuditTrailResponse auditResponse = service.getAuditTrails(auditRequest);
		findingManager.updateLocalFindings(projectName, auditResponse
				.getRevision(), auditResponse.getObsolete(), auditResponse
				.getUpdate(), monitor);

		// Update settings
		ClientSettingsManager settingsManager = ClientSettingsManager
				.getInstance(conn);
		SettingsRequest request = new SettingsRequest();
		request.setProject(projectName);
		Long revision = settingsManager.getSettingsRevision(projectName);
		request.setRevision(revision);
		SettingsReply reply = service.getSettings(request);
		Long serverRevision = reply.getRevision();
		if (serverRevision != null) {
			settingsManager.writeSettings(projectName, serverRevision, reply
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
			monitor.subTask("Deleting scans for project " + projectName);
			scanManager.deleteScans(getProjectScans(rec.getId()), monitor);
			monitor.subTask("Deleting matches for project " + projectName);
			deleteMatches.setLong(1, rec.getId());
			deleteMatches.executeUpdate();
			if (monitor != null) {
				if (monitor.isCanceled())
					return;
				monitor.worked(1);
			}
			monitor.subTask("Deleting findings for project " + projectName);
			deleteFindings.setLong(1, rec.getId());
			deleteFindings.executeUpdate();
			if (monitor != null) {
				if (monitor.isCanceled())
					return;
				monitor.worked(1);
			}
			rec.delete();
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
