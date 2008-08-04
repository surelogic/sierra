package com.surelogic.sierra.gwt.client.content;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.surelogic.sierra.gwt.client.Context;
import com.surelogic.sierra.gwt.client.ContextManager;
import com.surelogic.sierra.gwt.client.data.Status;
import com.surelogic.sierra.gwt.client.data.cache.Cache;
import com.surelogic.sierra.gwt.client.data.cache.CacheListener;
import com.surelogic.sierra.gwt.client.data.cache.Cacheable;
import com.surelogic.sierra.gwt.client.ui.BlockPanel;
import com.surelogic.sierra.gwt.client.ui.SearchBlock;
import com.surelogic.sierra.gwt.client.util.LangUtil;

public abstract class ListContentComposite<E extends Cacheable, C extends Cache<E>>
		extends ContentComposite {
	private final C cache;
	private final ListView listView;
	private final VerticalPanel westPanel = new VerticalPanel();
	private final ActionBlock actionBlock = new ActionBlock();
	private final VerticalPanel selectionPanel = new VerticalPanel();
	private CacheListener<E> cacheListener;
	private E selection;

	protected ListContentComposite(C cache) {
		super();
		this.cache = cache;
		this.listView = new ListView(cache);
	}

	@Override
	protected final void onInitialize(DockPanel rootPanel) {
		westPanel.setWidth("100%");

		listView.initialize();
		westPanel.add(listView);

		rootPanel.add(westPanel, DockPanel.WEST);
		rootPanel.setCellWidth(westPanel, "25%");

		selectionPanel.setWidth("100%");
		rootPanel.add(selectionPanel, DockPanel.CENTER);
		rootPanel.setCellWidth(selectionPanel, "75%");

		onInitialize(rootPanel, selectionPanel);

		cacheListener = new CacheListener<E>() {

			public void onStartRefresh(Cache<E> cache) {
				listView.clear();
				listView.setWaitStatus();
			}

			public void onRefresh(Cache<E> cache, Throwable failure) {
				listView.clearStatus();
				listView.refresh();
				refreshContext(ContextManager.getContext());
			}

			public void onItemUpdate(Cache<E> cache, E item, Status status,
					Throwable failure) {
				cache.refresh();

				if ((failure == null) && status.isSuccess()) {
					Context.createWithUuid(item).submit();
				} else if (!status.isSuccess()) {
					Window.alert("Save rejected: " + status.getMessage());
				} else if (failure != null) {
					Window.alert("Save failed: " + failure.getMessage());
				}
			}

		};

	}

	protected abstract void onInitialize(DockPanel rootPanel,
			VerticalPanel selectionPanel);

	@Override
	protected void onDeactivate() {
		cache.removeListener(cacheListener);
	}

	@Override
	protected void onUpdate(Context context) {
		if (!isActive()) {
			cache.addListener(cacheListener);
			cache.refresh();
		} else {
			refreshContext(context);
		}
	}

	protected final C getCache() {
		return cache;
	}

	protected final E getSelection() {
		return selection;
	}

	protected final VerticalPanel getSelectionPanel() {
		return selectionPanel;
	}

	protected final void addAction(Widget actionUI) {
		if (westPanel.getWidgetIndex(actionBlock) == -1) {
			westPanel.insert(actionBlock, 0);
		}
		actionBlock.addItem(actionUI);
	}

	private void refreshContext(Context context) {
		final String uuid = context.getUuid();
		if (LangUtil.notEmpty(uuid)) {
			selection = cache.getItem(uuid);
			if (selection != null) {
				listView.setSelection(selection);
			}
			onSelectionChanged(selection);
		} else if (cache.getItemCount() > 0) {
			Context.createWithUuid(cache.getItem(0)).submit();
		}
	}

	protected abstract void onSelectionChanged(E item);

	protected abstract String getItemText(E item);

	protected Widget getItemDecorator(E item) {
		return null;
	}

	protected abstract boolean isMatch(E item, String query);

	private class ActionBlock extends BlockPanel {

		@Override
		protected void onInitialize(VerticalPanel contentPanel) {
			// nothing to do
		}

		public void addItem(Widget w) {
			w.setWidth("100%");
			getContentPanel().add(w);
		}
	}

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
		protected Widget getItemDecorator(E item) {
			return ListContentComposite.this.getItemDecorator(item);
		};

		@Override
		protected void doItemClick(E item) {
			Context.createWithUuid(item).submit();
		}

	}

}
