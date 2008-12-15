package com.surelogic.sierra.jdbc.settings;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents a scan filter in the database. Equality is based on uid.
 * 
 * @author nathan
 * @see ScanFilters
 */
public class ScanFilterDO {

	private String uid;

	private String name;

	private long revision;

	private final Set<CategoryFilterDO> filterSets = new HashSet<CategoryFilterDO>();

	private final Set<TypeFilterDO> filterTypes = new HashSet<TypeFilterDO>();

	public String getUid() {
		return uid;
	}

	public void setUid(final String uid) {
		this.uid = uid;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public long getRevision() {
		return revision;
	}

	public void setRevision(final long revision) {
		this.revision = revision;
	}

	public Set<CategoryFilterDO> getCategories() {
		return filterSets;
	}

	public Set<TypeFilterDO> getFilterTypes() {
		return filterTypes;
	}

}
