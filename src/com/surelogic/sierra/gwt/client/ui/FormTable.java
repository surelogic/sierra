package com.surelogic.sierra.gwt.client.ui;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.surelogic.sierra.gwt.client.ui.panel.ActionPanel;

public class FormTable extends Composite {
	private final VerticalPanel rootPanel = new VerticalPanel();
	private final FlexTable contentTable = new FlexTable();
	private final Label errorMessage = new Label("");
	private final ActionPanel actionPanel = new ActionPanel();

	public FormTable(String okButtonText, final Command okCmd,
			final Command cancelCmd) {
		super();
		initWidget(rootPanel);
		rootPanel.setWidth("100%");

		errorMessage.setWidth("100%");
		errorMessage.addStyleName("error");

		contentTable.setWidth("100%");
		rootPanel.add(contentTable);

		actionPanel.addAction(okButtonText, new ClickListener() {

			public void onClick(Widget sender) {
				okCmd.execute();
			}
		});
		actionPanel.addAction("Cancel", new ClickListener() {

			public void onClick(Widget sender) {
				cancelCmd.execute();
			}
		});

		rootPanel.add(actionPanel);
		rootPanel.setCellHorizontalAlignment(actionPanel,
				VerticalPanel.ALIGN_RIGHT);
	}

	public final void addField(String title, Widget inputUI) {
		addField(new Label(title, false), inputUI);
	}

	public final void addField(Widget inputTitle, Widget inputUI) {
		final int nextRowIndex = contentTable.getRowCount();
		contentTable.setWidget(nextRowIndex, 0, inputTitle);
		inputUI.setWidth("100%");
		contentTable.setWidget(nextRowIndex, 1, inputUI);
	}

	public final void setErrorMessage(String text) {
		if (rootPanel.getWidgetIndex(errorMessage) == -1) {
			rootPanel.insert(errorMessage, 0);
		}
		errorMessage.setText(text);
	}

	public final void clearErrorMessage() {
		rootPanel.remove(errorMessage);
	}

	public final void setWaitStatus() {
		actionPanel.setWaitStatus();
	}

	public final void clearWaitStatus() {
		actionPanel.clearWaitStatus();
	}
}
