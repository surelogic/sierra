package com.surelogic.sierra.gwt.client.ui;

import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Label;

public final class LabelHelper {

	private LabelHelper() {
		// singleton
	}

	public static Label italics(final Label label) {
		label.addStyleName("font-italic");
		return label;
	}

	public static Label strong(final Label label) {
		label.addStyleName("font-strong");
		return label;
	}

	public static Label clickable(final Label label) {
		label.addStyleName("clickable");
		return label;
	}

	public static Label clickable(final Label label,
			final ClickListener listener) {
		label.addStyleName("clickable");
		label.addClickListener(listener);
		return label;
	}
}
