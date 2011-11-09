package com.surelogic.sierra.client.eclipse.model;

import java.util.List;

/**
 * This class is used to hold Sierra server information read when trying to
 * import the servers
 * 
 * @author Tanmay.Sinha
 * 
 */
public class ImportPageServerHolder {

	private String label;
	private boolean secure;
	private String host;
	private int port;
	private String user;
	private boolean savePassword;
	private List<String> connectedProjects;

	public List<String> getConnectedProjects() {
		return connectedProjects;
	}

	public void setConnectedProjects(List<String> connectedProjects) {
		this.connectedProjects = connectedProjects;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public boolean isSecure() {
		return secure;
	}

	public void setSecure(boolean secure) {
		this.secure = secure;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public boolean isSavePassword() {
		return savePassword;
	}

	public void setSavePassword(boolean savePassword) {
		this.savePassword = savePassword;
	}

	public String getProtocol() {
		return secure ? "https" : "http";
	}

	@Override
	public String toString() {
		final StringBuilder b = new StringBuilder();
		b.append("'").append(getLabel()).append("' is ");
		b.append(getProtocol()).append("://");
		b.append(getHost()).append(":").append(getPort());
		b.append("/user=\"").append(getUser()).append("\" ");
		return b.toString();
	}
}
