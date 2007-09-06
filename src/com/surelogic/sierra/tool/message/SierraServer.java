package com.surelogic.sierra.tool.message;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SierraServer {
	private final String host;
	private final Integer port;

	public SierraServer(String host, Integer port) {
		this.host = host;
		this.port = port;
	}

	public SierraServer(String server) {
		Matcher m = Pattern.compile("([^:]*)(:(\\d+))?").matcher(server);
		String strPort = m.group(3);
		if (strPort != null) {
			this.port = Integer.parseInt(strPort);
		} else {
			this.port = null;
		}
		this.host = m.group(1);

	}

	public String getHost() {
		return host;
	}

	public Integer getPort() {
		return port;
	}

}
