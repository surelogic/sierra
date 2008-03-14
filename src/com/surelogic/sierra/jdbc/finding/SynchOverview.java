package com.surelogic.sierra.jdbc.finding;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public final class SynchOverview {

	private final String project;
	private final Date time;
	private final int numAudits;
	
	private SynchOverview(ResultSet set) throws SQLException {
		this.project = set.getString(1);
		this.time = set.getTimestamp(2);
		this.numAudits = 1; // FIX set.getInt(3);
	}

	public String getProject() {
		return project;
	}

	public Date getTime() {
		return time;
	}
	
	public int getNumAudits() {
		return numAudits;
	}

	public boolean isEmpty() {
		return numAudits == 0;
	}
	
	public static List<SynchOverview> listOverviews(Connection conn)
			throws SQLException {
		Statement oSt = conn.createStatement();
		List<SynchOverview> overview = new ArrayList<SynchOverview>();
		try {
		  ResultSet set = oSt
		  .executeQuery("SELECT P.NAME, S.DATE_TIME FROM SYNCH S, PROJECT P WHERE P.ID = S.PROJECT_ID ORDER BY 2,1");
		  // FIX
		  try {
		    while (set.next()) {
		      overview.add(new SynchOverview(set));
		    }
		  } finally {
		    set.close();
		  }
		} finally {
			oSt.close();
		}
		return overview;
	}
}
