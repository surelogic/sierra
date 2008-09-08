package com.surelogic.sierra.gwt.client.ui.panel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.surelogic.sierra.gwt.client.Context;
import com.surelogic.sierra.gwt.client.content.ContentComposite;
import com.surelogic.sierra.gwt.client.data.cache.Cacheable;
import com.surelogic.sierra.gwt.client.ui.ItemLabel;
import com.surelogic.sierra.gwt.client.ui.LabelHelper;
import com.surelogic.sierra.gwt.client.ui.SelectionTracker;

public abstract class ListBlock<E extends Cacheable> extends BlockPanel {
	private final String title;
	private final SelectionTracker<ItemLabel<E>> selectionTracker = new SelectionTracker<ItemLabel<E>>();
	private final List<E> items = new ArrayList<E>();
	private final Label none = LabelHelper.italics(new Label("None", false));
	private ClickListener clickListener;

	public ListBlock(final String title) {
		super();
		this.title = title;
	}

	@Override
	protected final void onInitialize(final VerticalPanel contentPanel) {
		setTitle(title);
		setSubsectionStyle(true);

		clickListener = new ClickListener() {

			public void onClick(final Widget sender) {
				final ItemLabel<?> itemUI = (ItemLabel<?>) sender;
				final Cacheable item = (Cacheable) itemUI.getItem();
				new Context(getItemContent(), item.getUuid()).submit();
			}

		};
	}

	protected abstract ContentComposite getItemContent();

	public int getItemCount() {
		return items.size();
	}

	public E getItem(final int index) {
		return items.get(index);
	}

	public void clear() {
		final VerticalPanel content = getContentPanel();
		content.clear();
		content.add(none);
		items.clear();
	}

	public void addItem(final E item) {
		final ItemLabel<E> itemUI = new ItemLabel<E>(getItemText(item), item,
				clickListener);
		itemUI.setSelectionTracker(selectionTracker);
		itemUI.setTitle(getItemTooltip(item));
		final VerticalPanel content = getContentPanel();
		content.remove(none);
		content.add(itemUI);
		items.add(item);
	}

	public void addItems(final Collection<E> items) {
		for (final E item : items) {
			addItem(item);
		}
	}

	protected abstract String getItemText(E item);

	protected abstract String getItemTooltip(E item);

}
