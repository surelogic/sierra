package com.surelogic.sierra.jdbc.settings;

import com.surelogic.sierra.tool.message.Importance;

/**
 * Represents a filter on a category. Category filters can set the importance of
 * a category. Equality is based on uid.
 * 
 * @author nathan
 * @see ScanFilters
 * @see ScanFilterDO
 */
public class CategoryFilterDO {
	private String uid;

	private Importance importance;

	public CategoryFilterDO() {
	}

	public CategoryFilterDO(String uid, Importance importance) {
		this.uid = uid;
		this.importance = importance;
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
