package com.surelogic.sierra.gwt.client.data;

import java.io.Serializable;

public final class UserAccount implements Serializable {
	private static final long serialVersionUID = 6808103119746833312L;

	private long id;
	private String userName;
	private String serverName;
	private boolean isAdministrator;
	private boolean isActive;

	public UserAccount() {
		super();
	}

	public UserAccount(final long id, final String user, final String server,
			final boolean isAdministrator, final boolean isActive) {
		super();
		this.id = id;
		this.userName = user;
		this.isAdministrator = isAdministrator;
		this.isActive = isActive;
		this.serverName = server;
	}

	public long getId() {
		return id;
	}

	public void setId(final long id) {
		this.id = id;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(final String userName) {
		this.userName = userName;
	}

	public boolean isAdministrator() {
		return isAdministrator;
	}

	public void setAdministrator(final boolean isAdministrator) {
		this.isAdministrator = isAdministrator;
	}

	public boolean isActive() {
		return isActive;
	}

	public void setActive(final boolean isActive) {
		this.isActive = isActive;
	}

	public String getServerName() {
		return serverName;
	}

	public void setServerName(final String serverName) {
		this.serverName = serverName;
	}

	public boolean showServerConfig() {
		return isAdministrator
				&& (serverName == null || "* no name *".equals(serverName));
	}
}
