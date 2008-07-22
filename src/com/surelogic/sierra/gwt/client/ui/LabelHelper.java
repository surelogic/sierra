package com.surelogic.sierra.gwt.client.ui;

import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Label;

public final class LabelHelper {

	private LabelHelper() {
		// singleton
	}

	public static Label italics(Label label) {
		label.addStyleName("font-italic");
		return label;
	}

	public static Label clickable(Label label, ClickListener listener) {
		label.addStyleName("clickable");
		label.addClickListener(listener);
		return label;
	}
}
