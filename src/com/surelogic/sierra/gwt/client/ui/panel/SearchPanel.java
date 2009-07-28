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
import com.surelogic.sierra.gwt.client.ui.ItemWidget;
import com.surelogic.sierra.gwt.client.ui.link.ContentLink;
import com.surelogic.sierra.gwt.client.ui.panel.PagingPanel.PageListener;
import com.surelogic.sierra.gwt.client.ui.panel.SearchInputPanel.SearchListener;

public abstract class SearchPanel<E extends Cacheable, T extends Cache<E>>
		extends BasicPanel {
	private static final int ITEMS_PER_PAGE = 25;
	private static final String STYLE = "sl-SearchPanel";
	private final T cache;
	private final SearchInputPanel searchPanel = new SearchInputPanel();
	private final SearchResultsPanel results = new SearchResultsPanel();
	private E selection;

	public SearchPanel(final T cache) {
		super();
		this.cache = cache;
		addStyleName(STYLE);
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
	}

	public void setSelection(final E item) {
		selection = item;
		results.setSelection(item);
	}

	protected abstract ContentComposite getItemContent();

	protected abstract boolean isItemVisible(E item, String searchText);

	protected abstract String getItemText(E item);

	protected abstract Widget getItemDecorator(E item);
		
	static final boolean lazyCreatePanelItem = true;
	
	private class SearchResultsPanel extends BasicPanel {		
		private final List<E> searchResults = new ArrayList<E>();
		private final List<PanelItem> searchResultsData = new ArrayList<PanelItem>();
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
						PanelItem item = searchResultsData.get(itemIndex);
						if (lazyCreatePanelItem && item == null) {
							item = createPanelItem(searchResults.get(itemIndex));
							searchResultsData.set(itemIndex, item);
						}
						getContentPanel().add(item);
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
			int i = 0;
			for (final E item : cache) {
				if (isItemVisible(item, query)) {
					searchResults.add(item);
					if (lazyCreatePanelItem && i >= ITEMS_PER_PAGE) { 
						searchResultsData.add(null);
					} else {							
						searchResultsData.add(createPanelItem(item));
					}
					i++;
				}
			}
			if (searchResults.isEmpty()) {
				getContentPanel().add(new HTML("No matches found."));
			}

			clearStatus();
			setSelection(selection);
		}
		
		private PanelItem createPanelItem(E item) {
			final String itemText = getItemText(item);
			final ContentLink itemUI = new ContentLink(itemText,
					getItemContent(), item.getUuid());
			itemUI.setWidth(getOffsetWidth() + "px");
			
			final Widget decorator = getItemDecorator(item);
			final Widget widget;
			if (decorator != null) {
				final HorizontalPanel itemPanel = new HorizontalPanel();
				itemPanel.setWidth("100%");				
				itemPanel.add(itemUI);
				itemPanel.add(decorator);
				itemPanel.setCellHorizontalAlignment(decorator,
						HorizontalPanel.ALIGN_RIGHT);
				widget = itemPanel;
			} else {
				itemUI.setWidth("100%");
				widget = itemUI;
			}
			return new PanelItem(widget, item);
		}

		public void clearResults() {
			getContentPanel().clear();
			searchResults.clear();
			searchResultsData.clear();
		}

		public void refreshResults() {
			search(searchText);
		}

		public void setSelection(final E item) {
			final int size = searchResults.size();
			final int pages = 1 + (size / ITEMS_PER_PAGE);
			final int index;
			
			// update the page index and count
			if (item != null) {

				int itemIndex = 0;			
				for(; itemIndex < size; itemIndex++) {
					if (item == searchResults.get(itemIndex)) {
						break;
					}
				}
				if (itemIndex >= size) {
					itemIndex = 0;
				}
				index = itemIndex / ITEMS_PER_PAGE;
			} else {
				index = 0;
			}
			//if (pagingPanel.getPageIndex() != index || pagingPanel.getPageCount() != pages) {
			pagingPanel.setPaging(index, pages);
			//}
		}

		private class PanelItem extends ItemWidget<Widget, E> {

			public PanelItem(final Widget ui, final E item) {
				super(ui, item);
			}

		}
	}
}
