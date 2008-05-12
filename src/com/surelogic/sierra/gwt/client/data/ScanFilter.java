package com.surelogic.sierra.gwt.client.data;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class ScanFilter implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5806872701106755880L;

	private String uid;

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
		copy.uid = uid;
		copy.name = name;
		copy.revision = revision;
		copy.categories = categories == null ? null : new HashSet(categories);
		copy.types = types == null ? null : new HashSet(types);
		copy.projects = projects == null ? null : new HashSet(projects);
		return copy;
	}
}
