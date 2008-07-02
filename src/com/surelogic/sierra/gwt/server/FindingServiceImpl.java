package com.surelogic.sierra.gwt.server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.surelogic.common.jdbc.QB;
import com.surelogic.common.jdbc.Query;
import com.surelogic.sierra.gwt.SierraServiceServlet;
import com.surelogic.sierra.gwt.client.data.ArtifactOverview;
import com.surelogic.sierra.gwt.client.data.AuditOverview;
import com.surelogic.sierra.gwt.client.data.FindingOverview;
import com.surelogic.sierra.gwt.client.data.Result;
import com.surelogic.sierra.gwt.client.data.Scan;
import com.surelogic.sierra.gwt.client.service.FindingService;
import com.surelogic.sierra.jdbc.scan.ScanInfo;
import com.surelogic.sierra.jdbc.scan.Scans;
import com.surelogic.sierra.jdbc.server.ConnectionFactory;
import com.surelogic.sierra.jdbc.server.Server;
import com.surelogic.sierra.jdbc.server.UserQuery;
import com.surelogic.sierra.jdbc.server.UserTransaction;
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
				.withUserReadOnly(new UserTransaction<Result<FindingOverview>>() {

					public Result<FindingOverview> perform(
							final Connection conn, final Server server,
							final User user) throws Exception {
						PreparedStatement st;
						try {
							final long id = Long.parseLong(key);
							st = conn.prepareStatement(QB
									.get("portal.finding.byId"));
							st.setLong(1, id);
						} catch (final NumberFormatException e) {
							// Try for a uuid
							try {
								UUID.fromString(key);
								st = conn.prepareStatement(QB
										.get("portal.finding.byUuid"));
							} catch (final IllegalArgumentException ex) {
								return Result.failure(
										"Unparseable key: " + key, null);
							}
						}
						//F.ID,F.IMPORTANCE,F.SUMMARY,FT.NAME,FC.NAME,P.NAME,LM.
						// PACKAGE_NAME,LM.CLASS_NAME
						final ResultSet set = st.executeQuery();
						try {
							if (set.next()) {
								int idx = 1;
								final FindingOverview f = new FindingOverview();
								final long id = set.getLong(idx++);
								f.setImportance(Importance.values()[set
										.getInt(idx++)].toStringSentenceCase());
								f.setSummary(set.getString(idx++));
								f.setFindingType(set.getString(idx++));
								f.setCategory(set.getString(idx++));
								f.setProject(set.getString(idx++));
								f.setPackageName(set.getString(idx++));
								f.setClassName(set.getString(idx++));
								final PreparedStatement auditSt = conn
										.prepareStatement(QB
												.get("portal.finding.auditsById"));
								auditSt.setLong(1, id);
								final ResultSet auditSet = auditSt
										.executeQuery();
								final List<AuditOverview> audits = new ArrayList<AuditOverview>();
								f.setAudits(audits);
								try {
									// EVENT, VALUE, DATE_TIME, USER_NAME
									while (auditSet.next()) {
										final AuditOverview audit = new AuditOverview();
										int auditIdx = 1;
										final String event = auditSet
												.getString(auditIdx++);
										final String value = auditSet
												.getString(auditIdx++);
										switch (AuditEvent.valueOf(event)) {
										case COMMENT:
											audit.setText(value);
											break;
										case IMPORTANCE:
											audit
													.setText("Importance changed to "
															+ Importance
																	.fromValue(
																			value)
																	.toStringSentenceCase()
															+ ".");
											break;
										case READ:
											audit.setText("Finding examined.");
											break;
										case SUMMARY:
											audit
													.setText("Changed summary to \""
															+ value + "\"");
											break;
										default:
											break;
										}
										audit.setTime(Dates.format(auditSet
												.getTimestamp(auditIdx++)));
										audit.setUser(auditSet
												.getString(auditIdx++));
										audits.add(audit);
									}
								} finally {
									auditSet.close();
								}
								final List<ArtifactOverview> artifacts = new ArrayList<ArtifactOverview>();
								f.setArtifacts(artifacts);
								return Result.success(f);
							} else {
								return Result.failure("No finding with id "
										+ key + " exists.", null);
							}
						} finally {
							set.close();
						}
					}
				});
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
					s.setScanTime(Dates.format(info.getScanTime()));
					s.setUser(info.getUser());
					s.setUuid(info.getUid());
					scans.add(s);
				}
				return scans;
			}
		});
	}

}
