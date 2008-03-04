package com.surelogic.sierra.jdbc.server;

public class Notification {

	private final String toEmail;
	private final String fromEmail;
	private final String host;
	private final String user;
	private final String pass;
	private final Integer port;

	public Notification(String host, Integer port, String user, String pass,
			String toEmail, String fromEmail) {
		this.host = host;
		this.user = user;
		this.pass = pass;
		this.toEmail = toEmail;
		this.fromEmail = fromEmail;
		this.port = port;
	}

	public String getToEmail() {
		return toEmail;
	}

	public String getFromEmail() {
		return fromEmail;
	}

	public String getHost() {
		return host;
	}

	public Integer getPort() {
		return port;
	}

	public String getUser() {
		return user;
	}

	public String getPass() {
		return pass;
	}

}
