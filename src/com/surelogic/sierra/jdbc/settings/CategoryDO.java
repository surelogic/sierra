package com.surelogic.sierra.jdbc.settings;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents a filter set in the database. Equality is defined based on uid.
 * 
 * @author nathan
 * 
 */
public class CategoryDO {

	private String uid;
	private String name;
	private String info;
	private final Set<String> parents = new HashSet<String>();
	private final Set<CategoryEntryDO> filterEntries = new HashSet<CategoryEntryDO>();
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

	public Set<String> getParents() {
		return parents;
	}

	public Set<CategoryEntryDO> getFilters() {
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((uid == null) ? 0 : uid.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final CategoryDO other = (CategoryDO) obj;
		if (uid == null) {
			if (other.uid != null) {
				return false;
			}
		} else if (!uid.equals(other.uid)) {
			return false;
		}
		return true;
	}

}
