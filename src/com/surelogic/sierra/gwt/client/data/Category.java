package com.surelogic.sierra.gwt.client.data;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class Category implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7604533742268537846L;

	/**
	 * @gwt.typeArgs <com.surelogic.sierra.gwt.client.data.Category>
	 */
	private Set parents;

	/**
	 * @gwt.typeArgs <com.surelogic.sierra.gwt.client.data.FilterEntry>
	 */
	private Set entries;

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

	public Set getParents() {
		return parents;
	}

	public void setParents(Set parents) {
		this.parents = parents;
	}

	public Set getEntries() {
		return entries;
	}

	public void setEntries(Set entries) {
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
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Category other = (Category) obj;
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
