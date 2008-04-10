package com.surelogic.sierra.jdbc.settings;

import com.surelogic.sierra.tool.message.Importance;

/**
 * Represents a filter on a category. This data object is owned by a
 * {@link ScanFilterDO}.
 * 
 * @author nathan
 * 
 */
public class CategoryFilterDO {
	private String uid;

	private Importance importance;

	private boolean filtered;

	public CategoryFilterDO() {
	}

	public CategoryFilterDO(String uid, Importance importance, boolean filtered) {
		this.uid = uid;
		this.importance = importance;
		this.filtered = filtered;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public Importance getImportance() {
		return importance;
	}

	public void setImportance(Importance importance) {
		this.importance = importance;
	}

	public boolean isFiltered() {
		return filtered;
	}

	public void setFiltered(boolean filtered) {
		this.filtered = filtered;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((uid == null) ? 0 : uid.hashCode());
		return result;
	}

	@Override
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
		final CategoryFilterDO other = (CategoryFilterDO) obj;
		if (uid == null) {
			if (other.uid != null) {
				return false;
			}
		} else if (!uid.equals(other.uid)) {
			return false;
		}
		return true;
	}

}
