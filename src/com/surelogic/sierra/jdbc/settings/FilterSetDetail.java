package com.surelogic.sierra.jdbc.settings;

import java.util.ArrayList;
import java.util.List;

public class FilterSetDetail {

	private String uid;
	private String name;
	private List<ParentDetail> parents = new ArrayList<ParentDetail>();
	private List<FilterEntryDetail> filterEntries = new ArrayList<FilterEntryDetail>();

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

	public List<ParentDetail> getParents() {
		return parents;
	}

	public List<FilterEntryDetail> getFilters() {
		return filterEntries;
	}

}
