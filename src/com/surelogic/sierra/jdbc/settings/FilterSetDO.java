package com.surelogic.sierra.jdbc.settings;

import java.util.ArrayList;
import java.util.List;

public class FilterSetDO {

	private String uid;
	private String name;
	private String info;
	private final List<String> parents = new ArrayList<String>();
	private final List<FilterEntryDO> filterEntries = new ArrayList<FilterEntryDO>();
	private long revision;

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<String> getParents() {
		return parents;
	}

	public List<FilterEntryDO> getFilters() {
		return filterEntries;
	}

	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}

	public long getRevision() {
		return revision;
	}

	public void setRevision(long revision) {
		this.revision = revision;
	}

}
