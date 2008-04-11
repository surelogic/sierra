package com.surelogic.sierra.gwt.client.rules;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.KeyboardListenerAdapter;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.surelogic.sierra.gwt.client.ContentComposite;
import com.surelogic.sierra.gwt.client.Context;
import com.surelogic.sierra.gwt.client.data.Category;
import com.surelogic.sierra.gwt.client.data.FilterEntry;
import com.surelogic.sierra.gwt.client.service.ServiceHelper;
import com.surelogic.sierra.gwt.client.util.ExceptionTracker;
import com.surelogic.sierra.gwt.client.util.ImageHelper;
import com.surelogic.sierra.gwt.client.util.LangUtil;
import com.surelogic.sierra.gwt.client.util.UI;

public class RulesContent extends ContentComposite {
	public static final String PRIMARY_STYLE = "rules";
	private static final RulesContent instance = new RulesContent();
	private final VerticalPanel searchPanel = new VerticalPanel();
	private final FlexTable searchHeaderGrid = new FlexTable();
	private final TextBox searchText = new TextBox();
	private final VerticalPanel searchResults = new VerticalPanel();
	private final Map searchResultsData = new HashMap();
	// List of all Category objects
	private List categories;
	private final VerticalPanel detailsPanel = new VerticalPanel();
	private final CategoryPanel categoryPanel = new CategoryPanel();
	private Label currentSearchSelection;

	public static RulesContent getInstance() {
		return instance;
	}

	private RulesContent() {
		// no instances
	}

	protected void onInitialize(DockPanel rootPanel) {
		final Label title = UI.h2("Rules");
		rootPanel.add(title, DockPanel.NORTH);
		rootPanel.setCellHorizontalAlignment(title, DockPanel.ALIGN_LEFT);

		searchPanel.addStyleName(PRIMARY_STYLE + "-search-panel");
		searchHeaderGrid.addStyleName(PRIMARY_STYLE + "-search-header");
		searchText.setWidth("100%");
		searchHeaderGrid.setWidget(0, 0, searchText);
		final Label searchTitle = new Label("Search");
		searchTitle.addStyleName(PRIMARY_STYLE + "-search-title");
		searchHeaderGrid.setWidget(0, 1, searchTitle);
		searchHeaderGrid.setText(0, 2, "");
		searchPanel.add(searchHeaderGrid);

		searchResults.setWidth("100%");
		searchPanel.add(searchResults);
		final Label searchFooter = new Label("");
		searchFooter.addStyleName(PRIMARY_STYLE + "-search-footer");
		searchPanel.add(searchFooter);
		searchPanel.setCellWidth(searchFooter, "100%");

		detailsPanel.addStyleName(PRIMARY_STYLE + "-details-panel");
		detailsPanel.add(new Label(""));

		rootPanel.add(searchPanel, DockPanel.WEST);
		rootPanel.setCellWidth(searchPanel, "25%");
		rootPanel.add(detailsPanel, DockPanel.CENTER);
		rootPanel.setCellWidth(detailsPanel, "75%");

		searchText.addKeyboardListener(new KeyboardListenerAdapter() {
			public void onKeyUp(Widget sender, char keyCode, int modifiers) {
				search(searchText.getText());
			}
		});
		searchText.addChangeListener(new ChangeListener() {
			public void onChange(Widget sender) {
				search(searchText.getText());
			}
		});
	}

	protected void onActivate(Context context) {
		clearSearch();

		final VerticalPanel waitPanel = new VerticalPanel();
		waitPanel.setWidth("100%");
		waitPanel.addStyleName(PRIMARY_STYLE + "-search-category");
		waitPanel.add(ImageHelper.getWaitImage(16));
		searchResults.add(waitPanel);
		ServiceHelper.getSettingsService().getCategories(new AsyncCallback() {

			public void onFailure(Throwable caught) {
				// TODO handle this in the normal way, or switch to Callback
				clearSearch();
				searchResults.add(new Label("Error retrieving categories"));
				ExceptionTracker.logException(caught);
			}

			public void onSuccess(Object result) {
				categories = (List) result;
				search("");
				searchText.setFocus(true);
				if (!categories.isEmpty()) {
					selectCategory((Category) categories.get(0));
				}
			}
		});

	}

	protected boolean onDeactivate() {
		return true;
	}

	private void clearSearch() {
		searchResults.clear();
		searchResultsData.clear();
	}

	private void search(String text) {
		clearSearch();
		searchHeaderGrid.setWidget(0, 2, ImageHelper.getWaitImage(16));
		final StringBuffer queryBuf = new StringBuffer();
		for (int i = 0; i < text.length(); i++) {
			final char ch = text.charAt(i);
			if (Character.isLetterOrDigit(ch)) {
				queryBuf.append(Character.toLowerCase(ch));
			}
		}
		final String query = queryBuf.toString();
		for (final Iterator it = categories.iterator(); it.hasNext();) {
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
		searchHeaderGrid.setText(0, 2, "");
	}

	private void addSearchCategory(Category cat) {
		final Label catEntry = new Label(cat.getName());
		catEntry.addStyleName(PRIMARY_STYLE + "-search-category");
		catEntry.addClickListener(new SearchResultListener(cat));
		searchResultsData.put(cat, catEntry);
		searchResults.add(catEntry);
	}

	private void addSearchFinding(FilterEntry finding) {
		final Label findingEntry = new Label(finding.getName());
		findingEntry.addStyleName(PRIMARY_STYLE + "-search-finding");
		findingEntry.addClickListener(new SearchResultListener(finding));
		searchResultsData.put(finding, findingEntry);
		searchResults.add(findingEntry);
	}

	private void selectCategory(Category cat) {
		updateSelectionStyle(cat);

		categoryPanel.setCategory(cat);
		if (detailsPanel.getWidgetIndex(categoryPanel) == -1) {
			detailsPanel.clear();
			detailsPanel.add(categoryPanel);
		}
	}

	private void selectFinding(FilterEntry finding) {
		updateSelectionStyle(finding);

		// TODO redirect to finding page or something
		Window.alert("Finding selected: " + finding.getName());
	}

	private void updateSelectionStyle(Object searchData) {
		final String selectedStyle = PRIMARY_STYLE + "-search-selected";
		if (!LangUtil.equals(searchData, currentSearchSelection)) {
			if (currentSearchSelection != null) {
				currentSearchSelection.removeStyleName(selectedStyle);
			}

			currentSearchSelection = (Label) searchResultsData.get(searchData);
			if (currentSearchSelection != null) {
				currentSearchSelection.addStyleName(selectedStyle);
			}
		}
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
				selectCategory(category);
			} else {
				selectFinding(finding);
			}
		}

	}
}
