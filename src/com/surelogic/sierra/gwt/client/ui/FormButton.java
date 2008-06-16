package com.surelogic.sierra.gwt.client.ui;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public abstract class FormButton extends Composite {
	private final VerticalPanel rootPanel = new VerticalPanel();
	private final FormTable form;

	public FormButton(String title, String okButtonTitle) {
		super();
		initWidget(rootPanel);
		rootPanel.add(new StyledButton(title, new ClickListener() {

			public void onClick(Widget sender) {
				setOpen(!isOpen());
			}
		}));

		form = new FormTable(okButtonTitle, new Command() {

			public void execute() {
				doOkClick();
			}
		}, new Command() {

			public void execute() {
				rootPanel.remove(form);
			}
		});
	}

	protected FormTable getForm() {
		return form;
	}

	protected void setOpen(boolean open) {
		final int formIndex = rootPanel.getWidgetIndex(form);
		if (open) {
			if (formIndex == -1) {
				rootPanel.add(form);
			}
			onOpen();
		} else if (!open && formIndex >= 0) {
			rootPanel.remove(form);
		}

	}

	protected boolean isOpen() {
		return rootPanel.getWidgetIndex(form) >= 0;
	}

	protected void setWaitStatus() {
		form.setWaitStatus();
	}

	protected void clearWaitStatus() {
		form.clearWaitStatus();
	}

	protected abstract void onOpen();

	protected abstract void doOkClick();

}
