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

	public EmailInfo(String host, String port, String user, String pass,
			String fromEmail, String adminEmail) {
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

	public void setAdminEmail(String adminEmail) {
		this.adminEmail = adminEmail;
	}

	public String getServerEmail() {
		return fromEmail;
	}

	public void setFromEmail(String fromEmail) {
		this.fromEmail = fromEmail;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getPass() {
		return pass;
	}

	public void setPass(String pass) {
		this.pass = pass;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

}
