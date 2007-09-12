package com.surelogic.sierra.tool.message;

public class SierraServerLocation {
	private final String host;
	private final Integer port;
	private final String user;
	private final String pass;

	public SierraServerLocation(String host, Integer port, String user,
			String pass) {
		this.host = host;
		this.port = port;
		this.user = user;
		this.pass = pass;
	}

	public SierraServerLocation(String server) {
		String[] strArr = server.split(":");
		String strPort = strArr[1];
		if (strPort != null) {
			this.port = Integer.parseInt(strPort);
		} else {
			this.port = null;
		}
		this.host = strArr[0];
		this.user = null;
		this.pass = null;
		// TODO
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

	@Override
	public String toString() {
		final StringBuilder b = new StringBuilder();
		b.append(getHost()).append(":").append(getPort());
		b.append("/user=\"").append(getUser()).append("\" ");
		b.append(" password=\"").append(getPass()).append("\"");
		return b.toString();
	}
}
