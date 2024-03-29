package com.surelogic.sierra.gwt.client.data;

import java.io.Serializable;

import com.surelogic.sierra.gwt.client.data.cache.Cacheable;

public class Project implements Serializable, Cacheable {
	private static final long serialVersionUID = -6729902565459490855L;

	private String uuid;

	private String name;

	private ScanFilter scanFilter;

	public String getUuid() {
		return uuid;
	}

	public void setUuid(final String uuid) {
		this.uuid = uuid;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public ScanFilter getScanFilter() {
		return scanFilter;
	}

	public void setScanFilter(final ScanFilter scanFilter) {
		this.scanFilter = scanFilter;
	}

}
