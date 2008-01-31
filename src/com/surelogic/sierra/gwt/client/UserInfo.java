package com.surelogic.sierra.gwt.client;

import java.io.Serializable;

public class UserInfo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6808103119746833312L;

	private String userName;

	private boolean isAdministrator;

	public UserInfo() {
		// Do nothing
	}

	public UserInfo(String user, boolean isAdministrator) {
		this.userName = user;
		this.isAdministrator = isAdministrator;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public boolean isAdministrator() {
		return isAdministrator;
	}

	public void setAdministrator(boolean isAdministrator) {
		this.isAdministrator = isAdministrator;
	}

}
