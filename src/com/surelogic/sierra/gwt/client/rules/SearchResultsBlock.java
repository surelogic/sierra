package com.surelogic.sierra.gwt.client.rules;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.surelogic.sierra.gwt.client.data.Category;
import com.surelogic.sierra.gwt.client.ui.BlockPanel;
import com.surelogic.sierra.gwt.client.ui.ItemLabel;
import com.surelogic.sierra.gwt.client.ui.SelectionTracker;

public class SearchResultsBlock extends BlockPanel {
	public static final String PRIMARY_STYLE = "categories-search";
	private final CategoryCache categories;
	private final SelectionTracker selectionTracker = new SelectionTracker();
	private final Map<String, ItemLabel> searchResultsData = new HashMap<String, ItemLabel>();
	private String searchText;
	private Category currentSelection;

	public SearchResultsBlock(CategoryCache categories) {
		super();
		this.categories = categories;
	}

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
		for (Category cat : categories) {
			if (cat.getName().toLowerCase().indexOf(query) >= 0) {
				addSearchCategory(cat);
			}
		}
		clearStatus();
		setSelection(currentSelection);
	}

	public void clearResults() {
		getContentPanel().clear();
		searchResultsData.clear();
		selectionTracker.setSelected(null);
	}

	public void refreshResults() {
		search(searchText);
	}

	public void setSelection(Category cat) {
		currentSelection = cat;
		if (cat == null) {
			selectionTracker.setSelected(null);
		} else {
			ItemLabel catEntry = searchResultsData.get(cat.getUuid());
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
