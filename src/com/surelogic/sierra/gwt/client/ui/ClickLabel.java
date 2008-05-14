package com.surelogic.sierra.gwt.client.ui;

import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Label;

/**
 * Label with an additional style of <code>.clickable</code>
 * 
 * @author nathan
 * 
 */
public class ClickLabel extends Label {

	public ClickLabel(String label, ClickListener listener) {
		super(label);
		addStyleName("clickable");
		addClickListener(listener);
	}

}
