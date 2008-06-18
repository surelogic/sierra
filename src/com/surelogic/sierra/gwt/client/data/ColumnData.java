package com.surelogic.sierra.gwt.client.data;

public enum ColumnData {
	TEXT("cell-text"), DATE("cell-date"), NUMBER("cell-number");

	private final String css;

	ColumnData(String css) {
		this.css = css;
	}

	public String getCSS() {
		return css;
	}

}
