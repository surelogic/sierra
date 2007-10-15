package com.surelogic.sierra.jdbc.finding;

import java.util.Date;

public class AuditDetail {

	private final Date time;
	private final String text;
	private final String user;

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
