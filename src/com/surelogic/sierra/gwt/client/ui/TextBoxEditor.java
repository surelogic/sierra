package com.surelogic.sierra.gwt.client.ui;

import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.TextBox;

public abstract class TextBoxEditor extends InplaceEditor {

	public TextBoxEditor() {
		super(new TextBox());
	}

	protected final void setDefaultValue(FocusWidget editor) {
		setDefaultValue((TextBox) editor);
	}

	protected abstract void setDefaultValue(TextBox editor);

	protected final void closeEditor(FocusWidget editor, boolean canceled) {
		closeEditor((TextBox) editor, canceled);
	}

	protected abstract void closeEditor(TextBox editor, boolean canceled);

}
