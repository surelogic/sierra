package com.surelogic.sierra.jdbc.project;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.surelogic.common.jdbc.JDBCUtils;
import com.surelogic.common.jobs.SLProgressMonitor;
import com.surelogic.sierra.jdbc.finding.ClientFindingManager;
import com.surelogic.sierra.jdbc.record.ProjectRecord;
import com.surelogic.sierra.jdbc.settings.ConnectedServer;
import com.surelogic.sierra.tool.message.ServerMismatchException;
import com.surelogic.sierra.tool.message.SierraService;
import com.surelogic.sierra.tool.message.SierraServiceClient;
import com.surelogic.sierra.tool.message.SyncProjectRequest;
import com.surelogic.sierra.tool.message.SyncProjectResponse;
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
				.prepareStatement("INSERT INTO SYNCH (PROJECT_ID,DATE_TIME,COMMIT_REVISION,PRIOR_REVISION,COMMIT_COUNT,UPDATE_COUNT) VALUES ((SELECT ID FROM PROJECT WHERE NAME = ?),?,?,?,?,?)");
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

	public boolean synchronizeProjects(final ConnectedServer server,
			final List<String> projectNames, final SLProgressMonitor monitor)
			throws ServerMismatchException, SQLException {
		final SyncInfo info = synchronizeProjectsWithServer(server,
				projectNames, monitor, false);
		if (info == null) {
			return false;
		}
		return info.requiresUpdate();
	}

	/**
	 * @return true if updated
	 */
	public boolean synchronizeProject(final ConnectedServer server,
			final String projectName, final SLProgressMonitor monitor)
			throws ServerMismatchException, SQLException {
		final SyncProjectInfo info = synchronizeProjectWithServer(server,
				projectName, monitor, false);
		if (info == null) {
			return false;
		}
		return info.requiresUpdate();
	}

	public List<SyncTrailResponse> getProjectUpdates(
			final ConnectedServer server, final String projectName,
			final SLProgressMonitor monitor) throws ServerMismatchException,
			SQLException {
		final SyncProjectInfo info = synchronizeProjectWithServer(server,
				projectName, monitor, true);
		if (info == null) {
			return Collections.emptyList();
		}
		return info.response.getTrails();
	}

	private static class SyncProjectInfo {
		final SyncProjectRequest request;
		final SyncProjectResponse response;
		private boolean updated;

		SyncProjectInfo(final SyncProjectRequest req,
				final SyncProjectResponse resp, final boolean updated) {
			request = req;
			response = resp;
		}

		public boolean requiresUpdate() {
			return updated;
		}
	}

	private static class SyncInfo {
		final SyncRequest request;
		final SyncResponse response;
		private boolean updated;

		SyncInfo(final SyncRequest req, final SyncResponse resp,
				final boolean updated) {
			request = req;
			response = resp;
		}

		public boolean requiresUpdate() {
			return updated;
		}
	}

	private SyncInfo synchronizeProjectsWithServer(
			final ConnectedServer server, final List<String> projectNames,
			final SLProgressMonitor monitor, final boolean serverGet)
			throws ServerMismatchException, SQLException {
		if (projectNames.isEmpty()) {
			throw new IllegalArgumentException(
					"You must specify at least one project to synchronize on");
		}
		final SierraService service = SierraServiceClient.create(server
				.getLocation());
		final SyncRequest request = new SyncRequest();
		for (final String projectName : projectNames) {
			request.getProjects().add(
					getSyncProjectRequest(server, projectName, monitor,
							serverGet));
		}
		final SyncResponse reply = service.synchronize(request);
		final Iterator<String> namesIter = projectNames.iterator();
		final Iterator<SyncProjectRequest> requestIter = request.getProjects()
				.iterator();
		boolean updated = false;
		for (final SyncProjectResponse projectReply : reply.getProjects()) {
			updated |= updateProject(server, namesIter.next(), requestIter
					.next(), projectReply, monitor);
		}
		if (monitor.isCanceled()) {
			return null;
		}
		return new SyncInfo(request, reply, updated);
	}

	private SyncProjectInfo synchronizeProjectWithServer(
			final ConnectedServer server, final String projectName,
			final SLProgressMonitor monitor, final boolean serverGet)
			throws ServerMismatchException, SQLException {
		final SierraService service = SierraServiceClient.create(server
				.getLocation());
		if (monitor.isCanceled()) {
			return null;
		}
		final SyncProjectRequest request = getSyncProjectRequest(server,
				projectName, monitor, serverGet);
		monitor.worked(1);

		// Commit audits
		monitor.subTask("Sending local updates to the server.");
		final SyncProjectResponse reply = service.synchronizeProject(request);
		if (monitor.isCanceled()) {
			return null;
		}
		monitor.worked(1);
		boolean updated = false;
		if (!serverGet) {
			updated = updateProject(server, projectName, request, reply,
					monitor);
		}
		if (monitor.isCanceled()) {
			return null;
		}
		return new SyncProjectInfo(request, reply, updated);
	}

	private SyncProjectRequest getSyncProjectRequest(
			final ConnectedServer server, final String projectName,
			final SLProgressMonitor monitor, final boolean serverGet)
			throws SQLException {
		// Look up project. If it doesn't exist, create it and relate it to
		// the server.
		final ProjectRecord p = projectFactory.newProject();
		p.setName(projectName);
		if (!p.select()) {
			p.insert();
		}
		// Resolve the server uid. If one isn't associated with this
		// project, then do so now.
		selectServerUid.setLong(1, p.getId());
		final ResultSet set = selectServerUid.executeQuery();
		try {
			if (set.next()) {
				if (!server.getUuid().equals(set.getString(1))) {
					throw new IllegalArgumentException(
							String
									.format(
											"The project %s is already associated with a different server than %s.",
											projectName, server.getUuid()));
				}
			} else {
				insertServerUid.setLong(1, p.getId());
				insertServerUid.setString(2, server.getUuid());
				insertServerUid.execute();
			}
		} finally {
			set.close();
		}
		final SyncProjectRequest request = new SyncProjectRequest();
		request.setProject(projectName);
		request.setServer(server.getUuid());
		request.setRevision(findingManager.getLatestAuditRevision(projectName));
		if (serverGet) {
			request.setTrails(Collections.<SyncTrailRequest> emptyList());
		} else {
			request.setTrails(findingManager.getNewLocalAuditTrails(
					projectName, monitor));
		}
		return request;
	}

	private boolean updateProject(final ConnectedServer server,
			final String projectName, final SyncProjectRequest request,
			final SyncProjectResponse reply, final SLProgressMonitor monitor)
			throws SQLException {
		monitor.subTask("Writing remote updates into local database.");
		final boolean updated = reply.getScanFilter().equals(
				new Projects(conn).updateProjectFilter(projectName, reply
						.getScanFilter()))
				&& reply.getCommitRevision() == request.getRevision();
		findingManager.updateLocalAuditRevision(projectName, server
				.getLocation().getUser(), reply.getCommitRevision(), monitor);
		findingManager.updateLocalFindings(projectName, reply.getTrails(),
				monitor);
		if (monitor.isCanceled()) {
			return false;
		}
		monitor.worked(1);

		// Update settings
		// TODO
		monitor.subTask("Checking for updated settings for project "
				+ projectName);
		monitor.worked(1);
		int idx = 1;
		insertSynchRecord.setString(idx++, projectName);
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
		return updated;
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
