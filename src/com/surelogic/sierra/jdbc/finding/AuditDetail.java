package com.surelogic.sierra.jdbc.finding;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import com.surelogic.sierra.tool.message.AuditEvent;

public class AuditDetail {

	private final Date time;
	private final String text;
	private final String user;

	AuditDetail(ResultSet set) throws SQLException {
		int idx = 1;
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

	AuditDetail(String user, String text, Date time) {
		this.time = time;
		this.text = text;
		this.user = user;
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

}
