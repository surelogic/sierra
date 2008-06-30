package com.surelogic.sierra.jdbc.scan;

import java.util.Date;

public final class ScanInfo {

	private final String uid;
	private final String user;
	private final String project;
	private final String javaVendor;
	private final String javaVersion;
	private final ScanStatus status;
	private final Date scanTime;
	private final boolean isPartial;

	ScanInfo(final String uid, final String user, final String project,
			final String javaVendor, final String javaVersion,
			final ScanStatus status, final Date scanTime,
			final boolean isPartial) {
		this.uid = uid;
		this.user = user;
		this.project = project;
		this.javaVendor = javaVendor;
		this.javaVersion = javaVersion;
		this.status = status;
		this.scanTime = scanTime;
		this.isPartial = isPartial;
	}

	public String getJavaVendor() {
		return javaVendor;
	}

	public String getJavaVersion() {
		return javaVersion;
	}

	public ScanStatus getStatus() {
		return status;
	}

	public Date getScanTime() {
		return scanTime;
	}

	public String getUid() {
		return uid;
	}

	public boolean isPartial() {
		return isPartial;
	}

	public String getUser() {
		return user;
	}

	public String getProject() {
		return project;
	}

}
