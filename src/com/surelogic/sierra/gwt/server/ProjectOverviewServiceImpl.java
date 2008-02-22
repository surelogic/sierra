package com.surelogic.sierra.gwt.server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.surelogic.sierra.gwt.SierraServiceServlet;
import com.surelogic.sierra.gwt.client.data.ProjectOverview;
import com.surelogic.sierra.gwt.client.service.ProjectOverviewService;
import com.surelogic.sierra.jdbc.server.ConnectionFactory;
import com.surelogic.sierra.jdbc.server.Server;
import com.surelogic.sierra.jdbc.server.UserTransaction;
import com.surelogic.sierra.jdbc.user.User;
import com.surelogic.sierra.tool.message.Importance;

public class ProjectOverviewServiceImpl extends SierraServiceServlet implements
		ProjectOverviewService {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1399491183980140077L;

	public List<ProjectOverview> getProjectOverviews() {
		return ConnectionFactory
				.withUserReadOnly(new UserTransaction<List<ProjectOverview>>() {

					public List<ProjectOverview> perform(Connection conn,
							Server server, User user) throws Exception {
						final List<ProjectOverview> overview = new ArrayList<ProjectOverview>();
						PreparedStatement st = conn
								.prepareStatement("SELECT P.NAME, COUNT(DISTINCT F.ID), COUNT(A.ID), F.IMPORTANCE "
										+ "FROM PROJECT P, FINDING F, SIERRA_AUDIT A "
										+ "WHERE F.PROJECT_ID = P.ID AND "
										+ " A.FINDING_ID = F.ID AND "
										+ " A.REVISION >= (SELECT MIN(REVISION) FROM REVISION WHERE ? < DATE_TIME) "
										+ "GROUP BY P.NAME,F.IMPORTANCE "
										+ "ORDER BY P.NAME");
						final Calendar c = Calendar.getInstance();
						c.add(Calendar.DAY_OF_YEAR, -30);
						st.setTimestamp(1, new Timestamp(c.getTimeInMillis()));
						final ResultSet set = st.executeQuery();
						try {
							ProjectOverview po = new ProjectOverview();
							int findingCount = 0;
							int auditCount = 0;
							while (set.next()) {
								int idx = 1;
								final String name = set.getString(idx++);
								if (!name.equals(po.getName())) {
									po.setComments(auditCount);
									po.setFindings(findingCount);
									po = new ProjectOverview();
									overview.add(po);
									findingCount = 0;
									auditCount = 0;
									po.setName(name);
								}
								int importanceCount = set.getInt(idx++);
								findingCount += importanceCount;
								int auditImpCount = set.getInt(idx++);
								auditCount += auditImpCount;
								switch (Importance.values()[set.getInt(idx++)]) {
								case CRITICAL:
									po.setCritical(importanceCount);
									break;
								case HIGH:
									po.setHigh(importanceCount);
									break;
								case IRRELEVANT:
									po.setIrrelevant(importanceCount);
									break;
								case LOW:
									po.setLow(importanceCount);
									break;
								case MEDIUM:
									po.setMedium(importanceCount);
									break;
								default:
									// Value not understood. Do nothing
								}
							}
							po.setFindings(findingCount);
							po.setComments(auditCount);
						} finally {
							set.close();
						}
						final PreparedStatement lastSynchSt = conn
								.prepareStatement("SELECT SU.USER_NAME, R.DATE_TIME "
										+ "FROM COMMIT_AUDITS CA, SIERRA_USER SU, REVISION R "
										+ "WHERE CA.PROJECT_ID = (SELECT ID FROM PROJECT WHERE NAME = ?) AND "
										+ "CA.REVISION = (SELECT MAX(REVISION) FROM COMMIT_AUDITS WHERE PROJECT_ID = (SELECT ID FROM PROJECT WHERE NAME = ?)) "
										+ "AND SU.ID = CA.USER_ID AND R.REVISION = CA.REVISION");
						for (ProjectOverview po : overview) {
							lastSynchSt.setString(1, po.getName());
							lastSynchSt.setString(2, po.getName());
							final ResultSet lastSynchSet = lastSynchSt
									.executeQuery();
							try {
								if (lastSynchSet.next()) {
									po.setLastSynchUser(lastSynchSet
											.getString(1));
									po.setLastSynchDate(new Date(lastSynchSet
											.getTimestamp(2).getTime()));
								}
							} finally {
								lastSynchSet.close();
							}
						}
						return overview;
					}
				});
	}
}
