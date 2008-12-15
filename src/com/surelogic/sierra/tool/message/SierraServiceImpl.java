package com.surelogic.sierra.tool.message;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.surelogic.common.jdbc.ConnectionQuery;
import com.surelogic.common.jobs.NullSLProgressMonitor;
import com.surelogic.sierra.jdbc.finding.ServerFindingManager;
import com.surelogic.sierra.jdbc.project.ProjectRecordFactory;
import com.surelogic.sierra.jdbc.project.Projects;
import com.surelogic.sierra.jdbc.record.ProjectRecord;
import com.surelogic.sierra.jdbc.record.ScanRecord;
import com.surelogic.sierra.jdbc.scan.ScanManager;
import com.surelogic.sierra.jdbc.scan.ScanRecordFactory;
import com.surelogic.sierra.jdbc.server.ConnectionFactory;
import com.surelogic.sierra.jdbc.server.Server;
import com.surelogic.sierra.jdbc.server.ServerTransaction;
import com.surelogic.sierra.jdbc.server.UserTransaction;
import com.surelogic.sierra.jdbc.settings.SettingQueries;
import com.surelogic.sierra.jdbc.timeseries.TimeseriesManager;
import com.surelogic.sierra.jdbc.tool.FindingFilter;
import com.surelogic.sierra.jdbc.user.User;

/**
 * Implementation of {@link SierraService}.
 * 
 * @author nathan
 * 
 */
public class SierraServiceImpl extends SecureServiceServlet implements
		SierraService {

	private static final long serialVersionUID = -8265889420077755990L;
	private boolean on;

	public void publishRun(final Scan scan) throws ScanVersionException {
		if (!Server.getSoftwareVersion().equals(scan.getVersion())) {
			throw new ScanVersionException();
		}
		// We can't publish a run without a timeseries on the server
		final List<String> qList = scan.getConfig().getTimeseries();
		final Set<String> timeseries = new TreeSet<String>();
		timeseries.add(TimeseriesManager.ALL_SCANS);
		if (qList != null) {
			timeseries.addAll(qList);
		}
		ConnectionFactory.getInstance().withUserTransaction(
				new UserTransaction<Void>() {

					public Void perform(final Connection conn,
							final Server server, final User user)
							throws SQLException {
						final String uid = scan.getUid();
						final ScanRecord s = ScanRecordFactory
								.getInstance(conn).newScan();
						s.setUid(uid);
						if (!s.select()) {
							final String project = scan.getConfig()
									.getProject();
							final ScanManager manager = ScanManager
									.getInstance(conn);
							final FindingFilter filter = SettingQueries
									.scanFilterForProject(project).perform(
											new ConnectionQuery(conn));
							final ScanGenerator generator = manager
									.getScanGenerator(filter);
							generator.timeseries(timeseries).user(
									user.getName());
							MessageWarehouse.readScan(scan, generator);
							ConnectionFactory.getInstance()
									.delayUserTransaction(
											new UserTransaction<Void>() {

												public Void perform(
														final Connection conn,
														final Server server,
														final User user)
														throws SQLException {
													final ServerFindingManager fm = ServerFindingManager
															.getInstance(conn);
													try {
														fm.generateFindings(
																project, uid,
																filter, null);
														fm.generateOverview(
																project, uid,
																timeseries);
														// Increment the
														// revision whenever a
														// scan is
														// published.
														server.nextRevision();
														conn.commit();
														ScanManager
																.getInstance(
																		conn)
																.finalizeScan(
																		uid);
														log.info("Scan " + uid
																+ " finalized");
													} catch (final RuntimeException e) {
														handleScanException(
																conn, uid);
														throw e;
													} catch (final SQLException e) {
														handleScanException(
																conn, uid);
														throw e;
													}
													return null;
												}
											});
						}
						return null;
					}
				});
	}

	private void handleScanException(final Connection conn, final String uid) {
		try {
			conn.rollback();
			ScanManager.getInstance(conn).deleteScan(uid,
					new NullSLProgressMonitor());
		} catch (final Exception e1) {
			log
					.log(
							Level.SEVERE,
							"An error occurred while attempting to delete a failed scan from the database.",
							e1);
		}
	}

	public Timeseries getTimeseries(final TimeseriesRequest request) {
		return ConnectionFactory.getInstance().withUserReadOnly(
				new UserTransaction<Timeseries>() {

					public Timeseries perform(final Connection conn,
							final Server server, final User user)
							throws SQLException {
						final Timeseries q = new Timeseries();
						q.setTimeseries(TimeseriesManager.getInstance(conn)
								.getAllTimeseriesNames());
						return q;
					}
				});
	}

	public SyncResponse synchronizeProject(final SyncRequest request)
			throws ServerMismatchException {
		final String serverUid = request.getServer();
		final String project = request.getProject();
		final List<SyncTrailRequest> trails = request.getTrails();
		final UserTransaction<Long> commitChanges = new UserTransaction<Long>() {
			public Long perform(final Connection conn, final Server server,
					final User user) throws Exception {
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
				for (final SyncTrailRequest trail : trails) {
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
				for (final String uid : uids) {
					auditIter.next().setFinding(uid);
				}
				man
						.commitAuditTrails(projectId, user.getId(), revision,
								audits);
				return revision;
			}
		};
		final long revision = request.getRevision();
		final UserTransaction<SyncResponse> readChanges = new UserTransaction<SyncResponse>() {

			public SyncResponse perform(final Connection conn,
					final Server server, final User user) throws Exception {
				return ServerFindingManager.getInstance(conn).getAuditUpdates(
						project, revision);

			}
		};
		final String localUid = ConnectionFactory.getInstance().withReadOnly(
				new ServerTransaction<String>() {
					public String perform(final Connection conn,
							final Server server) throws Exception {
						return server.getUid();
					}
				});
		// Begin transaction
		if (!localUid.equals(serverUid)) {
			throw new ServerMismatchException(serverUid
					+ " does not match the server's uid: " + localUid);
		}
		return ConnectionFactory.getInstance().withUserTransaction(
				new UserTransaction<SyncResponse>() {

					public SyncResponse perform(final Connection conn,
							final Server server, final User user)
							throws Exception {
						final SyncResponse response = readChanges.perform(conn,
								server, user);
						if (trails != null && !trails.isEmpty()) {
							response.setCommitRevision(commitChanges.perform(
									conn, server, user));
						}
						response.setScanFilter(new Projects(conn)
								.getProjectFilter(project));
						return response;
					}
				});
	}

	@Override
	protected void service(final HttpServletRequest req,
			final HttpServletResponse resp) throws ServletException,
			IOException {
		synchronized (this) {
			if (on) {
				super.service(req, resp);
			} else {
				resp.setStatus(404);
			}
		}

	}

	@Override
	public void init(final ServletConfig config) throws ServletException {
		super.init(config);
		synchronized (this) {
			on = "on".equals(config.getServletContext().getInitParameter(
					"teamserver"));
		}
	}
}
