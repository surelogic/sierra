package com.surelogic.sierra.jdbc.finding;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.List;

public class SynchDetail {

	private final Date date;
	private final List<AuditDetail> audits;
	private final List<AuditDetail> commits;
	private final String project;

	private SynchDetail(Connection conn, String project, Date time)
			throws SQLException {
		Statement synchSt = conn.createStatement();
		synchSt.executeQuery("SELECT ");

		audits = null;
		commits = null;
		date = null;
		this.project = null;
	}

}
