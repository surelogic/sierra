package com.surelogic.sierra.portal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.surelogic.sierra.gwt.client.data.ProjectOverview;
import com.surelogic.sierra.gwt.client.data.UserOverview;
import com.surelogic.sierra.tool.message.Importance;

public final class PortalOverview {

	private final Connection conn;

	private PortalOverview(Connection conn) {
		this.conn = conn;
	}

	public List<UserOverview> getUserOverviews() throws SQLException {

		PreparedStatement auditSt = conn
				.prepareStatement("SELECT U.USER_NAME, MAX(U.IS_ACTIVE), COUNT(DISTINCT A.ID), MAX(R.DATE_TIME) "
						+ "FROM SIERRA_USER U "
						+ "LEFT OUTER JOIN SIERRA_AUDIT A ON A.USER_ID = U.ID "
						+ "LEFT OUTER JOIN REVISION R ON R.REVISION = A.REVISION "
						+ "WHERE A.REVISION >= (SELECT MIN(REVISION) FROM REVISION WHERE ? < DATE_TIME) OR A.REVISION IS NULL "
						+ "GROUP BY U.USER_NAME ORDER BY U.USER_NAME");
		PreparedStatement findingSt = conn
				.prepareStatement("SELECT U.USER_NAME, COUNT(DISTINCT F.ID) "
						+ "FROM SIERRA_USER U "
						+ "LEFT OUTER JOIN SIERRA_AUDIT A ON A.USER_ID = U.ID "
						+ "LEFT OUTER JOIN FINDING F ON F.ID = A.FINDING_ID "
						+ "WHERE A.REVISION >= (SELECT MIN(REVISION) FROM REVISION WHERE ? < DATE_TIME) OR A.REVISION IS NULL "
						+ "GROUP BY U.USER_NAME ORDER BY U.USER_NAME");
		final List<UserOverview> overviews = new ArrayList<UserOverview>();
		Timestamp time = thirtyDaysAgo();
		auditSt.setTimestamp(1, time);
		findingSt.setTimestamp(1, time);
		final ResultSet auditSet = auditSt.executeQuery();
		try {
			final ResultSet findingSet = findingSt.executeQuery();
			try {
				while (auditSet.next()) {
					findingSet.next();
					final UserOverview o = new UserOverview();
					o.setUserName(auditSet.getString(1));
					o.setActive("Y".equals(auditSet.getString(2)));
					o.setAudits(auditSet.getInt(3));
					o.setLastSynch(formattedDate(auditSet.getTimestamp(4)));
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

	public List<UserOverview> getEnabledUserOverviews() throws SQLException {
		final List<UserOverview> result = getUserOverviews();
		/*
		 * Remove disabled users.
		 */
		for (Iterator<UserOverview> i = result.iterator(); i.hasNext();) {
			UserOverview userOverview = i.next();
			if (!userOverview.isActive()) {
				i.remove();
			}
		}
		return result;
	}

	public List<ProjectOverview> getProjectOverviews() throws SQLException {
		final List<ProjectOverview> overview = new ArrayList<ProjectOverview>();
		PreparedStatement auditSt = conn
				.prepareStatement("SELECT P.NAME, COUNT(DISTINCT A.FINDING_ID), COUNT(A.ID)  "
						+ "FROM PROJECT P LEFT OUTER JOIN FINDING F ON F.PROJECT_ID = P.ID  "
						+ "LEFT OUTER JOIN SIERRA_AUDIT A ON A.FINDING_ID = F.ID  "
						+ "WHERE A.REVISION >= (SELECT MIN(REVISION) FROM REVISION WHERE ? < DATE_TIME) OR A.REVISION IS NULL "
						+ "GROUP BY P.NAME ORDER BY P.NAME");
		PreparedStatement scanSt = conn
				.prepareStatement("SELECT COUNT(F.ID), F.IMPORTANCE "
						+ "FROM SCAN_OVERVIEW SO, FINDING F "
						+ "WHERE F.ID = SO.FINDING_ID AND SO.SCAN_ID = ? "
						+ "GROUP BY F.IMPORTANCE");
		final PreparedStatement lastSynchSt = conn
				.prepareStatement("SELECT SU.USER_NAME, R.DATE_TIME "
						+ "FROM COMMIT_AUDITS CA, SIERRA_USER SU, REVISION R "
						+ "WHERE CA.PROJECT_ID = (SELECT ID FROM PROJECT WHERE NAME = ?) AND "
						+ "CA.REVISION = (SELECT MAX(REVISION) FROM COMMIT_AUDITS WHERE PROJECT_ID = (SELECT ID FROM PROJECT WHERE NAME = ?)) "
						+ "AND SU.ID = CA.USER_ID AND R.REVISION = CA.REVISION");
		auditSt.setTimestamp(1, thirtyDaysAgo());
		final ResultSet auditSet = auditSt.executeQuery();
		try {
			final Statement projectSt = conn.createStatement();
			try {
				final ResultSet projectSet = projectSt
						.executeQuery("SELECT P.PROJECT, P.TIME, P.SCAN_ID FROM LATEST_SCANS P WHERE P.QUALIFIER = '__ALL_SCANS__'");
				while (projectSet.next()) {
					int projectIdx = 1;
					final ProjectOverview po = new ProjectOverview();
					final String name = projectSet.getString(projectIdx++);
					final Date time = projectSet.getTimestamp(projectIdx++);
					po.setName(name);
					po.setLastScanDate(formattedDate(time));
					auditSet.next();
					auditSet.getString(1);
					po.setCommentedFindings(auditSet.getInt(2));
					po.setComments(auditSet.getInt(3));
					scanSt.setLong(1, projectSet.getLong(projectIdx++));
					final ResultSet scanSet = scanSt.executeQuery();
					try {
						int findingCount = 0;
						while (scanSet.next()) {
							final int importanceCount = scanSet.getInt(1);
							final int importance = scanSet.getInt(2);
							findingCount += importanceCount;
							switch (Importance.values()[importance]) {
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
						po.setTotalFindings(findingCount);
					} finally {
						scanSet.close();
					}
					lastSynchSt.setString(1, name);
					lastSynchSt.setString(2, name);
					final ResultSet lastSynchSet = lastSynchSt.executeQuery();
					try {
						if (lastSynchSet.next()) {
							po.setLastSynchUser(lastSynchSet.getString(1));
							po.setLastSynchDate(formattedDate(lastSynchSet
									.getTimestamp(2)));
						} else {
							po.setLastSynchUser("");
							po.setLastSynchDate("");
						}
					} finally {
						lastSynchSet.close();
					}
					overview.add(po);
				}
			} finally {
				projectSt.close();
			}
		} finally {
			auditSet.close();
		}
		return overview;
	}

	public static PortalOverview getInstance(Connection conn) {
		return new PortalOverview(conn);
	}

	private static Timestamp thirtyDaysAgo() {
		final Calendar c = Calendar.getInstance();
		c.add(Calendar.DAY_OF_YEAR, -30);
		return new Timestamp(c.getTimeInMillis());
	}

	private static String formattedDate(Date date) {
		if (date == null) {
			return "";
		} else {
			DateFormat format = new SimpleDateFormat("MM/dd/yy HH:mm a");
			return format.format(date);
		}
	}

}
