package com.surelogic.sierra.gwt.client.ui.panel;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.KeyboardListenerAdapter;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class SearchInputPanel extends Composite {
	private final FlexTable rootTable = new FlexTable();
	private final TextBox searchBox = new TextBox();
	private final List<SearchListener> listeners = new ArrayList<SearchListener>();

	public SearchInputPanel() {
		super();
		initWidget(rootTable);

		rootTable.setWidth("100%");
		rootTable.setText(0, 0, "Search");

		searchBox.setWidth("100%");
		rootTable.setWidget(0, 1, searchBox);

		rootTable.getColumnFormatter().setWidth(0, "25%");
		rootTable.getColumnFormatter().setWidth(1, "75%");

		searchBox.addKeyboardListener(new KeyboardListenerAdapter() {
			@Override
			public void onKeyUp(Widget sender, char keyCode, int modifiers) {
				fireSearch(searchBox.getText());
			}
		});
		searchBox.addChangeListener(new ChangeListener() {
			public void onChange(Widget sender) {
				fireSearch(searchBox.getText());
			}
		});
	}

	public TextBox getSearchBox() {
		return searchBox;
	}

	public String getSearchText() {
		return searchBox.getText();
	}

	public void addListener(SearchListener listener) {
		listeners.add(listener);
	}

	public void removeListener(SearchListener listener) {
		listeners.remove(listener);
	}

	private void fireSearch(String text) {
		for (final SearchListener listener : listeners) {
			listener.onSearch(this, text);
		}
	}

	public static interface SearchListener {

		void onSearch(SearchInputPanel sender, String text);

	}

}
