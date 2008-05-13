package com.surelogic.sierra.gwt.client.rules;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.surelogic.sierra.gwt.client.Context;
import com.surelogic.sierra.gwt.client.data.Cache;
import com.surelogic.sierra.gwt.client.data.CacheListener;
import com.surelogic.sierra.gwt.client.data.Cacheable;
import com.surelogic.sierra.gwt.client.data.Category;
import com.surelogic.sierra.gwt.client.data.FilterEntry;
import com.surelogic.sierra.gwt.client.ui.SectionPanel;

public class SearchResultsSection extends SectionPanel {
	private final CategoryCache categories;
	private final Map searchResultsData = new HashMap();
	private String searchText;

	public SearchResultsSection(CategoryCache categories) {
		super();
		this.categories = categories;
	}

	protected void onInitialize(VerticalPanel contentPanel) {
		setTitle("Results");
		categories.addListener(new CacheListener() {

			public void onStartRefresh(Cache cache) {
				clearResults();
				setWaitStatus();
			}

			public void onRefresh(Cache cache, Throwable failure) {
				if (failure == null) {
					refreshResults();
				} else {
					clearResults();
					setErrorStatus("Error retrieving categories");
				}
			}

			public void onItemUpdate(Cache cache, Cacheable item,
					Throwable failure) {
				// TODO Auto-generated method stub

			}
		});
	}

	protected void onActivate(Context context) {
		search("");
		updateSelectionUI(context);
	}

	protected void onUpdate(Context context) {
		updateSelectionUI(context);
	}

	protected void onDeactivate() {
		clearResults();
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
		for (final Iterator it = categories.getItemIterator(); it.hasNext();) {
			final Category cat = (Category) it.next();
			if (cat.getName().toLowerCase().indexOf(query) >= 0) {
				addSearchCategory(cat);
			} else if (!"".equals(text)) {
				boolean categoryAdded = false;
				for (final Iterator i = cat.getEntries().iterator(); i
						.hasNext();) {
					final FilterEntry e = (FilterEntry) i.next();
					if (e.getName().toLowerCase().indexOf(query) >= 0) {
						if (!categoryAdded) {
							addSearchCategory(cat);
							categoryAdded = true;
						}
						addSearchFinding(e);
					}
				}
			}
		}
		clearStatus();
	}

	private void clearResults() {
		getContentPanel().clear();
		searchResultsData.clear();
	}

	private void refreshResults() {
		search(searchText);
	}

	private void addSearchCategory(Category cat) {
		final Label catEntry = new Label(cat.getName());
		catEntry.addClickListener(new SearchResultListener(cat));
		searchResultsData.put(cat, catEntry);
		getContentPanel().add(catEntry);
	}

	private void addSearchFinding(FilterEntry finding) {
		final Label findingEntry = new Label(finding.getName());
		findingEntry.addClickListener(new SearchResultListener(finding));
		searchResultsData.put(finding, findingEntry);
		getContentPanel().add(findingEntry);
	}

	private void updateSelectionUI(Context context) {
		// TODO need to update the search results UI to show which item is
		// selected

		// final String categoryUuid = new RulesContext(context).getCategory();
		// if (LangUtil.notEmpty(categoryUuid)) {
		// final Category cat = (Category) categories.getItem(categoryUuid);
		//
		// }
	}

	private class SearchResultListener implements ClickListener {
		private final Category category;
		private final FilterEntry finding;

		public SearchResultListener(Category category) {
			super();
			this.category = category;
			finding = null;
		}

		public SearchResultListener(FilterEntry finding) {
			super();
			category = null;
			this.finding = finding;
		}

		public void onClick(Widget sender) {
			if (category != null) {
				new RulesContext(category).updateContext();
			} else {
				new RulesContext(finding).updateContext();
			}
		}

	}
}
