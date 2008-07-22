package com.surelogic.sierra.gwt.server;

import java.sql.Connection;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.surelogic.common.jdbc.NullRowHandler;
import com.surelogic.common.jdbc.Query;
import com.surelogic.common.jdbc.Row;
import com.surelogic.common.jdbc.RowHandler;
import com.surelogic.common.jdbc.SingleRowHandler;
import com.surelogic.sierra.gwt.SierraServiceServlet;
import com.surelogic.sierra.gwt.client.data.ArtifactOverview;
import com.surelogic.sierra.gwt.client.data.AuditOverview;
import com.surelogic.sierra.gwt.client.data.FindingOverview;
import com.surelogic.sierra.gwt.client.data.ImportanceView;
import com.surelogic.sierra.gwt.client.data.Result;
import com.surelogic.sierra.gwt.client.data.Scan;
import com.surelogic.sierra.gwt.client.data.ScanDetail;
import com.surelogic.sierra.gwt.client.service.FindingService;
import com.surelogic.sierra.jdbc.finding.ServerFindingManager;
import com.surelogic.sierra.jdbc.scan.ScanInfo;
import com.surelogic.sierra.jdbc.scan.Scans;
import com.surelogic.sierra.jdbc.server.ConnectionFactory;
import com.surelogic.sierra.jdbc.server.NullUserTransaction;
import com.surelogic.sierra.jdbc.server.Server;
import com.surelogic.sierra.jdbc.server.UserQuery;
import com.surelogic.sierra.jdbc.user.User;
import com.surelogic.sierra.tool.message.AuditEvent;
import com.surelogic.sierra.tool.message.Importance;
import com.surelogic.sierra.util.Dates;

public class FindingServiceImpl extends SierraServiceServlet implements
		FindingService {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5522046767503450943L;

	public Result<FindingOverview> getFinding(final String key) {
		if ((key == null) || "".equals(key)) {
			return Result.failure("No key specified", null);
		}
		return ConnectionFactory
				.withUserReadOnly(new UserQuery<Result<FindingOverview>>() {

					public Result<FindingOverview> perform(final Query query,
							final Server server, final User user) {
						try {
							// Is it a long
							final long id = Long.parseLong(key);
							return query.prepared(
									"portal.finding.byId",
									SingleRowHandler
											.from(new FindingOverviewHandler(
													query))).call(id);
						} catch (final NumberFormatException e) {
							// Try it as a uuid now
							UUID.fromString(key);
							return query.prepared(
									"portal.finding.byUuid",
									SingleRowHandler
											.from(new FindingOverviewHandler(
													query))).call(key);
						}
					}
				});
	}

	private static class FindingOverviewHandler implements
			RowHandler<Result<FindingOverview>> {
		private final Query query;

		FindingOverviewHandler(final Query q) {
			query = q;
		}

		public Result<FindingOverview> handle(final Row r) {
			final FindingOverview f = new FindingOverview();
			final long id = r.nextLong();
			f.setFindingId(id);
			f.setImportance(ImportanceView.values()[r.nextInt()]);
			f.setSummary(r.nextString());
			f.setFindingType(r.nextString());
			f.setCategory(r.nextString());
			f.setProject(r.nextString());
			f.setPackageName(r.nextString());
			f.setClassName(r.nextString());
			f.setAudits(query.prepared("portal.finding.auditsById",
					new RowHandler<AuditOverview>() {
						public AuditOverview handle(final Row r) {
							final AuditOverview audit = new AuditOverview();
							final String event = r.nextString();
							final String value = r.nextString();
							switch (AuditEvent.valueOf(event)) {
							case COMMENT:
								audit.setText(value);
								break;
							case IMPORTANCE:
								audit.setText("Importance changed to "
										+ Importance.fromValue(value)
												.toStringSentenceCase() + ".");
								break;
							case READ:
								audit.setText("Finding examined.");
								break;
							case SUMMARY:
								audit.setText("Changed summary to \"" + value
										+ "\"");
								break;
							default:
								break;
							}
							audit.setTime(Dates.format(r.nextDate()));
							audit.setUser(r.nextString());
							return audit;
						}
					}).call(id));
			f.setArtifacts(query.prepared("portal.finding.artifactsById",
					new RowHandler<ArtifactOverview>() {
						public ArtifactOverview handle(final Row r) {
							final ArtifactOverview ao = new ArtifactOverview();
							ao.setTime(Dates.format(r.nextDate()));
							ao.setTool(r.nextString());
							ao.setType(r.nextString());
							ao.setSummary(r.nextString());
							return ao;
						}
					}).call(id));
			return Result.success(f);
		}

	}

	public List<Scan> getScans(final String project) {
		return ConnectionFactory.withUserReadOnly(new UserQuery<List<Scan>>() {
			public List<Scan> perform(final Query query, final Server server,
					final User user) {
				final List<Scan> scans = new ArrayList<Scan>();
				for (final ScanInfo info : new Scans(query)
						.getScanInfo(project)) {
					final Scan s = new Scan();
					s.setJavaVendor(info.getJavaVendor());
					s.setJavaVersion(info.getJavaVersion());
					s.setProject(info.getProject());
					s.setScanTime(info.getScanTime());
					s.setScanTimeDisplay(Dates.format(info.getScanTime()));
					s.setUser(info.getUser());
					s.setUuid(info.getUid());
					scans.add(s);
				}
				return scans;
			}
		});
	}

	public ScanDetail getScanDetail(final String uuid) {
		return ConnectionFactory.withUserReadOnly(new UserQuery<ScanDetail>() {
			public ScanDetail perform(final Query query, final Server server,
					final User user) {
				if ((uuid != null) && !(uuid.length() == 0)) {
					final ScanDetail d = new ScanDetail();
					final Map<String, List<String>> compilations = d
							.getCompilations();
					final NullRowHandler detailHandler = new NullRowHandler() {
						@Override
						protected void doHandle(final Row r) {
							final Date time = r.nextDate();
							final String project = r.nextString();
							final int findingCount = r.nextInt();
							query.prepared("Scans.scanMetricDetails",
									SingleRowHandler.from(new NullRowHandler() {
										@Override
										protected void doHandle(final Row r) {
											final int packageCount = r
													.nextInt();
											final int classCount = r.nextInt();
											final int lineCount = r.nextInt();
											d.setClasses(Integer
													.toString(classCount)
													+ " classes");
											d.setDate(Dates.format(time));
											final double density = 1000
													* (double) findingCount
													/ lineCount;
											d.setDensity(NumberFormat
													.getInstance().format(
															density)
													+ " findings/kLoC");
											d.setFindings(Integer
													.toString(findingCount)
													+ " findings");
											d.setLinesOfCode(Integer
													.toString(lineCount)
													+ " lines of code");
											d.setPackages(Integer
													.toString(packageCount)
													+ " packages");
											d.setProject(project);
										}
									})).call(uuid);
						}
					};
					query.prepared("Scans.scanFindingDetails",
							SingleRowHandler.from(detailHandler)).call(uuid);
					query.prepared("Scans.scanCompilations",
							new NullRowHandler() {
								@Override
								protected void doHandle(final Row r) {
									final String pakkage = r.nextString();
									final String clazz = r.nextString();
									List<String> clazzes = compilations
											.get(pakkage);
									if (clazzes == null) {
										clazzes = new ArrayList<String>();
										compilations.put(pakkage, clazzes);
									}
									clazzes.add(clazz);
								}
							}).call(uuid);
					return d;
				}
				return null;
			}
		});
	}

	public Result<FindingOverview> changeImportance(final long findingId,
			final ImportanceView view) {
		ConnectionFactory.withUserTransaction(new NullUserTransaction() {
			@Override
			public void doPerform(final Connection conn, final Server server,
					final User user) throws Exception {
				ServerFindingManager.getInstance(conn).setImportance(findingId,
						user, server.nextRevision(),
						Importance.values()[view.ordinal()]);
			}
		});
		return getFinding(Long.toString(findingId));
	}

	public Result<FindingOverview> comment(final long findingId,
			final String comment) {
		ConnectionFactory.withUserTransaction(new NullUserTransaction() {
			@Override
			public void doPerform(final Connection conn, final Server server,
					final User user) throws Exception {
				ServerFindingManager.getInstance(conn).comment(findingId, user,
						server.nextRevision(), comment);
			}
		});
		return getFinding(Long.toString(findingId));
	}
}
