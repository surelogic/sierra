package com.surelogic.sierra.gwt.client.rules;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.KeyboardListenerAdapter;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.surelogic.sierra.gwt.client.Context;
import com.surelogic.sierra.gwt.client.ui.SectionPanel;
import com.surelogic.sierra.gwt.client.ui.StyledButton;

public class SearchSection extends SectionPanel {
	private final CategoryCache categories;
	private final FlexTable grid = new FlexTable();
	private final TextBox searchText = new TextBox();
	private final SearchResultsBlock results;

	public SearchSection(CategoryCache categories) {
		super();
		this.categories = categories;
		this.results = new SearchResultsBlock(this.categories);
	}

	protected void onInitialize(VerticalPanel contentPanel) {
		contentPanel.add(grid);

		grid.setWidth("100%");
		grid.getColumnFormatter().setWidth(0, "25%");
		grid.getColumnFormatter().setWidth(1, "75%");

		final StyledButton createCategoryLabel = new StyledButton(
				"Create a Category", new ClickListener() {

					public void onClick(Widget sender) {
						createCategory();
					}
				});
		grid.setWidget(0, 0, createCategoryLabel);
		grid.getFlexCellFormatter().setColSpan(0, 0, 2);
		final Label searchLabel = new Label("Search");
		grid.setWidget(1, 0, searchLabel);
		grid.setWidget(1, 1, searchText);
		searchText.setWidth("100%");

		results.initialize();
		results.setSubsectionStyle(true);
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

	protected void onUpdate(Context context) {
		if (!isActive()) {
			results.search("");
		} else {
			results.updateSelectionUI(context);
		}
	}

	protected void onDeactivate() {
		results.clearResults();
	}

	private void createCategory() {
		// TODO finish
		Window.alert("Create category");
	}
}
