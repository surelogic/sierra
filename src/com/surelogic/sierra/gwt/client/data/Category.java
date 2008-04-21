package com.surelogic.sierra.gwt.client.data;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.google.gwt.core.client.GWT;

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
	 * Returns the set of FilterEntry objects that are explicitly excluded from
	 * this filter set, which is equivalent to the set of entries from
	 * {@link Category#getEntries()} that are filtered.
	 * 
	 * @return
	 */
	public Set getExcludedEntries() {
		final HashSet set = new HashSet(entries.size());
		for (final Iterator i = entries.iterator(); i.hasNext();) {
			final FilterEntry e = (FilterEntry) i.next();
			if (e.isFiltered()) {
				set.add(e);
			}
		}
		return set;
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

	public void copy(Category source) {
		uuid = source.getUuid();
		revision = source.getRevision();
		name = source.getName();
		info = source.getInfo();
		if (source.getParents() != null) {
			parents = new HashSet();
			for (Iterator it = source.getParents().iterator(); it.hasNext();) {
				Category parent = new Category();
				parent.copy((Category) it.next());
				parents.add(parent);
			}
		} else {
			parents = null;
		}
		if (source.getEntries() != null) {
			entries = new HashSet();
			for (Iterator it = source.getEntries().iterator(); it.hasNext();) {
				FilterEntry entry = new FilterEntry();
				entry.copy((FilterEntry) it.next());
				entries.add(entry);
			}
		} else {
			entries = null;
		}
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
		if (!GWT.getTypeName(this).equals(GWT.getTypeName(obj))) {
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
