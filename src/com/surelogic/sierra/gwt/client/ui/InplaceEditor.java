package com.surelogic.sierra.gwt.client.ui;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FocusListener;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.HasFocus;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.Widget;

public abstract class InplaceEditor extends Composite implements HasFocus {
	private final SelectableGrid grid;
	private final FocusWidget editor;
	private int editingRow;
	private int editingColumn;
	private boolean canceled;

	public InplaceEditor(final SelectableGrid grid, final FocusWidget editor) {
		super();
		this.grid = grid;
		this.editor = editor;
		initWidget(editor);

		editor.setWidth("100%");

		editor.addKeyboardListener(new KeyboardListener() {

			public void onKeyDown(Widget sender, char keyCode, int modifiers) {
			}

			public void onKeyPress(Widget sender, char keyCode, int modifiers) {
				if (keyCode == KeyboardListener.KEY_ENTER) {
					canceled = false;
					editor.setFocus(false);
				} else if (keyCode == KeyboardListener.KEY_ESCAPE) {
					canceled = true;
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
				close(canceled);
			}
		});
	}

	public boolean isEditing(int row, int column) {
		return isAttached() && (row == editingRow) && (column == editingColumn);
	}

	public final void edit(int row, int column) {
		if (isAttached()) {
			close(false);
		}
		editingRow = row;
		editingColumn = column;

		initEditor(row, column);

		getGrid().setWidget(row, column, this);
		editor.setFocus(true);
	}

	public abstract void initEditor(int row, int column);

	public final void close(boolean canceled) {
		if (isAttached()) {
			closeEditor(canceled);
		}
	}

	public abstract void closeEditor(boolean canceled);

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

	public SelectableGrid getGrid() {
		return grid;
	}

	public FocusWidget getEditor() {
		return editor;
	}

	public int getEditingRow() {
		return editingRow;
	}

	public int getEditingColumn() {
		return editingColumn;
	}

}
