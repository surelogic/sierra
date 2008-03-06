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

	public List<ProjectOverview> getProjectOverviews() throws SQLException {
		final List<ProjectOverview> overview = new ArrayList<ProjectOverview>();
		PreparedStatement auditSt = conn
				.prepareStatement("SELECT P.NAME, COUNT(DISTINCT A.FINDING_ID), COUNT(A.ID)  "
						+ "FROM PROJECT P LEFT OUTER JOIN FINDING F ON F.PROJECT_ID = P.ID  "
						+ "LEFT OUTER JOIN SIERRA_AUDIT A ON A.FINDING_ID = F.ID  "
						+ "WHERE A.REVISION >= (SELECT MIN(REVISION) FROM REVISION WHERE ? < DATE_TIME) OR A.REVISION IS NULL "
						+ "GROUP BY P.NAME ORDER BY P.NAME");
		auditSt.setTimestamp(1, thirtyDaysAgo());
		final ResultSet auditSet = auditSt.executeQuery();
		try {
			Statement scanSt = conn.createStatement();
			try {
				final ResultSet scanSet = scanSt
						.executeQuery("SELECT P.PROJECT, P.TIME, COUNT (F.ID), F.IMPORTANCE  "
								+ "FROM LATEST_SCANS P LEFT OUTER JOIN SCAN_OVERVIEW SO ON SO.SCAN_ID = P.SCAN_ID  "
								+ "LEFT OUTER JOIN FINDING F ON F.ID = SO.FINDING_ID "
								+ "WHERE P.QUALIFIER = '__ALL_SCANS__'   "
								+ "GROUP BY P.PROJECT,P.TIME,F.IMPORTANCE ORDER BY P.PROJECT");
				ProjectOverview po = new ProjectOverview();
				int findingCount = 0;
				while (scanSet.next()) {
					int idx = 1;
					final String name = scanSet.getString(idx++);
					final Date time = scanSet.getTimestamp(idx++);
					if (!name.equals(po.getName())) {
						po.setTotalFindings(findingCount);
						findingCount = 0;
						po = new ProjectOverview();
						po.setName(name);
						po.setLastScanDate(formattedDate(time));
						auditSet.next();
						auditSet.getString(1);
						po.setCommentedFindings(auditSet.getInt(2));
						po.setComments(auditSet.getInt(3));
						overview.add(po);
					}
					final int importanceCount = scanSet.getInt(idx++);
					final int importance = scanSet.getInt(idx++);
					if (!scanSet.wasNull()) {
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
				}
				//Final finding count
				po.setTotalFindings(findingCount);
			} finally {
				scanSt.close();
			}
		} finally {
			auditSet.close();
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
