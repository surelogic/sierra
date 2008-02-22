package com.surelogic.sierra.gwt.client.data;

import java.io.Serializable;

public final class UserAccount implements Serializable {
	private static final long serialVersionUID = 6808103119746833312L;

	private long id;
	private String userName;
	private boolean isAdministrator;

	public UserAccount() {
		super();
	}

	public UserAccount(long id, String user, boolean isAdministrator) {
		super();
		this.id = id;
		this.userName = user;
		this.isAdministrator = isAdministrator;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
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
