package com.surelogic.sierra.jdbc.project;

public class ProjectDO {

	private long id;

	private String uuid;

	private String name;

	private String scanFilter;

	public long getId() {
		return id;
	}

	public void setId(final long id) {
		this.id = id;
	}

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

	public String getScanFilter() {
		return scanFilter;
	}

	public void setScanFilter(final String scanFilter) {
		this.scanFilter = scanFilter;
	}

}
