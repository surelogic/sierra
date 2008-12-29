package com.surelogic.sierra.gwt.client.ui.dialog;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public abstract class FormDialog extends OkCancelDialog {
	private final FlexTable contentTable = new FlexTable();

	public FormDialog(final String title, final String width) {
		super(title, width);
	}

	@Override
	protected final void doInitialize(final VerticalPanel contentPanel) {
		contentTable.setWidth("100%");
		contentPanel.add(contentTable);
		doInitialize(contentTable);
	}

	protected abstract void doInitialize(FlexTable contentTable);

	protected final void addField(final String title, final Widget inputUI) {
		addField(new Label(title, false), inputUI);
	}

	protected final void addField(final Widget inputTitle, final Widget inputUI) {
		final int nextRowIndex = contentTable.getRowCount();
		contentTable.setWidget(nextRowIndex, 0, inputTitle);
		inputUI.setWidth("100%");
		contentTable.setWidget(nextRowIndex, 1, inputUI);
	}

}
