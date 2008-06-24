package com.surelogic.sierra.gwt.client.ui;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.surelogic.sierra.gwt.client.ContentComposite;
import com.surelogic.sierra.gwt.client.Context;
import com.surelogic.sierra.gwt.client.data.Cacheable;

public abstract class ListBlock<E extends Cacheable> extends BlockPanel {
	private final String title;
	private final SelectionTracker<ItemLabel<E>> selectionTracker = new SelectionTracker<ItemLabel<E>>();
	private final List<E> items = new ArrayList<E>();
	final Label none = new Label("None", false);
	private ClickListener clickListener;

	public ListBlock(String title) {
		super();
		this.title = title;
	}

	@Override
	protected final void onInitialize(VerticalPanel contentPanel) {
		setTitle(title);
		setSubsectionStyle(true);

		none.addStyleName("font-italic");

		clickListener = new ClickListener() {

			public void onClick(Widget sender) {
				final ItemLabel<?> itemUI = (ItemLabel<?>) sender;
				final Cacheable item = (Cacheable) itemUI.getItem();
				Context.createWithUuid(getItemContent(), item.getUuid())
						.submit();
			}

		};
	}

	protected abstract ContentComposite getItemContent();

	public int getItemCount() {
		return items.size();
	}

	public E getItem(int index) {
		return items.get(index);
	}

	public void clear() {
		final VerticalPanel content = getContentPanel();
		content.clear();
		content.add(none);
		items.clear();
	}

	public void addItem(E item) {
		final ItemLabel<E> itemUI = new ItemLabel<E>(getItemText(item), item,
				selectionTracker, clickListener);
		itemUI.setTitle(getItemTooltip(item));
		final VerticalPanel content = getContentPanel();
		content.remove(none);
		content.add(itemUI);
		items.add(item);
	}

	protected abstract String getItemText(E item);

	protected abstract String getItemTooltip(E item);

}
