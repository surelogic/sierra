package com.surelogic.sierra.gwt.client.ui;

import com.google.gwt.user.client.ui.CheckBox;

public class ItemCheckBox<T> extends CheckBox {
	private T item;

	public ItemCheckBox(final T item) {
		super();
		this.item = item;
	}

	public ItemCheckBox(final String label, final T item) {
		super(label);
		this.item = item;
	}

	public ItemCheckBox(final String label, final boolean asHtml, final T item) {
		super(label, asHtml);
		this.item = item;
	}

	public T getItem() {
		return item;
	}

	public void setItem(final T item) {
		this.item = item;
	}

}
