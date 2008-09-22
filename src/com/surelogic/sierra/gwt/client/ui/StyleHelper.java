package com.surelogic.sierra.gwt.client.ui;

import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.SourcesClickEvents;
import com.google.gwt.user.client.ui.Widget;

public final class StyleHelper {

	private StyleHelper() {
		// singleton
	}

	public static <T extends Widget> T italics(final T w) {
		w.addStyleName("font-italic");
		return w;
	}

	public static <T extends Widget> T gray(final T w) {
		w.addStyleName("font-gray");
		return w;
	}

	public static <T extends Widget> T ungray(final T w) {
		w.removeStyleName("font-gray");
		return w;
	}

	public static <T extends Widget> T strong(final T w) {
		w.addStyleName("font-strong");
		return w;
	}

	public static <T extends Widget> T clickable(final T w) {
		w.addStyleName("clickable");
		return w;
	}

	public static <T extends Widget & SourcesClickEvents> T clickable(
			final T label, final ClickListener listener) {
		label.addStyleName("clickable");
		label.addClickListener(listener);
		return label;
	}
}
