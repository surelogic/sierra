package com.surelogic.sierra.gwt.client.ui;

import com.google.gwt.user.client.ui.KeyboardListenerAdapter;
import com.google.gwt.user.client.ui.Widget;

public abstract class EnterListener extends KeyboardListenerAdapter {

	public void onKeyUp(Widget sender, char keyCode, int modifiers) {
		if (keyCode == KEY_ENTER) {
			onEnter(sender, keyCode, modifiers);
		}
	}

	public abstract void onEnter(Widget sender, char keyCode, int modifiers);

}
