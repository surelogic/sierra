package com.surelogic.sierra.gwt.client.ui.panel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.surelogic.sierra.gwt.client.content.ContentComposite;
import com.surelogic.sierra.gwt.client.data.cache.Cacheable;
import com.surelogic.sierra.gwt.client.ui.StyleHelper;
import com.surelogic.sierra.gwt.client.ui.StyleHelper.Style;
import com.surelogic.sierra.gwt.client.ui.link.ContentLink;

public abstract class ListBlock<E extends Cacheable> extends BasicPanel {
	private final String title;
	private final List<E> items = new ArrayList<E>();
	private final Label none = StyleHelper.add(new Label("None", false),
			Style.ITALICS);

	public ListBlock(final String title) {
		super();
		this.title = title;
	}

	@Override
	protected final void onInitialize(final VerticalPanel contentPanel) {
		setTitle(title);
		setSubsectionStyle(true);
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
		final ContentLink itemUI = new ContentLink(getItemText(item),
				getItemContent(), item.getUuid());
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
