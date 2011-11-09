package com.surelogic.sierra.jdbc.settings;

/**
 * Represents an entry in a filter set. FilterEntryDO defines equals based on
 * it's finding type.
 * 
 * @author nathan
 * @see Categories
 * @see CategoryDO
 */
public class CategoryEntryDO {

	private final String findingType;

	private final boolean filtered;

	public CategoryEntryDO(String findingType, boolean filtered) {
		this.filtered = filtered;
		this.findingType = findingType;
	}

	public String getFindingType() {
		return findingType;
	}

	/**
	 * 
	 * @return {@code true} if the finding type should be removed from this category
	 */
	public boolean isFiltered() {
		return filtered;
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
		final CategoryEntryDO other = (CategoryEntryDO) obj;
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
