package com.surelogic.sierra.gwt.client.ui;

import com.google.gwt.user.client.ui.Label;

public class ItalicLabel extends Label {

	public ItalicLabel(String title) {
		super(title);
		addStyleName("font-italic");
	}

	public ItalicLabel(String title, boolean wordWrap) {
		super(title, wordWrap);
		addStyleName("font-italic");
	}
}
