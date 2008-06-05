package com.surelogic.sierra.gwt.client.ui;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasFocus;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.surelogic.sierra.gwt.client.data.Status;

public abstract class FormDialog extends DialogBox {
	private final VerticalPanel rootPanel = new VerticalPanel();
	private final FlexTable contentTable = new FlexTable();
	private final Label errorMessage = new Label("");
	private boolean initialized;
	private Status status;

	public FormDialog() {
		super();

		errorMessage.setWidth("100%");
		errorMessage.addStyleName("error");

		contentTable.setWidth("100%");
		rootPanel.add(contentTable);

		final HorizontalPanel buttonPanel = new HorizontalPanel();
		final Button ok = new Button("Ok");
		ok.addClickListener(new ClickListener() {

			public void onClick(Widget sender) {
				doOkClick();
			}
		});
		buttonPanel.add(ok);
		final Button cancel = new Button("Cancel");
		cancel.addClickListener(new ClickListener() {

			public void onClick(Widget sender) {
				hide();
			}
		});
		buttonPanel.add(cancel);

		rootPanel.add(buttonPanel);
		rootPanel.setCellHorizontalAlignment(buttonPanel,
				VerticalPanel.ALIGN_RIGHT);
		setWidget(rootPanel);
	}

	@Override
	public final void show() {
		if (!initialized) {
			initialized = true;
			doInitialize(contentTable);
		}

		super.show();

		HasFocus w = getInitialFocus();
		if (w != null) {
			w.setFocus(true);
		}
	}

	public final Status getStatus() {
		return status;
	}

	public final void setStatus(Status status) {
		this.status = status;
	}

	protected final void addField(String title, Widget inputUI) {
		addField(new Label(title, false), inputUI);
	}

	protected final void addField(Widget inputTitle, Widget inputUI) {
		final int nextRowIndex = contentTable.getRowCount();
		contentTable.setWidget(nextRowIndex, 0, inputTitle);
		contentTable.setWidget(nextRowIndex, 1, inputUI);
	}

	protected final void setErrorMessage(String text) {
		if (rootPanel.getWidgetIndex(errorMessage) == -1) {
			rootPanel.insert(errorMessage, 0);
		}
		errorMessage.setText(text);
	}

	protected final void clearErrorMessage() {
		rootPanel.remove(errorMessage);
	}

	protected abstract void doInitialize(FlexTable contentTable);

	protected abstract HasFocus getInitialFocus();

	protected abstract void doOkClick();
}
