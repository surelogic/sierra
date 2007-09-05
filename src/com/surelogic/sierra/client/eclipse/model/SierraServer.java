package com.surelogic.sierra.client.eclipse.model;

public final class SierraServer {

	private final SierraServerManager f_manager;

	public SierraServerManager getManager() {
		return f_manager;
	}

	public SierraServer(final SierraServerManager manager, final String name) {
		assert manager != null;
		f_manager = manager;
		assert name != null;
		f_name = name;
	}

	private String f_name = "";

	public String getName() {
		return f_name;
	}

	public void setName(String name) {
		f_name = name;
	}

	private boolean f_secure = false;

	public boolean isSecure() {
		return f_secure;
	}

	public void setSecure(boolean secure) {
		f_secure = secure;
	}

	private String f_host = "";

	public String getHost() {
		return f_host;
	}

	public void setHost(String host) {
		f_host = host;
	}

	private int f_port = 8080;

	public int getPort() {
		return f_port;
	}

	public void setPort(int port) {
		f_port = port;
	}

	private String f_user = "";

	public String getUser() {
		return f_user;
	}

	public void setUser(String user) {
		f_user = user;
	}

	private String f_password = "";

	public String getPassword() {
		return f_password;
	}

	public void setPassword(String password) {
		f_password = password;
	}

	private boolean f_savePassword = false;

	public boolean savePassword() {
		return f_savePassword;
	}

	public void setSavePassword(boolean savePassword) {
		f_savePassword = savePassword;
	}
}
