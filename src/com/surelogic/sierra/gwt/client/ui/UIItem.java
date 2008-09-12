package com.surelogic.sierra.gwt.client.ui;

import java.util.Collection;
import java.util.List;

import com.google.gwt.user.client.ui.UIObject;
import com.surelogic.sierra.gwt.client.util.LangUtil;

public class UIItem<T extends UIObject, U> {
	private T ui;
	private U item;

	public static <T extends UIObject, U> int indexOf(
			final List<UIItem<T, U>> list, final U item) {
		for (int i = 0; i < list.size(); i++) {
			final UIItem<T, U> entry = list.get(i);
			if (entry != null) {
				if (LangUtil.equals(entry.getItem(), item)) {
					return i;
				}
			}
		}
		return -1;
	}

	public static <T extends UIObject, U> int indexOf(
			final List<UIItem<T, U>> list, final T uiItem) {
		for (int i = 0; i < list.size(); i++) {
			final UIItem<T, U> entry = list.get(i);
			if (entry != null) {
				if (LangUtil.equals(entry.getUI(), uiItem)) {
					return i;
				}
			}
		}
		return -1;
	}

	public static <T extends UIObject, U> T findItemUI(
			final Collection<UIItem<T, U>> collection, final U item) {
		for (final UIItem<T, U> entry : collection) {
			if (LangUtil.equals(entry.getItem(), item)) {
				return entry.getUI();
			}
		}
		return null;
	}

	public static <T extends UIObject, U> U findItem(
			final Collection<UIItem<T, U>> collection, final T itemUI) {
		for (final UIItem<T, U> entry : collection) {
			if (LangUtil.equals(entry.getUI(), itemUI)) {
				return entry.getItem();
			}
		}
		return null;
	}

	public UIItem(final T ui, final U item) {
		super();
		this.ui = ui;
		this.item = item;
	}

	public T getUI() {
		return ui;
	}

	public void setUI(final T ui) {
		this.ui = ui;
	}

	public U getItem() {
		return item;
	}

	public void setItem(final U item) {
		this.item = item;
	}
}
