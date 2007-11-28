package com.surelogic.sierra.client.eclipse.views.selection;

import com.surelogic.common.eclipse.CascadingList;
import com.surelogic.sierra.client.eclipse.model.selection.FindingSearch;

/**
 * Abstract base class for all mediator columns managed within the
 * {@link FindingsSelectionView}
 */
public abstract class MColumn {

	private final CascadingList f_cascadingList;

	private final FindingSearch f_selection;

	MColumn(CascadingList cascadingList, FindingSearch selection,
			MColumn previousColumn) {
		assert cascadingList != null;
		f_cascadingList = cascadingList;
		assert selection != null;
		f_selection = selection;
		f_previousColumn = previousColumn;
		if (f_previousColumn != null)
			f_previousColumn.setNextColumn(this);
	}

	CascadingList getCascadingList() {
		return f_cascadingList;
	}

	FindingSearch getSelection() {
		return f_selection;
	}

	abstract void init();

	/**
	 * Indicates that a initialization of column after this one has completed
	 * initialization. Subclasses may override but must call this method.
	 * 
	 * <pre>
	 *   super.initOfNextColumnComplete();
	 * </pre>
	 */
	void initOfNextColumnComplete() {
		if (hasPreviousColumn())
			getPreviousColumn().initOfNextColumnComplete();
	}

	/**
	 * Subclasses may override, however they must invoke this method with code
	 * like
	 * 
	 * <pre>
	 * &#064;Override
	 * void dispose() {
	 * 	super.dispose();
	 * 	// do something
	 * }
	 * </pre>
	 * 
	 * so that subsequent columns have dispose invoked on them.
	 */
	void dispose() {
		if (f_previousColumn != null) {
			f_previousColumn.setNextColumn(null);
		}
		if (f_nextColumn != null) {
			f_nextColumn.dispose();
		}
	}

	/**
	 * Immutable reference to the column before this one, will be
	 * <code>null</code> if this is the first column.
	 */
	private final MColumn f_previousColumn;

	boolean hasPreviousColumn() {
		return f_previousColumn != null;
	}

	/**
	 * Gets the reference to the column before this one, will be
	 * <code>null</code> if this is the first column.
	 * 
	 * @return a reference to the column before this one, or <code>null</code>
	 *         if this is the first column.
	 */
	MColumn getPreviousColumn() {
		return f_previousColumn;
	}

	/**
	 * Mutable reference to the column after this one, will be <code>null</code>
	 * if this is the last column.
	 */
	private MColumn f_nextColumn = null;

	boolean hasNextColumn() {
		return f_nextColumn != null;
	}

	/**
	 * Gets the reference to the column after this one, will be
	 * <code>null</code> if this is the last column.
	 * 
	 * @return a reference to the column after this one, or <code>null</code>
	 *         if this is the last column.
	 */
	MColumn getNextColumn() {
		return f_nextColumn;
	}

	/**
	 * Sets the column after this column.
	 * 
	 * @param nextColumn
	 *            the column after this column, may be <code>null</code> is
	 *            this is now the last column.
	 */
	private void setNextColumn(MColumn nextColumn) {
		f_nextColumn = nextColumn;
	}
}
