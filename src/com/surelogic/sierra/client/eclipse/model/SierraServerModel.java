package com.surelogic.sierra.client.eclipse.model;

public final class SierraServerModel {

	private final SierraServerManager f_manager;

	public SierraServerManager getManager() {
		return f_manager;
	}

	public SierraServerModel(final SierraServerManager manager,
			final String label) {
		assert manager != null;
		f_manager = manager;
		assert label != null;
		f_label = label;
		assert !f_manager.f_labelToServer.containsKey(f_label);
		f_manager.f_labelToServer.put(f_label, this);
	}

	private String f_label = "";

	public String getLabel() {
		return f_label;
	}

	public void setLabel(String label) {
		if (label == null || label.equals(f_label))
			return;
		// overwrite semantics, i.e., no check if the new name is in use
		f_manager.f_labelToServer.remove(f_label);
		f_label = label;
		f_manager.f_labelToServer.put(f_label, this);
		f_manager.notifyObservers();
	}

	private boolean f_secure = false;

	public boolean isSecure() {
		return f_secure;
	}

	public String getProtocol() {
		return f_secure ? "https" : "http";
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

	public String toURLString() {
		final StringBuilder b = new StringBuilder();
		b.append(getProtocol()).append("://");
		b.append(getHost()).append(":").append(getPort());
		return b.toString();
	}

	@Override
	public String toString() {
		final StringBuilder b = new StringBuilder();
		b.append("'").append(getLabel()).append("' is ");
		b.append(getProtocol()).append("://");
		b.append(getHost()).append(":").append(getPort());
		b.append("/user=\"").append(getUser()).append("\" ");
		b.append(" password=\"").append(getPassword()).append("\" ");
		b.append(" password-is-saved=").append(savePassword());
		return b.toString();
	}
}
