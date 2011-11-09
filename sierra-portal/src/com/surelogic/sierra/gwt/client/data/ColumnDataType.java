package com.surelogic.sierra.gwt.client.data;

public enum ColumnDataType {
	DATE("cell-date"), LINK("cell-link"), NUMBER("cell-number"), TEXT(
			"cell-text"), WIDGET("cell-widget");

	private final String css;

	ColumnDataType(final String css) {
		this.css = css;
	}

	public String getCSS() {
		return css;
	}

}
