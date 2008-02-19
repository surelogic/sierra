package com.surelogic.sierra.gwt.client.ui;

import com.google.gwt.user.client.ui.TextBox;

public abstract class TextBoxEditor extends InplaceEditor {

	private String initialValue;

	public TextBoxEditor(SelectableGrid grid) {
		super(grid, new TextBox());
	}

	public void initEditor(int row, int column) {
		initialValue = getGrid().getText(row, column);
		getTextEditor().setText(initialValue);
	}

	public void closeEditor(boolean canceled) {
		String cellValue = initialValue;
		if (!canceled) {
			String newValue = getTextEditor().getText();
			if (updateValue(initialValue, newValue)) {
				cellValue = newValue;
			}
		}
		getGrid().setText(getEditingRow(), getEditingColumn(), cellValue);
	}

	protected abstract boolean updateValue(String oldValue, String newValue);

	private TextBox getTextEditor() {
		return (TextBox) getEditor();
	}
}
