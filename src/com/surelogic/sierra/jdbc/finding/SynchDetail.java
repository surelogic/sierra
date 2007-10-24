package com.surelogic.sierra.jdbc.finding;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

public class SynchDetail {

	private final Date date;
	private final List<AuditDetail> audits;
	private final List<AuditDetail> commits;
	private final String project;

	private SynchDetail(Connection conn, String project, Date time)
			throws SQLException {
		PreparedStatement synchSt = conn
				.prepareStatement("SELECT COMMIT_REVISION,PRIOR_REVISION FROM SYNCH_DETAIL WHERE PROJECT_ID = (SELECT ID FROM PROJECT WHERE NAME = ?) AND DATE_TIME = ?");
		try {
			synchSt.setString(1, project);
			synchSt.setTimestamp(2, new Timestamp(time.getTime()));
			ResultSet set = synchSt.executeQuery();

		} finally {
			synchSt.close();
		}
		audits = null;
		commits = null;
		date = null;
		this.project = null;
	}

	public static SynchDetail getDetail(Connection conn, String project,
			Date time) {
		return null;
	}

	public Date getDate() {
		return date;
	}

	public List<AuditDetail> getAudits() {
		return audits;
	}

	public List<AuditDetail> getCommits() {
		return commits;
	}

	public String getProject() {
		return project;
	}

}
