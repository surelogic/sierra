package com.surelogic.sierra.jdbc.settings;

import java.util.HashSet;
import java.util.Set;

public class SettingsDO {

	private String uid;

	private long revision;

	private final Set<String> filterSets = new HashSet<String>();

	private final Set<String> projects = new HashSet<String>();

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public long getRevision() {
		return revision;
	}

	public void setRevision(long revision) {
		this.revision = revision;
	}

	public Set<String> getFilterSets() {
		return filterSets;
	}

	public Set<String> getProjects() {
		return projects;
	}

}
