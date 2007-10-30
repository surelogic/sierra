package com.surelogic.sierra.jdbc.finding;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SynchDetail {

	private final Date time;
	private final String project;
	private final List<AuditDetail> audits;

	private SynchDetail(Connection conn, String project, Date time)
			throws SQLException {
		PreparedStatement synchSt = conn
				.prepareStatement("SELECT PRIOR_REVISION FROM SYNCH WHERE PROJECT_ID = (SELECT ID FROM PROJECT WHERE NAME = ?) AND DATE_TIME = ?");
		try {
			synchSt.setString(1, project);
			synchSt.setTimestamp(2, new Timestamp(time.getTime()));
			ResultSet set = synchSt.executeQuery();
			if (set.next()) {
				this.project = project;
				this.time = time;
				this.audits = new ArrayList<AuditDetail>();
				long priorRevision = set.getLong(1);
				PreparedStatement auditSt = conn
						.prepareStatement("SELECT A.FINDING_ID,U.USER_NAME,A.EVENT,A.VALUE,A.DATE_TIME"
								+ "   FROM SIERRA_AUDIT A, SIERRA_USER U"
								+ "   WHERE A.REVISION > ?"
								+ "   AND A.REVISION <= (SELECT MIN(PRIOR_REVISION) FROM SYNCH WHERE PROJECT_ID = (SELECT ID FROM PROJECT WHERE NAME = ?) AND PRIOR_REVISION > ?) AND U.ID = A.USER_ID");
				try {
					auditSt.setLong(1, priorRevision);
					auditSt.setString(2, project);
					auditSt.setLong(3, priorRevision);
					ResultSet auditSet = auditSt.executeQuery();
					while (auditSet.next()) {
						audits.add(new AuditDetail(auditSet));
					}
				} finally {
					auditSt.close();
				}
			} else {
				throw new IllegalArgumentException("No scan occurred at "
						+ time + " for project " + project);
			}
		} finally {
			synchSt.close();
		}

	}

	public static SynchDetail getSyncDetail(Connection conn, SynchOverview so)
			throws SQLException {
		return getSyncDetail(conn, so.getProject(), so.getTime());
	}

	public static SynchDetail getSyncDetail(Connection conn, String project,
			Date time) throws SQLException {
		return new SynchDetail(conn, project, time);
	}

	public Date getTime() {
		return time;
	}

	public String getProject() {
		return project;
	}

	public List<AuditDetail> getAudits() {
		return audits;
	}

}
