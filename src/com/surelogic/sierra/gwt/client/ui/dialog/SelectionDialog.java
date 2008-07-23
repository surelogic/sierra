package com.surelogic.sierra.gwt.client.ui.dialog;

import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasFocus;
import com.google.gwt.user.client.ui.KeyboardListenerAdapter;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public abstract class SelectionDialog extends FormDialog {
	private final TextBox searchBox = new TextBox();

	@Override
	protected void doInitialize(FlexTable contentTable) {
		final FlexTable searchTable = new FlexTable();
		searchTable.setWidth("50%");
		searchTable.setText(0, 0, "Search");
		searchBox.setWidth("100%");
		searchTable.setWidget(0, 1, searchBox);
		searchTable.getColumnFormatter().setWidth(0, "25%");
		searchTable.getColumnFormatter().setWidth(1, "75%");
		contentTable.setWidget(0, 0, searchTable);

		final Widget itemsUI = doDialogInitialize();
		final ScrollPanel itemsScroller = new ScrollPanel(itemsUI);
		itemsScroller.setSize("100%", "auto");
		contentTable.setWidget(1, 0, itemsScroller);

		searchBox.addKeyboardListener(new KeyboardListenerAdapter() {
			@Override
			public void onKeyUp(Widget sender, char keyCode, int modifiers) {
				search(searchBox.getText());
			}
		});
		searchBox.addChangeListener(new ChangeListener() {
			public void onChange(Widget sender) {
				search(searchBox.getText());
			}
		});
	}

	@Override
	protected HasFocus getInitialFocus() {
		return null;
	}

	protected abstract Widget doDialogInitialize();

	protected abstract void search(String text);

}
