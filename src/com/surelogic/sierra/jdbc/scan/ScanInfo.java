package com.surelogic.sierra.jdbc.scan;

import java.util.Date;

import com.surelogic.sierra.jdbc.record.ScanRecord;

public final class ScanInfo {

	private final String javaVendor;
	private final String javaVersion;
	private final ScanStatus status;
	private final Date scanTime;
	private final String uid;
	private final boolean isPartial;

	ScanInfo(ScanRecord record) {
		this.isPartial = record.isPartial();
		this.javaVendor = record.getJavaVendor();
		this.javaVersion = record.getJavaVersion();
		this.scanTime = record.getTimestamp();
		this.status = record.getStatus();
		this.uid = record.getUid();
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

}
