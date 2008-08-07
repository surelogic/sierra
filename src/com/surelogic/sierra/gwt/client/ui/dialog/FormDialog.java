package com.surelogic.sierra.gwt.client.ui.dialog;

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
	private final HorizontalPanel buttonPanel = new HorizontalPanel();
	private final Button okButton = new Button("Ok");
	private final Label errorMessage = new Label("");
	private boolean initialized;
	private Status status;

	public FormDialog(String title, String width) {
		super();
		setText(title);
		if (width != null) {
			setWidth(width);
		}

		rootPanel.setWidth("100%");

		errorMessage.setWidth("100%");
		errorMessage.addStyleName("error");

		contentTable.setWidth("100%");
		rootPanel.add(contentTable);

		buttonPanel.setWidth("100%");
		buttonPanel.addStyleName("sl-FormDialog-buttonpanel");

		final HorizontalPanel rightButtons = new HorizontalPanel();

		okButton.addClickListener(new ClickListener() {

			public void onClick(Widget sender) {
				doOkClick();
			}
		});
		rightButtons.add(okButton);
		final Button cancel = new Button("Cancel");
		cancel.addClickListener(new ClickListener() {

			public void onClick(Widget sender) {
				hide();
			}
		});
		rightButtons.add(cancel);
		buttonPanel.add(rightButtons);
		buttonPanel.setCellHorizontalAlignment(rightButtons,
				HorizontalPanel.ALIGN_RIGHT);
		buttonPanel.setCellVerticalAlignment(rightButtons,
				HorizontalPanel.ALIGN_MIDDLE);
		rootPanel.add(buttonPanel);
		setWidget(rootPanel);
	}

	@Override
	public final void show() {
		if (!initialized) {
			initialized = true;
			doInitialize(contentTable);
		}

		super.show();

		final HasFocus w = getInitialFocus();
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
		inputUI.setWidth("100%");
		contentTable.setWidget(nextRowIndex, 1, inputUI);
	}

	protected final void setOkEnabled(boolean enabled) {
		okButton.setEnabled(enabled);
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

	protected void doOkClick() {
		setStatus(Status.success());
		hide();
	}
}
