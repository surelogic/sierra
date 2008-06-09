package com.surelogic.sierra.gwt.client.content.categories;

import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.KeyboardListenerAdapter;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.surelogic.sierra.gwt.client.data.Category;
import com.surelogic.sierra.gwt.client.ui.BlockPanel;

public class SearchBlock extends BlockPanel {
	private final CategoryCache categories;
	private final FlexTable grid = new FlexTable();
	private final TextBox searchText = new TextBox();
	private final SearchResultsBlock results;
	private Category currentCategory;

	public SearchBlock(CategoryCache categories) {
		super();
		this.categories = categories;
		this.results = new SearchResultsBlock(this.categories);
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
	}

	public void startRefresh() {
		results.clearResults();
		results.setWaitStatus();
	}

	public void refresh() {
		results.search(searchText.getText());
		results.setSelection(currentCategory);
	}

	public void clear() {
		results.clearResults();
	}

	public void setSelection(Category cat) {
		currentCategory = cat;
		results.setSelection(currentCategory);
	}

}
