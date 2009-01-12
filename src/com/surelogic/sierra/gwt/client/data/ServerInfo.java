package com.surelogic.sierra.gwt.client.data;

import java.io.Serializable;

public class ServerInfo implements Serializable {
	private static final long serialVersionUID = -934601985763096364L;

	private String hostName;
	private String siteName;
	private String productVersion;
	private String currentVersion;
	private String availableVersion;
	private EmailInfo email;

	public String getHostName() {
		return hostName;
	}

	public void setHostName(final String hostName) {
		this.hostName = hostName;
	}

	public String getSiteName() {
		return siteName;
	}

	public void setSiteName(final String siteName) {
		this.siteName = siteName;
	}

	public String getProductVersion() {
		return productVersion;
	}

	public void setProductVersion(final String productVersion) {
		this.productVersion = productVersion;
	}

	public String getCurrentVersion() {
		return currentVersion;
	}

	public void setCurrentVersion(final String currentVersion) {
		this.currentVersion = currentVersion;
	}

	public String getAvailableVersion() {
		return availableVersion;
	}

	public void setAvailableVersion(final String availableVersion) {
		this.availableVersion = availableVersion;
	}

	public EmailInfo getEmail() {
		return email;
	}

	public void setEmail(final EmailInfo email) {
		this.email = email;
	}

	/**
	 * This ServerInfo should be displayed on the client before information is
	 * available from the server.
	 * 
	 * @return
	 */
	public static ServerInfo getDefault() {
		final ServerInfo info = new ServerInfo();
		final String waiting = "Waiting...";
		info.setAvailableVersion(waiting);
		info.setCurrentVersion(waiting);
		info.setCurrentVersion(waiting);
		info.setEmail(new EmailInfo(waiting, waiting, waiting, waiting,
				waiting, waiting));
		return info;
	}

}
