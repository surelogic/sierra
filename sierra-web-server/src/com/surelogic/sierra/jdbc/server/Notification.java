package com.surelogic.sierra.jdbc.server;

public class Notification {

	private String toEmail;
	private String fromEmail;
	private String host;
	private Integer port;
	private String user;
	private String password;

	public Notification() {
		super();
	}

	public Notification(final String host, final Integer port,
			final String user, final String pass, final String toEmail,
			final String fromEmail) {
		this.host = host;
		this.user = user;
		this.password = pass;
		this.toEmail = toEmail;
		this.fromEmail = fromEmail;
		this.port = port;
	}

	public String getToEmail() {
		return toEmail;
	}

	public void setToEmail(final String toEmail) {
		this.toEmail = toEmail;
	}

	public String getFromEmail() {
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

	public Integer getPort() {
		return port;
	}

	public void setPort(final Integer port) {
		this.port = port;
	}

	public String getUser() {
		return user;
	}

	public void setUser(final String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(final String pass) {
		this.password = pass;
	}
}
