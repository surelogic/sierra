package com.surelogic.sierra.gwt.client.ui;

import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.TextBox;
import com.surelogic.sierra.gwt.client.util.LangUtil;

public class TextBoxEditor extends InplaceEditor {
	private String initialValue;

	public static InplaceEditorFactory getFactory() {
		return new InplaceEditorFactory() {

			public InplaceEditor createEditor(SelectableGrid grid, int row,
					int column) {
				return new TextBoxEditor(grid, row, column);
			}

		};
	}

	public TextBoxEditor(SelectableGrid grid, int row, int column) {
		super(grid, row, column);
	}

	protected FocusWidget createEditor() {
		return new TextBox();
	}

	protected void initEditor(SelectableGrid grid, int row, int column) {
		initialValue = grid.getText(row, column);
		getTextEditor().setText(initialValue);
	}

	protected void closeEditor(SelectableGrid grid, int row, int column,
			boolean canceled) {
		String cellValue = initialValue;
		if (!canceled) {
			String newValue = getTextEditor().getText();
			boolean valueChanged = !LangUtil.equals(initialValue, newValue);
			if (valueChanged) {
				cellValue = (String) grid.fireChangeEvent(row, column,
						initialValue, newValue);
			}
		}
		grid.setText(row, column, cellValue);
	}

	private TextBox getTextEditor() {
		return (TextBox) getEditor();
	}
}
