package com.surelogic.sierra.gwt.client.ui.grid;

import com.google.gwt.user.client.ui.FocusListener;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.Widget;

public abstract class InplaceEditor {
	private static InplaceEditor currentEditor;

	private final SelectableGrid grid;
	private final int row;
	private final int column;
	private FocusWidget editor;
	private boolean canceled;

	public static boolean isEditing(int row, int column) {
		if (currentEditor == null) {
			return false;
		}
		return currentEditor.getRow() == row
				&& currentEditor.getColumn() == column;
	}

	public InplaceEditor(final SelectableGrid grid, final int row,
			final int column) {
		super();
		this.grid = grid;
		this.row = row;
		this.column = column;
	}

	public final boolean isEditing() {
		return editor != null;
	}

	public final void open() {
		if (currentEditor != null && currentEditor != this) {
			currentEditor.close(false);
		}
		currentEditor = this;

		if (editor == null) {
			editor = createEditor();
			editor.setWidth("100%");

			editor.addKeyboardListener(new KeyboardListener() {

				public void onKeyDown(Widget sender, char keyCode, int modifiers) {
				}

				public void onKeyPress(Widget sender, char keyCode,
						int modifiers) {
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
		initEditor(grid, row, column);

		grid.setWidget(row, column, editor);
		editor.setFocus(true);
	}

	public final void close(boolean canceled) {
		if (currentEditor == this) {
			currentEditor = null;
		}
		if (editor != null) {
			closeEditor(grid, row, column, canceled);
			editor = null;
		}
	}

	public SelectableGrid getGrid() {
		return grid;
	}

	public int getRow() {
		return row;
	}

	public int getColumn() {
		return column;
	}

	protected FocusWidget getEditor() {
		return editor;
	}

	protected abstract FocusWidget createEditor();

	protected abstract void initEditor(SelectableGrid grid, int row, int column);

	protected abstract void closeEditor(SelectableGrid grid, int row,
			int column, boolean canceled);

}
