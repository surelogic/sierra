package com.surelogic.sierra.tool.message;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
		Matcher m = Pattern.compile("([^:]*)(:(\\d+))?").matcher(server);
		String strPort = m.group(3);
		if (strPort != null) {
			this.port = Integer.parseInt(strPort);
		} else {
			this.port = null;
		}
		this.host = m.group(1);
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

}
