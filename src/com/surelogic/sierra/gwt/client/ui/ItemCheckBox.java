package com.surelogic.sierra.gwt.client.ui;

import com.google.gwt.user.client.ui.CheckBox;

public class ItemCheckBox<T> extends CheckBox implements HasItem<T> {
	private T item;

	public ItemCheckBox(T item) {
		super();
		this.item = item;
	}

	public ItemCheckBox(String label, T item) {
		super(label);
		this.item = item;
	}

	public ItemCheckBox(String label, boolean asHtml, T item) {
		super(label, asHtml);
		this.item = item;
	}

	public T getItem() {
		return item;
	}

	public void setItem(T item) {
		this.item = item;
	}

}
