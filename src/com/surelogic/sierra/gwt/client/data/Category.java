package com.surelogic.sierra.gwt.client.data;

import java.io.Serializable;
import java.util.List;

public class Category implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7604533742268537846L;

	/**
	 * @gwt.typeArgs <com.surelogic.sierra.gwt.client.data.Category>
	 */
	private List parents;

	/**
	 * @gwt.typeArgs <com.surelogic.sierra.gwt.client.data.FilterEntry>
	 */
	private List entries;

	private String name;

	private String uuid;

	private long revision;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List getParents() {
		return parents;
	}

	public void setParents(List parents) {
		this.parents = parents;
	}

	public List getEntries() {
		return entries;
	}

	public void setEntries(List entries) {
		this.entries = entries;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public long getRevision() {
		return revision;
	}

	public void setRevision(long revision) {
		this.revision = revision;
	}

}
