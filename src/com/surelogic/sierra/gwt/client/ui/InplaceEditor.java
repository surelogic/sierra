package com.surelogic.sierra.gwt.client.ui;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FocusListener;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.HasFocus;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.Widget;

public abstract class InplaceEditor extends Composite implements HasFocus {
	private final FocusWidget editor;
	private boolean canceled;

	public InplaceEditor(final FocusWidget editor) {
		super();
		this.editor = editor;
		initWidget(editor);

		editor.setWidth("100%");

		setDefaultValue(editor);

		editor.addKeyboardListener(new KeyboardListener() {

			public void onKeyDown(Widget sender, char keyCode, int modifiers) {
			}

			public void onKeyPress(Widget sender, char keyCode, int modifiers) {
				if (keyCode == KeyboardListener.KEY_ENTER) {
					editor.setFocus(false);
				} else if (keyCode == KeyboardListener.KEY_ESCAPE) {
					canceled = true;
					setDefaultValue(editor);
					editor.setFocus(false);
				}
			}

			public void onKeyUp(Widget sender, char keyCode, int modifiers) {
			}
		});

		editor.addFocusListener(new FocusListener() {

			public void onFocus(Widget sender) {
				canceled = false;
			}

			public void onLostFocus(Widget sender) {
				closeEditor(editor, canceled);
			}
		});
	}

	public int getTabIndex() {
		return editor.getTabIndex();
	}

	public void setAccessKey(char key) {
		editor.setAccessKey(key);
	}

	public void setFocus(boolean focused) {
		editor.setFocus(focused);
	}

	public void setTabIndex(int index) {
		editor.setTabIndex(index);
	}

	public void addFocusListener(FocusListener listener) {
		editor.addFocusListener(listener);
	}

	public void removeFocusListener(FocusListener listener) {
		editor.removeFocusListener(listener);
	}

	public void addKeyboardListener(KeyboardListener listener) {
		editor.addKeyboardListener(listener);
	}

	public void removeKeyboardListener(KeyboardListener listener) {
		editor.removeKeyboardListener(listener);
	}

	protected abstract void setDefaultValue(FocusWidget editor);

	protected abstract void closeEditor(FocusWidget editor, boolean canceled);

	protected FocusWidget getEditor() {
		return editor;
	}

}
