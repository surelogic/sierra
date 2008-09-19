package com.surelogic.sierra.gwt.client.ui;

import java.util.Collection;
import java.util.List;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.surelogic.sierra.gwt.client.util.LangUtil;

public class ItemWidget<T extends Widget, U> extends Composite {
	private final T ui;
	private U item;

	public static <T extends Widget, U> int indexOf(
			final List<? extends ItemWidget<T, U>> list, final U item) {
		for (int i = 0; i < list.size(); i++) {
			final ItemWidget<T, U> entry = list.get(i);
			if (entry != null) {
				if (LangUtil.equals(entry.getItem(), item)) {
					return i;
				}
			}
		}
		return -1;
	}

	public static <T extends Widget, U> int indexOf(
			final List<? extends ItemWidget<T, U>> list, final T uiItem) {
		for (int i = 0; i < list.size(); i++) {
			final ItemWidget<T, U> entry = list.get(i);
			if (entry != null) {
				if (LangUtil.equals(entry.getUI(), uiItem)) {
					return i;
				}
			}
		}
		return -1;
	}

	public static <T extends Widget, U> T findItemUI(
			final Collection<? extends ItemWidget<T, U>> collection,
			final U item) {
		for (final ItemWidget<T, U> entry : collection) {
			if (LangUtil.equals(entry.getItem(), item)) {
				return entry.getUI();
			}
		}
		return null;
	}

	public static <T extends Widget, U> U findItem(
			final Collection<? extends ItemWidget<T, U>> collection,
			final T itemUI) {
		for (final ItemWidget<T, U> entry : collection) {
			if (LangUtil.equals(entry.getUI(), itemUI)) {
				return entry.getItem();
			}
		}
		return null;
	}

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
