package com.surelogic.sierra.client.eclipse.model;

public enum FindingsColumn {

	PROJECT("Project__PRJ");

	private final String f_title;

	private FindingsColumn(String title) {
		f_title = title;
	}

	public String getColumnWithTitle() {
		return this.toString() + " \"" + f_title + "\"";
	}

	public String getColumnWithTitleAndTreeDivider() {
		return this.toString() + " \"" + f_title + "|\"";
	}
}
