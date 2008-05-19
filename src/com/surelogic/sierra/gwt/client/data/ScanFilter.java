package com.surelogic.sierra.gwt.client.data;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class ScanFilter implements Serializable, Cacheable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5806872701106755880L;

	private String uuid;

	private String name;

	private long revision;
	/**
	 * @gwt.typeArgs <com.surelogic.sierra.gwt.client.data.ScanFilterEntry>
	 */
	private Set categories;
	/**
	 * @gwt.typeArgs <com.surelogic.sierra.gwt.client.data.ScanFilterEntry>
	 */
	private Set types;

	/**
	 * @gwt.typeArgs <java.lang.String>
	 */
	private Set projects;

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getRevision() {
		return revision;
	}

	public void setRevision(long revision) {
		this.revision = revision;
	}

	/**
	 * @gwt.typeArgs <java.lang.String>
	 * @return
	 */
	public Set getProjects() {
		if (projects == null) {
			projects = new HashSet();
		}
		return projects;
	}

	/**
	 * Return the categories defined in this scan filter
	 * 
	 * @gwt.typeArgs <com.surelogic.sierra.gwt.client.data.ScanFilterEntry>
	 * @return
	 */
	public Set getCategories() {
		if (categories == null) {
			categories = new HashSet();
		}
		return categories;
	}

	/**
	 * Return the finding types referenced in this scan filter
	 * 
	 * @gwt.typeArgs <com.surelogic.sierra.gwt.client.data.ScanFilterEntry>
	 * @return
	 */
	public Set getTypes() {
		if (types == null) {
			types = new HashSet();
		}
		return types;
	}

	public ScanFilter copy() {
		final ScanFilter copy = new ScanFilter();
		copy.uuid = uuid;
		copy.name = name;
		copy.revision = revision;
		copy.categories = new HashSet(getCategories().size());
		for (final Iterator i = getCategories().iterator(); i.hasNext();) {
			copy.categories.add(i.next());
		}
		copy.types = new HashSet(getTypes().size());
		for (final Iterator i = getTypes().iterator(); i.hasNext();) {
			copy.types.add(i.next());
		}
		copy.projects = new HashSet(getProjects().size());
		for (final Iterator i = getProjects().iterator(); i.hasNext();) {
			copy.projects.add(i.next());
		}
		return copy;
	}

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		final ScanFilter other = (ScanFilter) obj;
		if (uuid == null) {
			if (other.uuid != null) {
				return false;
			}
		} else if (!uuid.equals(other.uuid)) {
			return false;
		}
		return true;
	}

}
