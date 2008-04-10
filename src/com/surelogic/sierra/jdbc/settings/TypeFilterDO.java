package com.surelogic.sierra.jdbc.settings;

import com.surelogic.sierra.tool.message.Importance;

/**
 * Represents a filter of a single finding type. This data object is owned by a
 * {@link ScanFilterDO}.
 * 
 * @author nathan
 * 
 */
public class TypeFilterDO {

	private String findingType;

	private Importance importance;

	private boolean filtered;

	public TypeFilterDO() {
		// Do nothing
	}

	public TypeFilterDO(String findingType, Importance importance,
			boolean filtered) {
		this.filtered = filtered;
		this.findingType = findingType;
		this.importance = importance;
	}

	public String getFindingType() {
		return findingType;
	}

	public void setFindingType(String findingType) {
		this.findingType = findingType;
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
		result = prime * result
				+ ((findingType == null) ? 0 : findingType.hashCode());
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
		final TypeFilterDO other = (TypeFilterDO) obj;
		if (findingType == null) {
			if (other.findingType != null) {
				return false;
			}
		} else if (!findingType.equals(other.findingType)) {
			return false;
		}
		return true;
	}

}
