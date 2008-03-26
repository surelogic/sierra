package com.surelogic.sierra.jdbc.settings;

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

}
