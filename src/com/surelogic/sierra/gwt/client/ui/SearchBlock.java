package com.surelogic.sierra.gwt.client.ui;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.surelogic.sierra.gwt.client.data.cache.Cache;
import com.surelogic.sierra.gwt.client.data.cache.Cacheable;
import com.surelogic.sierra.gwt.client.ui.PagingPanel.PageListener;
import com.surelogic.sierra.gwt.client.ui.SearchPanel.SearchListener;

public abstract class SearchBlock<E extends Cacheable, T extends Cache<E>>
		extends BlockPanel {
	private static final int ITEMS_PER_PAGE = 25;
	private final T cache;
	private final SearchPanel searchPanel = new SearchPanel();
	private final SearchResultsBlock results = new SearchResultsBlock();
	private E selection;

	public SearchBlock(T cache) {
		super();
		this.cache = cache;
	}

	@Override
	protected void onInitialize(VerticalPanel contentPanel) {
		contentPanel.add(searchPanel);

		results.initialize();
		results.setSubsectionStyle(true);
		contentPanel.add(results);

		searchPanel.addListener(new SearchListener() {

			public void onSearch(SearchPanel sender, String text) {
				results.search(text);
			}
		});
	}

	public void clear() {
		results.clearResults();
	}

	public void refresh() {
		results.search(searchPanel.getSearchText());
		results.setSelection(selection);
	}

	public void setSelection(E item) {
		selection = item;
		results.setSelection(item);
	}

	protected abstract boolean isItemVisible(E item, String searchText);

	protected abstract String getItemText(E item);

	protected abstract Widget getItemDecorator(E item);

	protected abstract void doItemClick(E item);

	private class SearchResultsBlock extends BlockPanel {
		private final SelectionTracker<ItemLabel<E>> selectionTracker = new SelectionTracker<ItemLabel<E>>();
		private final List<ItemLabel<E>> searchResultsData = new ArrayList<ItemLabel<E>>();
		private PagingPanel pagingPanel;
		private String searchText;

		@Override
		protected void onInitialize(VerticalPanel contentPanel) {
			setTitle(" ");

			pagingPanel = new PagingPanel(new PageListener() {

				public void onPageChange(PagingPanel sender, int pageIndex,
						int pageCount) {
					getContentPanel().clear();

					final int firstItemIndex = pagingPanel.getPageIndex()
							* ITEMS_PER_PAGE;
					for (int itemIndex = firstItemIndex; (itemIndex < (firstItemIndex + ITEMS_PER_PAGE))
							&& (itemIndex < searchResultsData.size()); itemIndex++) {
						getContentPanel().add(searchResultsData.get(itemIndex));
					}
				}

			});

			final DockPanel titlePanel = getTitlePanel();
			titlePanel.clear();
			titlePanel.add(pagingPanel, DockPanel.CENTER);
			pagingPanel.setVisible(false);
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
			for (final E item : cache) {
				if (isItemVisible(item, query)) {
					final ItemLabel<E> itemUI = new ItemLabel<E>(
							getItemText(item), item, new SearchResultListener(
									item));
					itemUI.setSelectionTracker(selectionTracker);
					itemUI.setDecorator(getItemDecorator(item), true);
					searchResultsData.add(itemUI);
				}
			}
			if (searchResultsData.isEmpty()) {
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
			final ItemLabel<E> itemUI = getItemUI(item);

			// update the page index and count
			if (item != null) {
				final int itemIndex = searchResultsData.indexOf(itemUI);
				pagingPanel.setPaging(itemIndex / ITEMS_PER_PAGE,
						1 + (searchResultsData.size() / ITEMS_PER_PAGE));
			} else {
				pagingPanel.setPaging(0,
						1 + (searchResultsData.size() / ITEMS_PER_PAGE));
			}

			// update the ui item selection
			if (itemUI == null) {
				selectionTracker.setSelected(null);
			} else {
				itemUI.setSelected(true);
			}
		}

		private ItemLabel<E> getItemUI(E item) {
			for (final ItemLabel<E> nextItem : searchResultsData) {
				if (nextItem.getItem().equals(item)) {
					return nextItem;
				}
			}
			return null;
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
