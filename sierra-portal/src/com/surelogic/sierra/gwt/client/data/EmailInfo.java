package com.surelogic.sierra.gwt.client.data;

import java.io.Serializable;

public class EmailInfo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1837341576790740396L;

	private String adminEmail;
	private String fromEmail;
	private String host;
	private String pass;
	private String user;
	private String port;

	public EmailInfo() {
	}

	public EmailInfo(final String host, final String port, final String user,
			final String pass, final String fromEmail, final String adminEmail) {
		this.host = host;
		this.port = port;
		this.user = user;
		this.pass = pass;
		this.fromEmail = fromEmail;
		this.adminEmail = adminEmail;
	}

	public String getAdminEmail() {
		return adminEmail;
	}

	public void setAdminEmail(final String adminEmail) {
		this.adminEmail = adminEmail;
	}

	public String getServerEmail() {
		return fromEmail;
	}

	public void setFromEmail(final String fromEmail) {
		this.fromEmail = fromEmail;
	}

	public String getHost() {
		return host;
	}

	public void setHost(final String host) {
		this.host = host;
	}

	public String getPort() {
		return port;
	}

	public void setPort(final String port) {
		this.port = port;
	}

	public String getPass() {
		return pass;
	}

	public void setPass(final String pass) {
		this.pass = pass;
	}

	public String getUser() {
		return user;
	}

	public void setUser(final String user) {
		this.user = user;
	}

	public boolean isValid() {
		return port != null && host != null && fromEmail != null
				&& adminEmail != null && !port.isEmpty() && !host.isEmpty()
				&& !adminEmail.isEmpty() && !fromEmail.isEmpty();
	}

}
