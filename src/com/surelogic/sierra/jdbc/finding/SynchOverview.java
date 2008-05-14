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
	private final int numCommitted;
	private final int numReceived;

	private SynchOverview(ResultSet set) throws SQLException {
		project = set.getString(1);
		time = set.getTimestamp(2);
		numCommitted = set.getInt(3);
		numReceived = set.getInt(4);
	}

	public String getProject() {
		return project;
	}

	public Date getTime() {
		return time;
	}

	public int getNumCommitted() {
		return numCommitted;
	}

	public int getNumReceived() {
		return numReceived;
	}

	public boolean isEmpty() {
		return (numCommitted == 0) && (numReceived == 0);
	}

	public static List<SynchOverview> listOverviews(Connection conn)
			throws SQLException {
		final Statement oSt = conn.createStatement();
		final List<SynchOverview> overview = new ArrayList<SynchOverview>();
		try {
			final ResultSet set = oSt
					.executeQuery("SELECT P.NAME, S.DATE_TIME, S.COMMIT_COUNT, S.UPDATE_COUNT FROM SYNCH S, PROJECT P WHERE P.ID = S.PROJECT_ID ORDER BY 2,1");
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
