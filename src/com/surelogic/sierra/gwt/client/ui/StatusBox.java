package com.surelogic.sierra.gwt.client.ui;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.surelogic.sierra.gwt.client.data.Status;

public class StatusBox extends Composite {

	private final HTML html = new HTML();

	private Status status;

	public StatusBox() {
		super();
		initWidget(html);
	}

	public StatusBox(Status status) {
		super();
		initWidget(html);
		setStatus(status);
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		if (status.isSuccess()) {
			html.setHTML("<span class=\"success\">" + status.getMessage()
					+ "</span>");
		} else {
			html.setHTML("<span class=\"error\">" + status.getMessage()
					+ "</span>");
		}
		this.status = status;
	}

	public void clear() {
		status = null;
		html.setHTML("");
	}

}
