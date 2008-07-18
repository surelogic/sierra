package com.surelogic.sierra.gwt.client.data;

public enum ColumnData {
	DATE("cell-date"), LINK("cell-link"), NUMBER("cell-number"), TEXT(
			"cell-text"), WIDGET("cell-widget");

	private final String css;

	ColumnData(final String css) {
		this.css = css;
	}

	public String getCSS() {
		return css;
	}

}
