package com.surelogic.sierra.client.eclipse.model;

public enum FindingsColumn {

	CATEGORY("CATEGORY \"Category", "CATEGORY"),

	CLASS_NAME("CLASS_NAME \"Class__CLASS", "CLASS_NAME"),

	LOC("LOC \"Line", "LOC"),

	MNEMONIC("MNEMONIC \"Mnemonic", "MNEMONIC"),

	PACKAGE_NAME("PACKAGE_NAME \"Package__PACKAGE", "PACKAGE_NAME"),

	PRIORITY("PRIORITY \"Priority", "PRIORITY_CODE"),

	SUMMARY("SUMMARY \"Summary", "SUMMARY"),

	TOOL("TOOL \"Tool", "TOOL");

	private final String f_title;
	private final String f_order;

	private FindingsColumn(String title, String order) {
		if (title == null)
			throw new IllegalArgumentException("title must be non-null");
		if (order == null)
			throw new IllegalArgumentException("order must be non-null");
		f_title = title;
		f_order = order;
	}

	public String getColumnWithTitle() {
		return f_title + "\"";
	}

	public String getColumnWithTitleAndTreeDivider() {
		return f_title + "|\"";
	}

	public String getOrderBy() {
		return f_order;
	}
}
