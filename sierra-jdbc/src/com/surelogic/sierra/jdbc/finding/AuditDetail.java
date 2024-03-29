package com.surelogic.sierra.jdbc.finding;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.surelogic.common.jdbc.QB;
import com.surelogic.sierra.tool.message.AuditEvent;

public class AuditDetail {

	private final long findingId;
	private final Date time;
	private final String text;
	private final String user;

	AuditDetail(ResultSet set) throws SQLException {
		int idx = 1;
		findingId = set.getLong(idx++);
		user = set.getString(idx++);
		switch (AuditEvent.valueOf(set.getString(idx++))) {
		case COMMENT:
			text = set.getString(idx++);
			break;
		case IMPORTANCE:
			String importance = set.getString(idx++);
			importance = importance.substring(0, 1)
					+ importance.toLowerCase().substring(1);
			text = "Importance changed to " + importance + ".";
			break;
		case READ:
			set.getString(idx++);
			text = "Finding examined.";
			break;
		case SUMMARY:
			text = "Summary changed to \"" + set.getString(idx++) + "\"";
			break;
		default:
			text = "Unknown type of audit.";
			break;
		}
		time = set.getTimestamp(idx++);
	}

	public long getFindingId() {
		return findingId;
	}

	public Date getTime() {
		return time;
	}

	public String getText() {
		return text;
	}

	public String getUser() {
		return user;
	}

	public static List<AuditDetail> getDetails(Connection conn, long findingId)
			throws SQLException {
		List<AuditDetail> audits = new ArrayList<AuditDetail>();
		Statement st = conn.createStatement();
		try {
			ResultSet set = st.executeQuery(QB.get(16, findingId));
			try {
				while (set.next()) {
					audits.add(new AuditDetail(set));
				}
			} finally {
				set.close();
			}
		} finally {
			st.close();
		}
		return audits;
	}

}
