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
	private final List<SynchedFindingDetail> findings;
	private SynchDetail(Connection conn, String project, Date time)
			throws SQLException {
		PreparedStatement synchSt = conn
				.prepareStatement("SELECT COMMIT_REVISION,PRIOR_REVISION FROM SYNCH_DETAIL WHERE PROJECT_ID = (SELECT ID FROM PROJECT WHERE NAME = ?) AND DATE_TIME = ?");
		try {
			synchSt.setString(1, project);
			synchSt.setTimestamp(2, new Timestamp(time.getTime()));
			ResultSet set = synchSt.executeQuery();
			if (set.next()) {
				this.project = project;
				this.time = time;
				this.findings = new ArrayList<SynchedFindingDetail>();
				List<AuditDetail> audits = new ArrayList<AuditDetail>();
				List<AuditDetail> commits = new ArrayList<AuditDetail>();
				long commitRevision = set.getLong(1);
				long priorRevision = set.getLong(2);

				PreparedStatement auditSt = conn
						.prepareStatement("SELECT U.USER_NAME,A.EVENT,A.VALUE,A.DATE_TIME FROM SIERRA_AUDIT A, SIERRA_USER U WHERE A.REVISION = ? AND U.ID = A.USER_ID");
				try {
					auditSt.setLong(1, commitRevision);
					ResultSet auditSet = auditSt.executeQuery();
					while (auditSet.next()) {
						commits.add(new AuditDetail(set));
					}
					auditSt.setLong(1, priorRevision);
					auditSet = auditSt.executeQuery();
					while (auditSet.next()) {
						audits.add(new AuditDetail(set));
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

	public static SynchDetail getDetail(Connection conn, String project,
			Date time) {
		return null;
	}

	public Date getTime() {
		return time;
	}

	public String getProject() {
		return project;
	}

}
