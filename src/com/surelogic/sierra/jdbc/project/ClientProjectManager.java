package com.surelogic.sierra.jdbc.project;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.surelogic.common.SLProgressMonitor;
import com.surelogic.sierra.jdbc.JDBCUtils;
import com.surelogic.sierra.jdbc.finding.ClientFindingManager;
import com.surelogic.sierra.jdbc.record.ProjectRecord;
import com.surelogic.sierra.tool.message.ServerMismatchException;
import com.surelogic.sierra.tool.message.ServerUIDRequest;
import com.surelogic.sierra.tool.message.SierraServerLocation;
import com.surelogic.sierra.tool.message.SierraService;
import com.surelogic.sierra.tool.message.SierraServiceClient;
import com.surelogic.sierra.tool.message.SyncRequest;
import com.surelogic.sierra.tool.message.SyncResponse;

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
		final SierraService service = SierraServiceClient.create(server);

		// Look up project. If it doesn't exist, create it and relate it to the
		// server.
		final ProjectRecord p = projectFactory.newProject();
		p.setName(projectName);
		if (!p.select()) {
			p.insert();
		}

		// TODO put server uid back into project
		final String serverUid = service.getUid(new ServerUIDRequest())
				.getUid();
		if (monitor.isCanceled()) {
			return;
		}
		monitor.worked(1);
		monitor.subTask("Sending local updates to the server.");
		// Commit audits
		final SyncRequest request = new SyncRequest();
		request.setProject(projectName);
		request.setServer(serverUid);
		request.setRevision(findingManager.getLatestAuditRevision(projectName));
		request.setTrails(findingManager
				.getNewLocalAudits(projectName, monitor));
		final SyncResponse reply = service.synchronizeProject(request);
		monitor.worked(1);
		monitor.subTask("Writing remote updates into local database.");
		findingManager.deleteLocalAudits(projectName, monitor);
		findingManager.updateLocalFindings(projectName, reply.getTrails(),
				monitor);
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
