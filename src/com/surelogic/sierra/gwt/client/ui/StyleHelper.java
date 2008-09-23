package com.surelogic.sierra.gwt.client.ui;

import com.google.gwt.user.client.ui.Widget;

/**
 * This is a utility class to programmatically add and remove styles on a
 * {@link Widget}.
 * 
 */
public final class StyleHelper {
	public static enum Style {
		ITALICS("font-italics"), GRAY("font-gray"), STRONG("font-strong"), CLICKABLE(
				"clickable");

		private final String id;

		private Style(final String id) {
			this.id = id;
		}

		public String getId() {
			return id;
		}
	}

	public static <T extends Widget> T add(final T w, final Style... styles) {
		for (final Style s : styles) {
			w.addStyleName(s.getId());
		}
		return w;
	}

	public static <T extends Widget> T remove(final T w, final Style... styles) {
		for (final Style s : styles) {
			w.removeStyleName(s.getId());
		}
		return w;
	}

	private StyleHelper() {
		// singleton
	}

}
