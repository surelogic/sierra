package com.surelogic.sierra.jdbc.finding;

import java.util.Date;

public class CommentDetail {

	private final Date time;
	private final String comment;
	private final String user;

	CommentDetail(String user, String comment, Date time) {
		this.time = time;
		this.comment = comment;
		this.user = user;
	}

	public Date getTime() {
		return time;
	}

	public String getComment() {
		return comment;
	}

	public String getUser() {
		return user;
	}

}
