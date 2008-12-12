package com.surelogic.sierra.gwt.client.data;

import java.io.Serializable;
import java.util.Date;

import com.surelogic.sierra.gwt.client.data.cache.Cacheable;
import com.surelogic.sierra.gwt.client.util.LangUtil;

public class Scan implements Serializable, Cacheable, Comparable<Scan> {
	private static final long serialVersionUID = -6122774750507037724L;

	private String uuid;
	private String user;
	private String project;
	private String javaVendor;
	private String javaVersion;
	private String scanTimeDisplay;
	private Date scanTime;

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

	public String getScanTimeDisplay() {
		return scanTimeDisplay;
	}

	public void setScanTimeDisplay(final String scanTimeDisplay) {
		this.scanTimeDisplay = scanTimeDisplay;
	}

	public Date getScanTime() {
		return scanTime;
	}

	public void setScanTime(final Date scanTime) {
		this.scanTime = scanTime;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Scan)) {
			return false;
		}
		Scan other = (Scan) obj;
		if (!LangUtil.equals(uuid, other.uuid)) {
			return false;
		}
		if (!LangUtil.equals(user, other.user)) {
			return false;
		}
		if (!LangUtil.equals(project, other.project)) {
			return false;
		}
		if (!LangUtil.equals(javaVendor, other.javaVendor)) {
			return false;
		}
		if (!LangUtil.equals(javaVersion, other.javaVersion)) {
			return false;
		}
		if (!LangUtil.equals(scanTimeDisplay, other.scanTimeDisplay)) {
			return false;
		}
		if (!LangUtil.equals(scanTime, other.scanTime)) {
			return false;
		}
		return true;
	}
	
	public int compareTo(final Scan o) {
		if (scanTime == null) {
			if (o.scanTime == null) {
				return 0;
			}
			return -1;
		} else if (o.scanTime == null) {
			return 1;
		}
		
		return scanTime.compareTo(o.scanTime);
	}

}
