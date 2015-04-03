package com.surelogic.sierra.gwt.client.data;

import java.io.Serializable;

/**
 * Overview of an audit on a finding.
 * 
 * @author nathan
 * 
 */
public class AuditOverview implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8257640608776172447L;

	private String time;
	private String text;
	private String user;

	public AuditOverview() {
		// Do nothing
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

}
