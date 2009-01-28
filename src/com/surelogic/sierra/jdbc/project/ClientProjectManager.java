package com.surelogic.sierra.jdbc.project;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import com.surelogic.common.jdbc.JDBCUtils;
import com.surelogic.common.jobs.SLProgressMonitor;
import com.surelogic.sierra.jdbc.finding.ClientFindingManager;
import com.surelogic.sierra.jdbc.record.ProjectRecord;
import com.surelogic.sierra.jdbc.settings.ConnectedServer;
import com.surelogic.sierra.tool.message.ServerMismatchException;
import com.surelogic.sierra.tool.message.SierraService;
import com.surelogic.sierra.tool.message.SierraServiceClient;
import com.surelogic.sierra.tool.message.SyncRequest;
import com.surelogic.sierra.tool.message.SyncResponse;
import com.surelogic.sierra.tool.message.SyncTrailRequest;
import com.surelogic.sierra.tool.message.SyncTrailResponse;

public final class ClientProjectManager extends ProjectManager {

	private final ClientFindingManager findingManager;
	private final PreparedStatement insertSynchRecord;
	private final PreparedStatement deleteSynchByProject;
	private final PreparedStatement selectServerUid;
	private final PreparedStatement insertServerUid;
	private final PreparedStatement deleteProjectScanFilter;

	private ClientProjectManager(final Connection conn) throws SQLException {
		super(conn);
		findingManager = ClientFindingManager.getInstance(conn);
		insertSynchRecord = conn
				.prepareStatement("INSERT INTO SYNCH (PROJECT_ID,DATE_TIME,COMMIT_REVISION,PRIOR_REVISION,COMMIT_COUNT,UPDATE_COUNT) VALUES (?,?,?,?,?,?)");
		deleteSynchByProject = conn
				.prepareStatement("DELETE FROM SYNCH WHERE PROJECT_ID = ?");
		selectServerUid = conn
				.prepareStatement("SELECT SERVER_UUID FROM PROJECT_SERVER WHERE PROJECT_ID = ?");
		insertServerUid = conn
				.prepareStatement("INSERT INTO PROJECT_SERVER (PROJECT_ID, SERVER_UUID) VALUES (?,?)");
		deleteProjectScanFilter = conn
				.prepareStatement("DELETE FROM SETTINGS_PROJECT_RELTN WHERE PROJECT_NAME = ?");
	}

	public ClientFindingManager getFindingManager() {
		return findingManager;
	}

	/**
	 * @return true if updated
	 */
	public boolean synchronizeProject(final ConnectedServer server,
			final String projectName, final SLProgressMonitor monitor)
			throws ServerMismatchException, SQLException {
		final SyncInfo info = synchronizeProjectWithServer(server, projectName, monitor, false);
		if (info == null) {
			return false;
		}
		return info.requiresUpdate();
	}

	public List<SyncTrailResponse> getProjectUpdates(
			final ConnectedServer server, final String projectName,
			final SLProgressMonitor monitor) throws ServerMismatchException,
			SQLException {
		final SyncInfo info = synchronizeProjectWithServer(server, projectName, monitor, true); 
		if (info == null) {
			return Collections.emptyList();
		}
		return info.response.getTrails();
	}

	private static class SyncInfo {
		final SyncRequest request;
		final SyncResponse response;
		
		SyncInfo(SyncRequest req, SyncResponse resp) {
			request = req;
			response = resp;
		}

		public boolean requiresUpdate() {
			return request.getRevision() != response.getCommitRevision();
			//return !request.getTrails().isEmpty() || !response.getTrails().isEmpty();
		}
	}
	
	private SyncInfo synchronizeProjectWithServer(
			final ConnectedServer server, final String projectName,
			final SLProgressMonitor monitor, final boolean serverGet)
			throws ServerMismatchException, SQLException {
		final SierraService service = SierraServiceClient.create(server
				.getLocation());

		// Look up project. If it doesn't exist, create it and relate it to the
		// server.
		final ProjectRecord p = projectFactory.newProject();
		p.setName(projectName);
		if (!p.select()) {
			p.insert();
		}

		// Resolve the server uid. If one isn't associated with this project,
		// then do so now.
		selectServerUid.setLong(1, p.getId());
		final ResultSet set = selectServerUid.executeQuery();
		String serverUid;
		try {
			if (set.next()) {
				serverUid = set.getString(1);
			} else {
				serverUid = server.getUuid();
				insertServerUid.setLong(1, p.getId());
				insertServerUid.setString(2, serverUid);
				insertServerUid.execute();
			}
		} finally {
			set.close();
		}
		if (monitor.isCanceled()) {
			return null;
		}
		monitor.worked(1);

		// Commit audits
		monitor.subTask("Sending local updates to the server.");
		final SyncRequest request = new SyncRequest();
		request.setProject(projectName);
		request.setServer(serverUid);
		request.setRevision(findingManager.getLatestAuditRevision(projectName));
		if (serverGet) {
			request.setTrails(Collections.<SyncTrailRequest> emptyList());
		} else {
			request.setTrails(findingManager.getNewLocalAuditTrails(
					projectName, monitor));
		}
		final SyncResponse reply = service.synchronizeProject(request);
		if (monitor.isCanceled()) {
			return null;
		}
		monitor.worked(1);

		new Projects(conn).updateProjectFilter(projectName, reply
				.getScanFilter());
		if (!serverGet) {
			monitor.subTask("Writing remote updates into local database.");
			findingManager.updateLocalAuditRevision(projectName, server
					.getLocation().getUser(), reply.getCommitRevision(),
					monitor);
			findingManager.updateLocalFindings(projectName, reply.getTrails(),
					monitor);
			if (monitor.isCanceled()) {
				return null;
			}
			monitor.worked(1);

			// Update settings
			// TODO
			monitor.subTask("Checking for updated settings for project "
					+ projectName);
			monitor.worked(1);
			int idx = 1;
			insertSynchRecord.setLong(idx++, p.getId());
			insertSynchRecord.setTimestamp(idx++, JDBCUtils.now());
			insertSynchRecord.setLong(idx++, findingManager
					.getLatestAuditRevision(projectName));
			insertSynchRecord.setLong(idx++, request.getRevision());
			int commitCount = 0;
			int updateCount = 0;
			for (final SyncTrailRequest req : request.getTrails()) {
				commitCount += req.getAudits().size();
			}
			for (final SyncTrailResponse rep : reply.getTrails()) {
				updateCount += rep.getAudits().size();
			}
			insertSynchRecord.setLong(idx++, commitCount);
			insertSynchRecord.setLong(idx++, updateCount);
			insertSynchRecord.execute();
		}
		return new SyncInfo(request, reply);
	}

	@Override
	public void deleteProject(final String projectName,
			final SLProgressMonitor monitor) throws SQLException {
		final ProjectRecord rec = projectFactory.newProject();
		rec.setName(projectName);
		if (rec.select()) {
			if (monitor != null) {
				monitor.subTask("Deleting scans for project " + projectName);
			}
			scanManager.deleteScans(getProjectScans(rec.getId()), monitor);
			deleteSynchByProject.setLong(1, rec.getId());
			deleteSynchByProject.execute();
			findingManager.deleteFindings(projectName, monitor);
			deleteProjectScanFilter.setString(1, projectName);
			deleteProjectScanFilter.execute();
			if (monitor != null) {
				if (!monitor.isCanceled()) {
					rec.delete();
				}
			}
		}
	}

	public static ClientProjectManager getInstance(final Connection conn)
			throws SQLException {
		return new ClientProjectManager(conn);
	}
}
