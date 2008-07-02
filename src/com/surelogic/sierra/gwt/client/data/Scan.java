package com.surelogic.sierra.gwt.client.data;

import java.io.Serializable;

public class Scan implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6122774750507037724L;

	private String uuid;
	private String user;
	private String project;
	private String javaVendor;
	private String javaVersion;
	private String scanTime;

	public String getUuid() {
		return uuid;
	}

	public void setUuid(final String uuid) {
		this.uuid = uuid;
	}

	public String getUser() {
		return user;
	}

	public void setUser(final String user) {
		this.user = user;
	}

	public String getProject() {
		return project;
	}

	public void setProject(final String project) {
		this.project = project;
	}

	public String getJavaVendor() {
		return javaVendor;
	}

	public void setJavaVendor(final String javaVendor) {
		this.javaVendor = javaVendor;
	}

	public String getJavaVersion() {
		return javaVersion;
	}

	public void setJavaVersion(final String javaVersion) {
		this.javaVersion = javaVersion;
	}

	public String getScanTime() {
		return scanTime;
	}

	public void setScanTime(final String scanTime) {
		this.scanTime = scanTime;
	}

}
