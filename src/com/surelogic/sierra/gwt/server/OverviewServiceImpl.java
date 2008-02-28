package com.surelogic.sierra.gwt.server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.surelogic.sierra.gwt.SierraServiceServlet;
import com.surelogic.sierra.gwt.client.data.ProjectOverview;
import com.surelogic.sierra.gwt.client.data.UserOverview;
import com.surelogic.sierra.gwt.client.service.OverviewService;
import com.surelogic.sierra.jdbc.server.ConnectionFactory;
import com.surelogic.sierra.jdbc.server.Server;
import com.surelogic.sierra.jdbc.server.UserTransaction;
import com.surelogic.sierra.jdbc.user.User;
import com.surelogic.sierra.tool.message.Importance;

public class OverviewServiceImpl extends SierraServiceServlet implements
		OverviewService {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1399491183980140077L;

	public List<UserOverview> getUserOverviews() {
		return ConnectionFactory
				.withUserReadOnly(new UserTransaction<List<UserOverview>>() {

					public List<UserOverview> perform(Connection conn,
							Server server, User user) throws Exception {
						PreparedStatement auditSt = conn
								.prepareStatement("SELECT U.USER_NAME, COUNT(DISTINCT A.ID), MAX(R.DATE_TIME)  "
										+ "FROM SIERRA_USER U, SIERRA_AUDIT A, REVISION R "
										+ "WHERE A.USER_ID = U.ID AND R.REVISION = A.REVISION AND "
										+ "A.REVISION >= (SELECT MIN(REVISION) FROM REVISION WHERE ? < DATE_TIME) "
										+ "GROUP BY U.USER_NAME "
										+ "ORDER BY U.USER_NAME ");
						PreparedStatement findingSt = conn
								.prepareStatement("SELECT U.USER_NAME, COUNT(DISTINCT F.ID) "
										+ "FROM SIERRA_USER U, SIERRA_AUDIT A, FINDING F "
										+ "WHERE A.USER_ID = U.ID AND F.ID = A.FINDING_ID AND "
										+ "A.REVISION >= (SELECT MIN(REVISION) FROM REVISION WHERE ? < DATE_TIME) "
										+ "GROUP BY U.USER_NAME "
										+ "ORDER BY U.USER_NAME ");
						final List<UserOverview> overviews = new ArrayList<UserOverview>();
						Timestamp time = thirtyDaysAgo();
						auditSt.setTimestamp(1, time);
						findingSt.setTimestamp(1, time);
						final ResultSet auditSet = auditSt.executeQuery();
						try {
							final ResultSet findingSet = findingSt
									.executeQuery();
							try {
								while (auditSet.next()) {
									findingSet.next();
									final UserOverview o = new UserOverview();
									o.setUserName(auditSet.getString(1));
									o.setAudits(auditSet.getInt(2));
									o.setLastSynch(formattedDate(auditSet
											.getTimestamp(3)));
									o.setFindings(findingSet.getInt(2));
									overviews.add(o);
								}
							} finally {
								findingSet.close();
							}
						} finally {
							auditSet.close();
						}
						return overviews;
					}
				});
	}

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

						st.setTimestamp(1, thirtyDaysAgo());
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
									po
											.setLastSynchDate(formattedDate(lastSynchSet
													.getTimestamp(2)));
								}
							} finally {
								lastSynchSet.close();
							}
						}
						return overview;
					}
				});
	}

	private static String formattedDate(Date date) {
		DateFormat format = new SimpleDateFormat();
		return format.format(date);
	}

	private static Timestamp thirtyDaysAgo() {
		final Calendar c = Calendar.getInstance();
		c.add(Calendar.DAY_OF_YEAR, -30);
		return new Timestamp(c.getTimeInMillis());
	}
}
