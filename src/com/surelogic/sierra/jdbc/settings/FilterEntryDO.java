package com.surelogic.sierra.jdbc.settings;

/**
 * Represents an entry in a filter set. FilterEntryDO defines equals based on
 * it's finding type.
 * 
 * @author nathan
 * 
 */
public class FilterEntryDO {

	private final String findingType;

	private final boolean filtered;

	public FilterEntryDO(String findingType, boolean filtered) {
		this.filtered = filtered;
		this.findingType = findingType;
	}

	public String getFindingType() {
		return findingType;
	}

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
		final FilterEntryDO other = (FilterEntryDO) obj;
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
