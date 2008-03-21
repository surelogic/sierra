package com.surelogic.sierra.gwt.client.data;

import java.io.Serializable;
import java.util.List;

public class FilterSet implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7604533742268537846L;

	/**
	 * @gwt.typeArgs <com.surelogic.sierra.gwt.client.data.FilterSet>
	 */
	private List parents;

	/**
	 * @gwt.typeArgs <com.surelogic.sierra.gwt.client.data.FilterEntry>
	 */
	private List entries;

	private String name;

	private String uuid;

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

}
