package com.surelogic.sierra.gwt.client.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.surelogic.sierra.gwt.client.data.cache.Cacheable;
import com.surelogic.sierra.gwt.client.util.LangUtil;

public class Category implements Serializable, Cacheable, Comparable<Category> {
	private static final long serialVersionUID = 7604533742268537846L;

	private Set<Category> parents;

	private Set<FindingTypeFilter> entries;

	private String name;

	private String uuid;

	private long revision;

	private String info;

	private List<ScanFilterInfo> scanFiltersUsing;

	private boolean local;

	private String ownerLabel;

	private String ownerURL;

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public Set<Category> getParents() {
		return parents;
	}

	public void setParents(final Set<Category> parents) {
		this.parents = parents;
	}

	public Set<FindingTypeFilter> getEntries() {
		return entries;
	}

	public void setEntries(final Set<FindingTypeFilter> entries) {
		this.entries = entries;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(final String uuid) {
		this.uuid = uuid;
	}

	public long getRevision() {
		return revision;
	}

	public void setRevision(final long revision) {
		this.revision = revision;
	}

	public String getInfo() {
		return info;
	}

	public void setInfo(final String info) {
		this.info = info;
	}

	public boolean isLocal() {
		return local;
	}

	public void setLocal(final boolean local) {
		this.local = local;
	}

	public String getOwnerLabel() {
		return ownerLabel;
	}

	public void setOwnerLabel(final String ownerLabel) {
		this.ownerLabel = ownerLabel;
	}

	public String getOwnerURL() {
		return ownerURL;
	}

	public void setOwnerURL(final String ownerURL) {
		this.ownerURL = ownerURL;
	}

	public List<ScanFilterInfo> getScanFiltersUsing() {
		if (scanFiltersUsing == null) {
			scanFiltersUsing = new ArrayList<ScanFilterInfo>();
		}
		return scanFiltersUsing;
	}

	/**
	 * Returns the set of FilterEntry objects that are explicitly excluded from
	 * this filter set, which is equivalent to the set of entries from
	 * {@link Category#getEntries()} that are filtered.
	 * 
	 * @return
	 */
	public Set<FindingTypeFilter> getExcludedEntries() {
		final HashSet<FindingTypeFilter> set = new HashSet<FindingTypeFilter>(
				entries.size());
		for (final FindingTypeFilter entry : entries) {
			if (entry.isFiltered()) {
				set.add(entry);
			}
		}
		return Collections.unmodifiableSet(set);
	}

	/**
	 * Returns the set of FilterEntry objects that are included in this
	 * category.
	 * 
	 * @return
	 */
	public Set<FindingTypeFilter> getIncludedEntries() {
		final Set<FindingTypeFilter> set = new HashSet<FindingTypeFilter>();
		if (parents != null) {
			for (final Category parent : parents) {
				set.addAll(parent.getIncludedEntries());
			}
		}
		if (entries != null) {
			for (final FindingTypeFilter entry : entries) {
				if (entry.isFiltered()) {
					set.remove(entry);
				} else {
					set.add(entry);
				}
			}
		}
		return Collections.unmodifiableSet(set);
	}

	public boolean parentContains(final FindingTypeFilter finding) {
		for (final Category parent : getParents()) {
			final boolean contains = parent.getEntries().contains(finding);
			if (contains) {
				return true;
			}
			final boolean recursiveContains = parent.parentContains(finding);
			if (recursiveContains) {
				return true;
			}
		}

		return false;
	}

	public void updateFilter(final FindingTypeFilter finding) {
		// remove any previous copies of the finding and re-add it to reflect
		// the current filter flag
		getEntries().remove(finding);
		getEntries().add(finding);

		cleanCategories();
	}

	public void cleanCategories() {
		final List<Category> unusedCategories = new ArrayList<Category>();
		for (final Category parent : getParents()) {
			if (parent.getIncludedEntries().isEmpty()) {
				unusedCategories.add(parent);
			}
		}
		for (final Category unusedCat : unusedCategories) {
			getParents().remove(unusedCat);
		}
	}

	public Category copy() {
		final Category copy = new Category();
		copy.uuid = uuid;
		copy.revision = revision;
		copy.name = name;
		copy.info = info;
		copy.local = local;
		if (parents != null) {
			copy.parents = new HashSet<Category>();
			for (final Category parent : parents) {
				copy.parents.add(parent.copy());
			}
		} else {
			copy.parents = null;
		}
		if (entries != null) {
			copy.entries = new HashSet<FindingTypeFilter>();
			for (final FindingTypeFilter entry : entries) {
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
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if ((obj != null) && (obj instanceof Category)) {
			return LangUtil.equals(uuid, ((Category) obj).uuid);
		}
		return false;
	}

	public int compareTo(final Category o) {
		int cmp = Boolean.valueOf(o.isLocal()).compareTo(
				Boolean.valueOf(isLocal()));
		if (cmp == 0) {
			cmp = getName().compareToIgnoreCase(o.getName());
		}
		return cmp;
	}

	public static class ScanFilterInfo implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 2208099313060915788L;
		private String uuid;
		private String name;

		public ScanFilterInfo() {
			// Do nothing
		}

		public ScanFilterInfo(final String uuid, final String name) {
			this.uuid = uuid;
			this.name = name;
		}

		public String getUuid() {
			return uuid;
		}

		public void setUuid(final String uuid) {
			this.uuid = uuid;
		}

		public String getName() {
			return name;
		}

		public void setName(final String name) {
			this.name = name;
		}

	}

}
