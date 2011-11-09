package com.surelogic.sierra.jdbc.tool;

public class FindingTypeDisplay {

	private final FindingTypeKey key;
	private final String category;
	private final String display;

	FindingTypeDisplay(FindingTypeKey key, String category, String display) {
		this.key = key;
		this.category = category;
		this.display = display;
	}

	public String getTool() {
		return key.getTool();
	}

	public String getVersion() {
		return key.getVersion();
	}

	public String getCategory() {
		return category;
	}

	public String getDisplay() {
		return display;
	}

	public String getMnemonic() {
		return key.getMnemonic();
	}

	public FindingTypeKey getKey() {
		return key;
	}

}
