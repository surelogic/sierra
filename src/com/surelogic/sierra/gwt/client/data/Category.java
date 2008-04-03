package com.surelogic.sierra.gwt.client.data;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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

	private String info;

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

	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}

	/**
	 * Returns the set of FilterEntry objects that are included in this
	 * category.
	 * 
	 * @return
	 */
	public Set getIncludedEntries() {
		final Set set = new HashSet();
		if (parents != null) {
			for (final Iterator i = parents.iterator(); i.hasNext();) {
				set.addAll(((Category) i.next()).getIncludedEntries());
			}
		}
		if (entries != null) {
			for (final Iterator i = entries.iterator(); i.hasNext();) {
				final FilterEntry entry = (FilterEntry) i.next();
				if (entry.isFiltered()) {
					set.remove(entry);
				} else {
					set.add(entry);
				}
			}
		}
		return set;
	}

}
