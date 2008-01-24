package com.surelogic.sierra.gwt.client;

import java.io.Serializable;

public class ServerInfo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -934601985763096364L;
	private String currentVersion;
	private String availableVersion;
	private String email;

	public String getCurrentVersion() {
		return currentVersion;
	}

	public void setCurrentVersion(String currentVersion) {
		this.currentVersion = currentVersion;
	}

	public String getAvailableVersion() {
		return availableVersion;
	}

	public void setAvailableVersion(String availableVersion) {
		this.availableVersion = availableVersion;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	/**
	 * This ServerInfo should be displayed on the client before information is
	 * available from the server.
	 * 
	 * @return
	 */
	static ServerInfo getDefault() {
		ServerInfo info = new ServerInfo();
		final String waiting = "Waiting...";
		info.setAvailableVersion(waiting);
		info.setCurrentVersion(waiting);
		info.setEmail(waiting);
		return info;
	}

}
