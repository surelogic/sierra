package com.surelogic.sierra.tool.message;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.jdbc.finding.ServerFindingManager;
import com.surelogic.sierra.jdbc.project.ProjectRecordFactory;
import com.surelogic.sierra.jdbc.qualifier.QualifierManager;
import com.surelogic.sierra.jdbc.record.ProjectRecord;
import com.surelogic.sierra.jdbc.scan.ScanManager;
import com.surelogic.sierra.jdbc.server.ConnectionFactory;
import com.surelogic.sierra.jdbc.server.Server;
import com.surelogic.sierra.jdbc.server.ServerTransaction;
import com.surelogic.sierra.jdbc.server.UserContext;
import com.surelogic.sierra.jdbc.server.UserTransaction;
import com.surelogic.sierra.jdbc.settings.SettingsManager;
import com.surelogic.sierra.jdbc.tool.FindingFilter;
import com.surelogic.sierra.jdbc.tool.FindingTypeManager;
import com.surelogic.sierra.jdbc.user.User;
import com.surelogic.sierra.message.srpc.SRPCServlet;

/**
 * Implementation of {@link SierraService}.
 * 
 * TODO this implementation does not currently validate server uid. We need to
 * fix this.
 * 
 * @author nathan
 * 
 */
public class SierraServiceImpl extends SRPCServlet implements SierraService {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8265889420077755990L;

	private static final Logger log = SLLogger
			.getLoggerFor(SierraServiceImpl.class);

	public SettingsReply getSettings(SettingsRequest request)
			throws ServerMismatchException {
		SettingsReply reply = new SettingsReply();
		// TODO
		return reply;
	}

	public void publishRun(final Scan scan) {
		// We can't publish a run without a qualifier on the server
		final List<String> q = scan.getConfig().getQualifiers();
		ConnectionFactory.withUserTransaction(new UserTransaction<Void>() {

			public Void perform(Connection conn, Server server, User user)
					throws SQLException {
				final String uid = scan.getUid();
				final String project = scan.getConfig().getProject();
				final ScanManager manager = ScanManager.getInstance(conn);
				final FindingFilter filter = FindingTypeManager.getInstance(
						conn).getMessageFilter(
						SettingsManager.getInstance(conn).getSettingsByProject(
								project));
				final ScanGenerator generator = manager
						.getScanGenerator(filter);
				generator.qualifiers(q).user(user.getName());
				MessageWarehouse.readScan(scan, generator);
				conn.commit();
				ConnectionFactory
						.delayUserTransaction(new UserTransaction<Void>() {

							public Void perform(Connection conn, Server server,
									User user) throws SQLException {
								ServerFindingManager fm = ServerFindingManager
										.getInstance(conn);
								fm.generateFindings(project, uid, filter, null);
								fm.generateOverview(project, uid,
										new HashSet<String>(q));
								return null;
							}
						});
				return null;
			}
		});
	}

	public Timeseries getQualifiers(TimeseriesRequest request) {
		return ConnectionFactory
				.withUserReadOnly(new UserTransaction<Timeseries>() {

					public Timeseries perform(Connection conn, Server server,
							User user) throws SQLException {
						Timeseries q = new Timeseries();
						q.setQualifier(QualifierManager.getInstance(conn)
								.getAllQualifierNames());
						return q;
					}
				});
	}

	public SyncResponse synchronizeProject(SyncRequest request)
			throws ServerMismatchException {
		final String serverUid = request.getServer();
		final String project = request.getProject();
		final List<SyncTrailRequest> trails = request.getTrails();
		final UserTransaction<Void> commitChanges = new UserTransaction<Void>() {
			public Void perform(Connection conn, Server server, User user)
					throws Exception {
				final ProjectRecord projectRecord = ProjectRecordFactory
						.getInstance(conn).newProject();
				projectRecord.setName(project);
				if (!projectRecord.select()) {
					projectRecord.insert();
				}
				final long projectId = projectRecord.getId();
				final long revision = server.nextRevision();
				final List<Merge> merges = new ArrayList<Merge>(trails.size());
				final List<AuditTrail> audits = new ArrayList<AuditTrail>(
						trails.size());
				for (SyncTrailRequest trail : trails) {
					final AuditTrail audit = new AuditTrail();
					audit.setAudits(trail.getAudits());
					audits.add(audit);
					merges.add(trail.getMerge());
				}
				final ServerFindingManager man = ServerFindingManager
						.getInstance(conn);
				final List<String> uids = man.mergeAuditTrails(projectId,
						revision, merges);
				final Iterator<AuditTrail> auditIter = audits.iterator();
				for (String uid : uids) {
					auditIter.next().setFinding(uid);
				}
				man
						.commitAuditTrails(projectId, user.getId(), revision,
								audits);
				return null;
			}
		};
		final long revision = request.getRevision();
		final UserTransaction<SyncResponse> readChanges = new UserTransaction<SyncResponse>() {

			public SyncResponse perform(Connection conn, Server server,
					User user) throws Exception {
				return ServerFindingManager.getInstance(conn).getAuditUpdates(
						project, revision);

			}
		};
		final String localUid = ConnectionFactory
				.withReadOnly(new ServerTransaction<String>() {
					public String perform(Connection conn, Server server)
							throws Exception {
						return server.getUid();
					}
				});
		// Begin transaction
		if (!localUid.equals(serverUid)) {
			throw new ServerMismatchException(serverUid
					+ " does not match the server's uid: " + localUid);
		}
		if (trails != null && !trails.isEmpty()) {
			return ConnectionFactory
					.withUserTransaction(new UserTransaction<SyncResponse>() {

						public SyncResponse perform(Connection conn,
								Server server, User user) throws Exception {
							commitChanges.perform(conn, server, user);
							return readChanges.perform(conn, server, user);
						}
					});
		} else {
			return ConnectionFactory
					.withUserReadOnly(new UserTransaction<SyncResponse>() {

						public SyncResponse perform(Connection conn,
								Server server, User user) throws Exception {
							return readChanges.perform(conn, server, user);
						}
					});
		}
	}

	public ServerUIDReply getUid(ServerUIDRequest request) {
		ServerUIDReply reply = new ServerUIDReply();
		reply.setUid(ConnectionFactory
				.withUserReadOnly(new UserTransaction<String>() {

					public String perform(Connection conn, Server server,
							User user) throws SQLException {
						return server.getUid();
					}
				}));
		return reply;
	}

	public GlobalSettings getGlobalSettings(GlobalSettingsRequest request) {
		return ConnectionFactory
				.withUserReadOnly(new UserTransaction<GlobalSettings>() {

					public GlobalSettings perform(Connection conn,
							Server server, User user) throws SQLException {
						final GlobalSettings settings = new GlobalSettings();
						settings.setFilter(SettingsManager.getInstance(conn)
								.getGlobalSettings());
						return settings;
					}
				});
	}

	public void writeGlobalSettings(final GlobalSettings settings) {
		ConnectionFactory.withUserTransaction(new UserTransaction<Object>() {

			public Object perform(Connection conn, Server server, User user)
					throws SQLException {
				SettingsManager.getInstance(conn).writeGlobalSettings(
						settings.getFilter());
				return null;
			}
		});
	}

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		try {
			UserContext.set((User) req.getAttribute("SierraUser"));
			super.service(req, resp);
		} finally {
			UserContext.remove();
		}
	}
}
