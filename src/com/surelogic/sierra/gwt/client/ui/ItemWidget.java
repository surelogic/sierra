package com.surelogic.sierra.gwt.client.ui;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class ItemWidget<T extends Widget, U> extends Composite {
	private final T ui;
	private U item;

	public ItemWidget(final T ui, final U item) {
		super();
		this.ui = ui;
		this.item = item;
		initWidget(ui);
	}

	public T getUI() {
		return ui;
	}

	public U getItem() {
		return item;
	}

	public void setItem(final U item) {
		this.item = item;
	}
}
