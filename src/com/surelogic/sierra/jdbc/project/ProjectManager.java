package com.surelogic.sierra.jdbc.project;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.surelogic.common.SLProgressMonitor;
import com.surelogic.sierra.jdbc.finding.ClientFindingManager;
import com.surelogic.sierra.jdbc.finding.FindingManager;
import com.surelogic.sierra.jdbc.record.ProjectRecord;
import com.surelogic.sierra.jdbc.scan.ScanManager;
import com.surelogic.sierra.jdbc.settings.ClientSettingsManager;
import com.surelogic.sierra.tool.message.AuditTrailResponse;
import com.surelogic.sierra.tool.message.CommitAuditTrailRequest;
import com.surelogic.sierra.tool.message.GetAuditTrailRequest;
import com.surelogic.sierra.tool.message.Merge;
import com.surelogic.sierra.tool.message.MergeAuditTrailRequest;
import com.surelogic.sierra.tool.message.MergeAuditTrailResponse;
import com.surelogic.sierra.tool.message.ServerMismatchException;
import com.surelogic.sierra.tool.message.ServerUIDRequest;
import com.surelogic.sierra.tool.message.SettingsReply;
import com.surelogic.sierra.tool.message.SettingsRequest;
import com.surelogic.sierra.tool.message.SierraServerLocation;
import com.surelogic.sierra.tool.message.SierraService;
import com.surelogic.sierra.tool.message.SierraServiceClient;

public class ProjectManager {

	private final Connection conn;

	private final ScanManager scanManager;
	private final FindingManager findingManager;
	private final ProjectRecordFactory projectFactory;

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

	public void synchronizeProject(SierraServerLocation server,
			String projectName, SLProgressMonitor monitor)
			throws ServerMismatchException, SQLException {
		/*
		 * Synchronization consists of four steps. First, we need to find any
		 * findings that have been created/merged locally, and merge them on the
		 * database. Second, we commit our audits. Third, we get our updates
		 * from the server and apply them locally. Finally, we also check to see
		 * if we have any updates to settings.
		 */
		SierraService service = new SierraServiceClient(server)
				.getSierraServicePort();

		// Look up project. If it doesn't exist, create it and relate it to the
		// server.
		ProjectRecord rec = projectFactory.newProject();
		rec.setName(projectName);
		if (!rec.select()) {
			rec.insert();
		}
		String serverUid = rec.getServerUid();
		if (serverUid == null) {
			serverUid = service.getUid(new ServerUIDRequest()).getUid();
			rec.setServerUid(serverUid);
			rec.update();
		}
		if (monitor.isCanceled()) {
			return;
		}
		monitor.worked(1);
		// Commit merges
		monitor.subTask("Committing local merges from project " + projectName
				+ ".");
		MergeAuditTrailRequest mergeRequest = new MergeAuditTrailRequest();
		mergeRequest.setServer(serverUid);
		mergeRequest.setProject(projectName);
		List<Merge> merges = findingManager.getNewLocalMerges(projectName,
				monitor);
		if (!merges.isEmpty()) {
			mergeRequest.setMerge(merges);
			MergeAuditTrailResponse mergeResponse = service
					.mergeAuditTrails(mergeRequest);
			findingManager.updateLocalTrailUids(projectName, mergeResponse
					.getRevision(), mergeResponse.getTrail(), merges, monitor);
		}
		if (monitor.isCanceled()) {
			return;
		}
		monitor.worked(1);
		// Commit Audits
		monitor.subTask("Committing local audits for project " + projectName
				+ ".");
		CommitAuditTrailRequest commitRequest = new CommitAuditTrailRequest();
		commitRequest.setServer(serverUid);
		commitRequest.setAuditTrail(findingManager.getNewLocalAudits(
				projectName, monitor));
		service.commitAuditTrails(commitRequest);

		if (monitor.isCanceled()) {
			return;
		}
		monitor.worked(1);
		// Get updated audits from server
		monitor.subTask("Updating findings for project " + projectName
				+ " from server.");
		findingManager.deleteLocalAudits(projectName, monitor);
		GetAuditTrailRequest auditRequest = new GetAuditTrailRequest();
		auditRequest.setProject(projectName);
		auditRequest.setRevision(findingManager
				.getLatestAuditRevision(projectName));
		auditRequest.setServer(serverUid);
		AuditTrailResponse auditResponse = service.getAuditTrails(auditRequest);
		findingManager.updateLocalFindings(projectName, auditResponse
				.getObsolete(), auditResponse.getUpdate(), monitor);
		if (monitor.isCanceled()) {
			return;
		}
		monitor.worked(1);
		// Update settings
		monitor.subTask("Checking for updated settings for project "
				+ projectName);
		ClientSettingsManager settingsManager = ClientSettingsManager
				.getInstance(conn);
		SettingsRequest request = new SettingsRequest();
		request.setServer(serverUid);
		request.setProject(projectName);
		Long revision = settingsManager.getSettingsRevision(projectName);
		request.setRevision(revision);
		SettingsReply reply = service.getSettings(request);
		Long serverRevision = reply.getRevision();
		if (serverRevision != null) {
			settingsManager.writeSettings(projectName, serverRevision, reply
					.getSettings());
		}
		monitor.worked(1);
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

	private Collection<String> getProjectScans(Long projectId)
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

	public static ProjectManager getInstance(Connection conn)
			throws SQLException {
		return new ProjectManager(conn);
	}
}
