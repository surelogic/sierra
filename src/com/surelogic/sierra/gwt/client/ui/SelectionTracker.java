package com.surelogic.sierra.gwt.client.ui;

import com.surelogic.sierra.gwt.client.util.LangUtil;

public class SelectionTracker<T> {
	private T selected;

	public boolean isSelected(Object item) {
		return LangUtil.equals(selected, item);
	}

	public T getSelected() {
		return selected;
	}

	public T setSelected(T selected) {
		final T lastSelection = this.selected;
		this.selected = selected;
		return lastSelection;
	}
}
