package com.surelogic.sierra.tool.message;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.jdbc.finding.ServerFindingManager;
import com.surelogic.sierra.jdbc.qualifier.QualifierManager;
import com.surelogic.sierra.jdbc.scan.ScanManager;
import com.surelogic.sierra.jdbc.server.Server;
import com.surelogic.sierra.jdbc.server.ServerConnection;
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

	JAXBContext ctx;

	@PostConstruct
	public void init() {
		try {
			ctx = JAXBContext.newInstance(Settings.class);
		} catch (JAXBException e) {
			log.log(Level.SEVERE,
					"An error occurred while initializing a web service bean.",
					e);
		}
	}

	public SettingsReply getSettings(SettingsRequest request)
			throws ServerMismatchException {
		SettingsReply reply = new SettingsReply();
		// TODO
		return reply;
	}

	public void publishRun(final Scan scan) {
		// We can't publish a run without a qualifier on the server
		final List<String> q = scan.getConfig().getQualifiers();
		if (q == null || q.isEmpty()) {
			log
					.fine("No qualifiers were specified in this scan, could not persist scan "
							+ scan.getUid());
			return;
		}

		ServerConnection.withTransaction(new WebTransaction<Object>() {

			@Override
			public Object perform(Connection conn, Server server)
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
				generator.qualifiers(q).user(getUserName());
				MessageWarehouse.readScan(scan, generator);
				conn.commit();
				ServerConnection.delayTransaction(new WebTransaction<Object>() {
					@Override
					public Object perform(Connection conn, Server server)
							throws SQLException {
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

	public Qualifiers getQualifiers(QualifierRequest request) {
		return ServerConnection.withReadOnly(new WebTransaction<Qualifiers>() {

			@Override
			public Qualifiers perform(Connection conn, Server server)
					throws SQLException {
				Qualifiers q = new Qualifiers();
				q.setQualifier(QualifierManager.getInstance(conn)
						.getAllQualifierNames());
				return q;
			}
		});
	}

	public CommitAuditTrailResponse commitAuditTrails(
			final CommitAuditTrailRequest audits)
			throws ServerMismatchException {
		final List<AuditTrail> trails = audits.getAuditTrail();
		if (trails != null && !trails.isEmpty()) {
			final CommitAuditTrailResponse response = ServerConnection
					.withTransaction(new WebTransaction<CommitAuditTrailResponse>() {

						@Override
						public CommitAuditTrailResponse perform(
								Connection conn, Server server)
								throws SQLException {
							final CommitAuditTrailResponse response = new CommitAuditTrailResponse();
							final long revision = server.nextRevision();
							response.setRevision(revision);
							response.setUid(ServerFindingManager.getInstance(
									conn).commitAuditTrails(
									User
											.getUser(getUserName(),
													conn).getId(), revision,
									trails));
							return response;
						}
					});
			return response;
		} else {
			return new CommitAuditTrailResponse();
		}
	}

	/**
	 * @throws ServerMismatchException
	 * 
	 */
	@SuppressWarnings("unchecked")
	public AuditTrailResponse getAuditTrails(final GetAuditTrailRequest request)
			throws ServerMismatchException {
		return ServerConnection
				.withReadOnly(new WebTransaction<AuditTrailResponse>() {

					@Override
					public AuditTrailResponse perform(Connection conn,
							Server server) throws SQLException {
						AuditTrailResponse response = new AuditTrailResponse();
						String project = request.getProject();
						Long revision = request.getRevision();
						ServerFindingManager man = ServerFindingManager
								.getInstance(conn);
						response.setObsolete(man.getObsoletedTrails(project,
								revision));
						response.setUpdate(man.getAuditUpdates(project,
								revision));

						return response;
					}

				});

	}

	/**
	 * Finds or creates an trail that contains all of the provided matches for
	 * each requested merge. If none of the matches exist, we create a new trail
	 * and assign it to each match. If some of the matches belong to one trail,
	 * and the others none, then we merely assign the others to the existing
	 * trail. Finally, if we have multiple trails, we create an entirely new
	 * one, and obsolete each existing trail with the new one.
	 * 
	 * @throws ServerMismatchException
	 */
	public MergeAuditTrailResponse mergeAuditTrails(
			final MergeAuditTrailRequest seed) throws ServerMismatchException {
		return ServerConnection
				.withTransaction(new WebTransaction<MergeAuditTrailResponse>() {

					@Override
					public MergeAuditTrailResponse perform(Connection conn,
							Server server) throws SQLException {
						final MergeAuditTrailResponse response = new MergeAuditTrailResponse();
						final List<Merge> merges = seed.getMerge();
						final long revision = server.nextRevision();
						response.setTrail(ServerFindingManager
								.getInstance(conn).mergeAuditTrails(
										seed.getProject(), revision, merges));
						return response;
					}
				});
	}

	public ServerUIDReply getUid(ServerUIDRequest request) {
		ServerUIDReply reply = new ServerUIDReply();
		reply.setUid(ServerConnection
				.withReadOnly(new WebTransaction<String>() {
					@Override
					public String perform(Connection conn, Server server)
							throws SQLException {
						return server.getUid();
					}
				}));
		return reply;
	}

	public GlobalSettings getGlobalSettings(GlobalSettingsRequest request) {
		return ServerConnection
				.withReadOnly(new WebTransaction<GlobalSettings>() {

					@Override
					public GlobalSettings perform(Connection conn, Server server)
							throws SQLException {
						final GlobalSettings settings = new GlobalSettings();
						settings.setFilter(SettingsManager.getInstance(conn)
								.getGlobalSettings());
						return settings;
					}
				});
	}

	public void writeGlobalSettings(final GlobalSettings settings) {
		ServerConnection.withTransaction(new WebTransaction<Object>() {
			@Override
			public Object perform(Connection conn, Server server)
					throws SQLException {
				SettingsManager.getInstance(conn).writeGlobalSettings(
						settings.getFilter());
				return null;
			}
		});
	}

	private abstract class WebTransaction<T> implements UserTransaction<T> {

		private final String userName;

		WebTransaction() {
			userName = getCurrentPrincipal().getName();
		}

		@Override
		public String getUserName() {
			return userName;
		}

	}

}
