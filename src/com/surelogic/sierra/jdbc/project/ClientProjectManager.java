package com.surelogic.sierra.jdbc.project;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import com.surelogic.common.SLProgressMonitor;
import com.surelogic.sierra.jdbc.finding.ClientFindingManager;
import com.surelogic.sierra.jdbc.record.ProjectRecord;
import com.surelogic.sierra.jdbc.settings.ClientSettingsManager;
import com.surelogic.sierra.tool.message.AuditTrailResponse;
import com.surelogic.sierra.tool.message.CommitAuditTrailRequest;
import com.surelogic.sierra.tool.message.CommitAuditTrailResponse;
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
import com.surelogic.sierra.tool.message.axis.SierraServiceClient;
public class ClientProjectManager extends ProjectManager {

	private final ClientFindingManager findingManager;
	private final PreparedStatement insertSynchRecord;
	private final PreparedStatement deleteSynchByProject;

	private ClientProjectManager(Connection conn) throws SQLException {
		super(conn);
		this.findingManager = ClientFindingManager.getInstance(conn);
		this.insertSynchRecord = conn
				.prepareStatement("INSERT INTO SYNCH (PROJECT_ID,DATE_TIME,COMMIT_REVISION,PRIOR_REVISION) VALUES (?,?,?,?)");
		this.deleteSynchByProject = conn
				.prepareStatement("DELETE FROM SYNCH WHERE PROJECT_ID = ?");
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
				;

		// Look up project. If it doesn't exist, create it and relate it to the
		// server.
		ProjectRecord p = projectFactory.newProject();
		p.setName(projectName);
		if (!p.select()) {
			p.insert();
		}
		String serverUid = p.getServerUid();
		if (serverUid == null) {
			serverUid = service.getUid(new ServerUIDRequest()).getUid();
			p.setServerUid(serverUid);
			p.update();
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
		CommitAuditTrailResponse commitResponse = service
				.commitAuditTrails(commitRequest);

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
		int idx = 1;
		insertSynchRecord.setLong(idx++, p.getId());
		insertSynchRecord.setTimestamp(idx++, new Timestamp(new Date()
				.getTime()));
		insertSynchRecord.setLong(idx++,
				commitResponse.getRevision() == null ? -1 : commitResponse
						.getRevision());
		insertSynchRecord.setLong(idx++, auditRequest.getRevision());
		insertSynchRecord.execute();
	}

	@Override
	public void deleteProject(String projectName, SLProgressMonitor monitor)
			throws SQLException {
		ProjectRecord rec = projectFactory.newProject();
		rec.setName(projectName);
		if (rec.select()) {
			if (monitor != null)
				monitor.subTask("Deleting scans for project " + projectName);
			scanManager.deleteScans(getProjectScans(rec.getId()), monitor);
			deleteSynchByProject.setLong(1, rec.getId());
			deleteSynchByProject.execute();
			findingManager.deleteFindings(projectName, monitor);
			if (monitor != null) {
				if (!monitor.isCanceled()) {
					rec.delete();
				}
			}
		}
	}

	public static ClientProjectManager getInstance(Connection conn)
			throws SQLException {
		return new ClientProjectManager(conn);
	}
}
