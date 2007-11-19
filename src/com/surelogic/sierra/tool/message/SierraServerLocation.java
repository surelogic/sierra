package com.surelogic.sierra.tool.message;

public class SierraServerLocation {

	private static final int DEFAULT_PORT = 8080;

	private static final String UNLABELED_SERVER = "unlabeled server";

	public SierraServerLocation(String host, boolean secure, int port,
			String user, String password) {
		this(UNLABELED_SERVER, host, secure, port, user, password);
	}

	public SierraServerLocation(String label, String host, boolean secure,
			int port, String user, String pass) {
		f_host = host;
		f_secure = secure;
		f_port = port;
		f_user = user;
		f_password = pass;
		f_label = label;
	}

	public SierraServerLocation(String server, String user, String pass) {
		/*
		 * TODO: fix this to set the protocol properly
		 */
		String[] strArr = server.split(":");
		String strPort = strArr[1];
		if (strPort != null) {
			f_port = Integer.parseInt(strPort);
		} else {
			f_port = DEFAULT_PORT;
		}
		f_host = strArr[0];
		f_secure = false;
		f_user = user;
		f_password = pass;
		f_label = UNLABELED_SERVER;
	}

	/**
	 * A client label for this server location, or <tt>unknown</tt> if not a
	 * client connection, e.g., in the Ant task.
	 */
	private final String f_label;

	public String getLabel() {
		return f_label;
	}

	private final boolean f_secure;

	public boolean isSecure() {
		return f_secure;
	}

	public String getProtocol() {
		return f_secure ? "https" : "http";
	}

	private final String f_host;

	public String getHost() {
		return f_host;
	}

	private final int f_port;

	public int getPort() {
		return f_port;
	}

	private final String f_user;

	public String getUser() {
		return f_user;
	}

	private final String f_password;

	public String getPass() {
		return f_password;
	}

	@Override
	public String toString() {
		final StringBuilder b = new StringBuilder();
		b.append("'").append(getLabel()).append("' is ");
		b.append(getProtocol()).append("://");
		b.append(getHost()).append(":").append(getPort());
		b.append("/user=\"").append(getUser()).append("\" ");
		b.append(" password=\"").append(getPass()).append("\"");
		return b.toString();
	}
}
