package com.surelogic.sierra.gwt.server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.surelogic.common.jdbc.QB;
import com.surelogic.sierra.gwt.client.data.AuditOverview;
import com.surelogic.sierra.gwt.client.data.FindingOverview;
import com.surelogic.sierra.gwt.client.data.Result;
import com.surelogic.sierra.gwt.client.service.FindingService;
import com.surelogic.sierra.jdbc.server.ConnectionFactory;
import com.surelogic.sierra.jdbc.server.Server;
import com.surelogic.sierra.jdbc.server.ServerTransaction;
import com.surelogic.sierra.tool.message.Importance;

public class FindingServiceImpl extends RemoteServiceServlet implements
		FindingService {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5522046767503450943L;

	public Result getFinding(final String key) {
		if (key == null || "".equals(key)) {
			return Result.failure("No key specified");
		}
		return ConnectionFactory.withReadOnly(new ServerTransaction<Result>() {

			public Result perform(Connection conn, Server server)
					throws Exception {
				PreparedStatement st;
				try {
					final long id = Long.parseLong(key);
					st = conn.prepareStatement(QB.get("portal.finding.byId"));
					st.setLong(1, id);
				} catch (NumberFormatException e) {
					// Try for a uuid
					try {
						UUID.fromString(key);
						st = conn.prepareStatement(QB
								.get("portal.finding.byUuid"));
					} catch (IllegalArgumentException ex) {
						return Result.failure("Unparseable key: " + key);
					}
				}
				// F.ID,F.IMPORTANCE,F.SUMMARY,FT.NAME,FC.NAME,P.NAME,LM.PACKAGE_NAME,LM.CLASS_NAME
				final ResultSet set = st.executeQuery();
				try {
					if (set.next()) {
						int idx = 1;
						final FindingOverview f = new FindingOverview();
						final long id = set.getLong(idx++);
						f.setImportance(Importance.values()[set.getInt(idx++)]
								.toStringSentenceCase());
						f.setSummary(set.getString(idx++));
						f.setFindingType(set.getString(idx++));
						f.setCategory(set.getString(idx++));
						f.setProject(set.getString(idx++));
						f.setPackageName(set.getString(idx++));
						f.setClassName(set.getString(idx++));
						PreparedStatement auditSt = conn.prepareStatement(QB
								.get("portal.finding.auditsById"));
						auditSt.setLong(1, id);
						final ResultSet auditSet = auditSt.executeQuery();
						final List<AuditOverview> audits = new ArrayList<AuditOverview>();
						try {
							// EVENT, DATE_TIME, VALUE, USER_NAME
							while (auditSet.next()) {
								AuditOverview audit = new AuditOverview();
								int auditIdx = 1;
								audit.setText(auditSet.getString(auditIdx++));
								audit.setTime(auditSet.getTimestamp(auditIdx++)
										.toString());
								audit.setUser(auditSet.getString(auditIdx++));
								audits.add(audit);
							}
						} finally {
							auditSet.close();
						}
						return Result.success(f);

					} else {
						return Result.failure("No finding with id " + key
								+ " exists.");
					}
				} finally {
					set.close();
				}
			}
		});
	}

}
