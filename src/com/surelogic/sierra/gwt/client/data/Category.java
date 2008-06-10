package com.surelogic.sierra.gwt.client.data;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import com.surelogic.sierra.gwt.client.util.LangUtil;

public class Category implements Serializable, Cacheable {
	private static final long serialVersionUID = 7604533742268537846L;

	private Set<Category> parents;

	private Set<FindingType> entries;

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

	public Set<Category> getParents() {
		return parents;
	}

	public void setParents(Set<Category> parents) {
		this.parents = parents;
	}

	public Set<FindingType> getEntries() {
		return entries;
	}

	public void setEntries(Set<FindingType> entries) {
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
	public Set<FindingType> getExcludedEntries() {
		final HashSet<FindingType> set = new HashSet<FindingType>(entries
				.size());
		for (FindingType entry : entries) {
			if (entry.isFiltered()) {
				set.add(entry);
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
	public Set<FindingType> getIncludedEntries() {
		final Set<FindingType> set = new HashSet<FindingType>();
		if (parents != null) {
			for (Category parent : parents) {
				set.addAll(parent.getIncludedEntries());
			}
		}
		if (entries != null) {
			for (FindingType entry : entries) {
				if (entry.isFiltered()) {
					set.remove(entry);
				} else {
					set.add(entry);
				}
			}
		}
		return set;
	}

	public boolean parentContains(FindingType finding) {
		for (Category parent : getParents()) {
			boolean contains = parent.getEntries().contains(finding);
			if (contains) {
				return true;
			}
			boolean recursiveContains = parent.parentContains(finding);
			if (recursiveContains) {
				return true;
			}
		}

		return false;
	}

	public Category copy() {
		Category copy = new Category();
		copy.uuid = uuid;
		copy.revision = revision;
		copy.name = name;
		copy.info = info;
		if (parents != null) {
			copy.parents = new HashSet<Category>();
			for (Category parent : parents) {
				copy.parents.add(parent.copy());
			}
		} else {
			copy.parents = null;
		}
		if (entries != null) {
			copy.entries = new HashSet<FindingType>();
			for (FindingType entry : entries) {
				copy.entries.add(entry.copy());
			}
		} else {
			copy.entries = null;
		}
		return copy;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj != null && obj instanceof Category) {
			return LangUtil.equals(uuid, ((Category) obj).uuid);
		}
		return false;
	}

}
