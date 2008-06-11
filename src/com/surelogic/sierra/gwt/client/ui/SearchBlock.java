package com.surelogic.sierra.gwt.client.ui;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.KeyboardListenerAdapter;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.surelogic.sierra.gwt.client.data.Cache;
import com.surelogic.sierra.gwt.client.data.CacheListener;
import com.surelogic.sierra.gwt.client.data.Cacheable;
import com.surelogic.sierra.gwt.client.data.Status;

public abstract class SearchBlock<E extends Cacheable, T extends Cache<E>>
		extends BlockPanel {
	private final T cache;
	private final FlexTable grid = new FlexTable();
	private final TextBox searchText = new TextBox();
	private final SearchResultsBlock results = new SearchResultsBlock();
	private E selection;

	public SearchBlock(T cache) {
		super();
		this.cache = cache;
	}

	@Override
	protected void onInitialize(VerticalPanel contentPanel) {
		contentPanel.add(grid);

		grid.setWidth("100%");
		grid.getColumnFormatter().setWidth(0, "25%");
		grid.getColumnFormatter().setWidth(1, "75%");

		final Label searchLabel = new Label("Search");
		grid.setWidget(0, 0, searchLabel);
		grid.setWidget(0, 1, searchText);
		searchText.setWidth("100%");

		results.initialize();
		results.setSubsectionStyle(true);
		contentPanel.add(results);

		searchText.addKeyboardListener(new KeyboardListenerAdapter() {
			@Override
			public void onKeyUp(Widget sender, char keyCode, int modifiers) {
				results.search(searchText.getText());
			}
		});
		searchText.addChangeListener(new ChangeListener() {
			public void onChange(Widget sender) {
				results.search(searchText.getText());
			}
		});

		cache.addListener(new CacheListener<E>() {

			public void onStartRefresh(Cache<E> cache) {
				results.clearResults();
				results.setWaitStatus();
			}

			public void onItemUpdate(Cache<E> cache, E item, Status status,
					Throwable failure) {
				results.search(searchText.getText());
				results.setSelection(selection);
			}

			public void onRefresh(Cache<E> cache, Throwable failure) {
				results.search(searchText.getText());
				results.setSelection(selection);
			}

		});
	}

	public void clear() {
		results.clearResults();
	}

	public void setSelection(E item) {
		selection = item;
		results.setSelection(item);
	}

	protected abstract boolean isMatch(E item, String query);

	protected abstract String getItemText(E item);

	protected abstract void doItemClick(E item);

	private class SearchResultsBlock extends BlockPanel {

		private final SelectionTracker<ItemLabel<E>> selectionTracker = new SelectionTracker<ItemLabel<E>>();
		private final Map<String, ItemLabel<E>> searchResultsData = new HashMap<String, ItemLabel<E>>();
		private String searchText;

		@Override
		protected void onInitialize(VerticalPanel contentPanel) {
			setTitle(" ");
		}

		public void search(String text) {
			searchText = text;

			clearResults();
			setWaitStatus();
			final StringBuffer queryBuf = new StringBuffer();
			for (int i = 0; i < text.length(); i++) {
				final char ch = text.charAt(i);
				if (Character.isLetterOrDigit(ch)) {
					queryBuf.append(Character.toLowerCase(ch));
				}
			}
			final String query = queryBuf.toString();
			boolean matchFound = false;
			for (E item : cache) {
				if (isMatch(item, query)) {
					addItem(item);
					matchFound = true;
				}
			}
			if (!matchFound) {
				getContentPanel().add(new HTML("No matches found."));
			}
			clearStatus();
			setSelection(selection);
		}

		public void clearResults() {
			getContentPanel().clear();
			searchResultsData.clear();
			selectionTracker.setSelected(null);
		}

		public void refreshResults() {
			search(searchText);
		}

		public void setSelection(E item) {
			if (item == null) {
				selectionTracker.setSelected(null);
			} else {
				ItemLabel<E> itemUI = searchResultsData.get(item.getUuid());
				if (itemUI != null) {
					itemUI.setSelected(true);
				}
			}
		}

		private void addItem(E item) {
			final ItemLabel<E> itemUI = new ItemLabel<E>(getItemText(item),
					item, selectionTracker, new SearchResultListener(item));
			searchResultsData.put(item.getUuid(), itemUI);
			getContentPanel().add(itemUI);
		}

		private class SearchResultListener implements ClickListener {
			private final E item;

			public SearchResultListener(E item) {
				super();
				this.item = item;
			}

			public void onClick(Widget sender) {
				doItemClick(item);
			}

		}
	}
}
