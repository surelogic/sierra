package com.surelogic.sierra.gwt.client.ui;

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

public final class UIUtil {

	public static Widget createNBSP() {
		return new HTML("&nbsp;", true);
	}

	private UIUtil() {
		// singleton
	}
}
