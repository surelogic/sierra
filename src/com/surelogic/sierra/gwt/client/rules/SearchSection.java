package com.surelogic.sierra.gwt.client.rules;

import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.KeyboardListenerAdapter;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.surelogic.sierra.gwt.client.Context;
import com.surelogic.sierra.gwt.client.ui.SectionPanel;

public class SearchSection extends SectionPanel {
	private final CategoryCache categories;
	private final FlexTable grid = new FlexTable();
	private final ListBox categoryView = new ListBox();
	private final TextBox searchText = new TextBox();
	private final SearchResultsSubsection results;

	public SearchSection(CategoryCache categories) {
		super();
		this.categories = categories;
		this.results = new SearchResultsSubsection(this.categories);
	}

	protected void onInitialize(VerticalPanel contentPanel) {
		contentPanel.add(grid);

		grid.setWidth("100%");
		grid.getColumnFormatter().setWidth(0, "25%");
		grid.getColumnFormatter().setWidth(1, "75%");

		final Label viewLabel = new Label("View");
		grid.setWidget(0, 0, viewLabel);
		grid.setWidget(0, 1, categoryView);
		categoryView.setWidth("100%");

		final Label searchLabel = new Label("Search");
		grid.setWidget(1, 0, searchLabel);
		grid.setWidget(1, 1, searchText);
		searchText.setWidth("100%");

		contentPanel.add(results);

		searchText.addKeyboardListener(new KeyboardListenerAdapter() {
			public void onKeyUp(Widget sender, char keyCode, int modifiers) {
				results.search(searchText.getText());
			}
		});
		searchText.addChangeListener(new ChangeListener() {
			public void onChange(Widget sender) {
				results.search(searchText.getText());
			}
		});
	}

	protected void onActivate(Context context) {
		results.onActivate(context);
	}

	protected void onDeactivate() {
		results.onDeactivate();
	}

	protected void onUpdate(Context context) {
		results.onUpdate(context);
	}

}
