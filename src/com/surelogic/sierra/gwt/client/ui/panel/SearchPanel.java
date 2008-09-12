package com.surelogic.sierra.gwt.client.ui.panel;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.surelogic.sierra.gwt.client.content.ContentComposite;
import com.surelogic.sierra.gwt.client.data.cache.Cache;
import com.surelogic.sierra.gwt.client.data.cache.Cacheable;
import com.surelogic.sierra.gwt.client.ui.UIItem;
import com.surelogic.sierra.gwt.client.ui.link.ContentLink;
import com.surelogic.sierra.gwt.client.ui.panel.PagingPanel.PageListener;
import com.surelogic.sierra.gwt.client.ui.panel.SearchInputPanel.SearchListener;

public abstract class SearchPanel<E extends Cacheable, T extends Cache<E>>
		extends BlockPanel {
	private static final int ITEMS_PER_PAGE = 25;
	private final T cache;
	private final SearchInputPanel searchPanel = new SearchInputPanel();
	private final SearchResultsPanel results = new SearchResultsPanel();
	private E selection;

	public SearchPanel(final T cache) {
		super();
		this.cache = cache;
	}

	@Override
	protected void onInitialize(final VerticalPanel contentPanel) {
		contentPanel.add(searchPanel);

		results.initialize();
		results.setSubsectionStyle(true);
		contentPanel.add(results);

		searchPanel.addListener(new SearchListener() {

			public void onSearch(final SearchInputPanel sender,
					final String text) {
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

	public void setSelection(final E item) {
		selection = item;
		results.setSelection(item);
	}

	protected abstract ContentComposite getItemContent();

	protected abstract boolean isItemVisible(E item, String searchText);

	protected abstract String getItemText(E item);

	protected abstract Widget getItemDecorator(E item);

	private class SearchResultsPanel extends BlockPanel {
		private final List<UIItem<HorizontalPanel, E>> searchResultsData = new ArrayList<UIItem<HorizontalPanel, E>>();
		private PagingPanel pagingPanel;
		private String searchText;

		@Override
		protected void onInitialize(final VerticalPanel contentPanel) {
			setTitle(" ");

			pagingPanel = new PagingPanel(new PageListener() {

				public void onPageChange(final PagingPanel sender,
						final int pageIndex, final int pageCount) {
					getContentPanel().clear();

					final int firstItemIndex = pagingPanel.getPageIndex()
							* ITEMS_PER_PAGE;
					for (int itemIndex = firstItemIndex; (itemIndex < (firstItemIndex + ITEMS_PER_PAGE))
							&& (itemIndex < searchResultsData.size()); itemIndex++) {
						getContentPanel().add(
								searchResultsData.get(itemIndex).getUI());
					}
				}

			});

			final DockPanel titlePanel = getTitlePanel();
			titlePanel.clear();
			titlePanel.add(pagingPanel, DockPanel.CENTER);
			pagingPanel.setVisible(false);
		}

		public void search(final String text) {
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
					final HorizontalPanel itemPanel = new HorizontalPanel();
					itemPanel.setWidth("100%");
					final ContentLink itemUI = new ContentLink(
							getItemText(item), getItemContent(), item.getUuid());
					itemPanel.add(itemUI);
					final Widget decorator = getItemDecorator(item);
					if (decorator != null) {
						itemPanel.add(decorator);
						itemPanel.setCellHorizontalAlignment(decorator,
								HorizontalPanel.ALIGN_RIGHT);
					}
					searchResultsData.add(new UIItem<HorizontalPanel, E>(
							itemPanel, item));
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
		}

		public void refreshResults() {
			search(searchText);
		}

		public void setSelection(final E item) {
			final HorizontalPanel itemUI = UIItem.findItemUI(searchResultsData,
					item);

			// update the page index and count
			if (item != null) {
				final int itemIndex = UIItem.indexOf(searchResultsData, itemUI);
				pagingPanel.setPaging(itemIndex / ITEMS_PER_PAGE,
						1 + (searchResultsData.size() / ITEMS_PER_PAGE));
			} else {
				pagingPanel.setPaging(0,
						1 + (searchResultsData.size() / ITEMS_PER_PAGE));
			}

		}

	}
}
