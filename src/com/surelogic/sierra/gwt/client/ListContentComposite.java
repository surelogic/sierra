package com.surelogic.sierra.gwt.client;

import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.surelogic.sierra.gwt.client.data.Cache;
import com.surelogic.sierra.gwt.client.data.CacheListenerAdapter;
import com.surelogic.sierra.gwt.client.data.Cacheable;
import com.surelogic.sierra.gwt.client.ui.SearchBlock;
import com.surelogic.sierra.gwt.client.util.LangUtil;

public abstract class ListContentComposite<E extends Cacheable, C extends Cache<E>>
		extends ContentComposite {
	private final C cache;
	private final ListView listView;
	private final VerticalPanel selectionPanel = new VerticalPanel();

	protected ListContentComposite(C cache) {
		super();
		this.cache = cache;
		this.listView = new ListView(cache);
	}

	@Override
	protected final void onInitialize(DockPanel rootPanel) {
		listView.initialize();
		rootPanel.add(listView, DockPanel.WEST);
		rootPanel.setCellWidth(listView, "25%");

		selectionPanel.setWidth("100%");
		rootPanel.add(selectionPanel, DockPanel.CENTER);
		rootPanel.setCellWidth(selectionPanel, "75%");

		onInitialize(rootPanel, selectionPanel);

		cache.addListener(new CacheListenerAdapter<E>() {

			@Override
			public void onRefresh(Cache<E> cache, Throwable failure) {
				refreshContext(ContextManager.getContext());
			}

		});
	}

	protected abstract void onInitialize(DockPanel rootPanel,
			VerticalPanel selectionPanel);

	@Override
	protected void onDeactivate() {
		cache.clear();
	}

	@Override
	protected void onUpdate(Context context) {
		if (!isActive()) {
			cache.refresh();
		} else {
			refreshContext(context);
		}
	}

	protected final VerticalPanel getSelectionPanel() {
		return selectionPanel;
	}

	private void refreshContext(Context context) {
		final String uuid = context.getUuid();
		if (LangUtil.notEmpty(uuid)) {
			final E item = cache.getItem(uuid);
			if (item != null) {
				listView.setSelection(item);
			}
			onSelectionChanged(item);
		} else if (cache.getItemCount() > 0) {
			Context.createWithUuid(cache.getItem(0)).submit();
		}
	}

	protected abstract void onSelectionChanged(E item);

	protected abstract String getItemText(E item);

	protected abstract boolean isMatch(E item, String query);

	private class ListView extends SearchBlock<E, C> {
		public ListView(C cache) {
			super(cache);
		}

		@Override
		protected boolean isMatch(E item, String query) {
			return ListContentComposite.this.isMatch(item, query);
		}

		@Override
		protected String getItemText(E item) {
			return ListContentComposite.this.getItemText(item);
		}

		@Override
		protected void doItemClick(E item) {
			Context.createWithUuid(item).submit();
		}

	}
}
