package com.surelogic.sierra.jdbc.finding;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.surelogic.sierra.jdbc.JDBCUtils;

public class SynchDetail {

	private final Date time;
	private final String project;
	private final List<AuditDetail> audits;

	private SynchDetail(Connection conn, String project, Date time)
			throws SQLException {
		PreparedStatement synchSt = conn
				.prepareStatement("SELECT PRIOR_REVISION FROM SYNCH WHERE PROJECT_ID = (SELECT ID FROM PROJECT WHERE NAME = ?) AND DATE_TIME = ?");
		PreparedStatement upperSynchRange = conn
				.prepareStatement("SELECT MIN(PRIOR_REVISION) FROM SYNCH WHERE PROJECT_ID = (SELECT ID FROM PROJECT WHERE NAME = ?) AND PRIOR_REVISION > ?");
		try {
			synchSt.setString(1, project);
			synchSt.setTimestamp(2, new Timestamp(time.getTime()));
			ResultSet set = synchSt.executeQuery();
			if (set.next()) {
				this.project = project;
				this.time = time;
				this.audits = new ArrayList<AuditDetail>();
				long priorRevision = set.getLong(1);
				upperSynchRange.setString(1, project);
				upperSynchRange.setLong(2, priorRevision);
				ResultSet upperRangeSet = upperSynchRange.executeQuery();
				try {
					upperRangeSet.next();
					long upperRevision = upperRangeSet.getLong(1);
					String auditStmt = "SELECT A.FINDING_ID,U.USER_NAME,A.EVENT,A.VALUE,A.DATE_TIME"
							+ "   FROM PROJECT P, FINDING F, SIERRA_AUDIT A, SIERRA_USER U"
							+ "   WHERE "
							+ "      P.NAME = '"
							+ JDBCUtils.escapeString(project)
							+ "' AND"
							+ "      F.PROJECT_ID = P.ID AND"
							+ "      A.FINDING_ID = F.ID AND"
							+ "      A.REVISION > "
							+ priorRevision
							+ (upperRangeSet.wasNull() ? ""
									: (" AND A.REVISION <= " + upperRevision));
					Statement auditSt = conn.createStatement();
					try {
						ResultSet auditSet = auditSt.executeQuery(auditStmt);
						while (auditSet.next()) {
							audits.add(new AuditDetail(auditSet));
						}
					} finally {
						auditSt.close();
					}
				} finally {
					upperSynchRange.close();
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
