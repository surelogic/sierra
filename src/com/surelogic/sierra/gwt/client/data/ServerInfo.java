package com.surelogic.sierra.gwt.client.data;

import java.io.Serializable;

public class ServerInfo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -934601985763096364L;
	private String currentVersion;
	private String availableVersion;
	private EmailInfo email;

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

	public EmailInfo getEmail() {
		return email;
	}

	public void setEmail(EmailInfo email) {
		this.email = email;
	}

	/**
	 * This ServerInfo should be displayed on the client before information is
	 * available from the server.
	 * 
	 * @return
	 */
	public static ServerInfo getDefault() {
		ServerInfo info = new ServerInfo();
		final String waiting = "Waiting...";
		info.setAvailableVersion(waiting);
		info.setCurrentVersion(waiting);
		info
				.setEmail(new EmailInfo(waiting, waiting, waiting, waiting,
						waiting));
		return info;
	}

}
