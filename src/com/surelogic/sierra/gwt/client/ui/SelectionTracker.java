package com.surelogic.sierra.gwt.client.ui;

import com.surelogic.sierra.gwt.client.util.LangUtil;

public class SelectionTracker {
	private Object selected;

	public boolean isSelected(Object item) {
		return LangUtil.equals(selected, item);
	}

	public Object getSelected() {
		return selected;
	}

	public Object setSelected(Object selected) {
		final Object lastSelection = this.selected;
		this.selected = selected;
		return lastSelection;
	}
}
