package com.surelogic.sierra.jdbc.settings;

import com.surelogic.sierra.tool.message.FindingType;

public class FilterEntryDetail {

	private FindingType findingType;

	private boolean filtered;

	public FindingType getFindingType() {
		return findingType;
	}

	public void setFindingType(FindingType findingType) {
		this.findingType = findingType;
	}

	public boolean isFiltered() {
		return filtered;
	}

	public void setFiltered(boolean filtered) {
		this.filtered = filtered;
	}

}
