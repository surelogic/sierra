package com.surelogic.sierra.gwt.client.rules;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.surelogic.sierra.gwt.client.Context;
import com.surelogic.sierra.gwt.client.ContextManager;
import com.surelogic.sierra.gwt.client.data.Cache;
import com.surelogic.sierra.gwt.client.data.CacheListener;
import com.surelogic.sierra.gwt.client.data.Cacheable;
import com.surelogic.sierra.gwt.client.data.Category;
import com.surelogic.sierra.gwt.client.data.Status;
import com.surelogic.sierra.gwt.client.ui.BlockPanel;
import com.surelogic.sierra.gwt.client.ui.ItemLabel;
import com.surelogic.sierra.gwt.client.ui.SelectionTracker;
import com.surelogic.sierra.gwt.client.util.LangUtil;

public class SearchResultsBlock extends BlockPanel {
	public static final String PRIMARY_STYLE = "categories-search";
	private final CategoryCache categories;
	private final SelectionTracker selectionTracker = new SelectionTracker();
	private final Map searchResultsData = new HashMap();
	private String searchText;

	public SearchResultsBlock(CategoryCache categories) {
		super();
		this.categories = categories;
	}

	protected void onInitialize(VerticalPanel contentPanel) {
		setTitle(" ");
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
					Status status, Throwable failure) {
				// TODO Auto-generated method stub

			}
		});
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
			}
		}
		clearStatus();
		updateSelectionUI(ContextManager.getContext());
	}

	public void clearResults() {
		getContentPanel().clear();
		searchResultsData.clear();
		selectionTracker.setSelected(null);
	}

	public void refreshResults() {
		search(searchText);
	}

	public void updateSelectionUI(Context context) {
		final CategoriesContext rulesCtx = new CategoriesContext(context);
		if (LangUtil.notEmpty(rulesCtx.getCategory())) {
			ItemLabel catEntry = (ItemLabel) searchResultsData.get(rulesCtx
					.getCategory());
			if (catEntry != null) {
				catEntry.setSelected(true);
			}
		}
	}

	private void addSearchCategory(Category cat) {
		final ItemLabel catEntry = new ItemLabel(cat.getName(), cat,
				selectionTracker, new SearchResultListener(cat));
		searchResultsData.put(cat.getUuid(), catEntry);
		getContentPanel().add(catEntry);
	}

	private class SearchResultListener implements ClickListener {
		private final Category category;

		public SearchResultListener(Category category) {
			super();
			this.category = category;
		}

		public void onClick(Widget sender) {
			new CategoriesContext(category).updateContext();
		}

	}
}
