package com.surelogic.sierra.client.eclipse.views.selection;

import java.util.logging.Level;

import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.PlatformUI;

import com.surelogic.common.eclipse.CascadingList;
import com.surelogic.common.eclipse.RadioArrowMenu;
import com.surelogic.common.eclipse.RadioArrowMenu.IRadioMenuObserver;
import com.surelogic.common.eclipse.logging.SLStatus;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.client.eclipse.model.selection.AbstractFilterObserver;
import com.surelogic.sierra.client.eclipse.model.selection.Filter;
import com.surelogic.sierra.client.eclipse.model.selection.ISelectionFilterFactory;
import com.surelogic.sierra.client.eclipse.model.selection.FindingSearch;

public final class MRadioMenuColumn extends MColumn implements
		IRadioMenuObserver {

	private int f_column;

	private RadioArrowMenu f_menu = null;

	MRadioMenuColumn(CascadingList cascadingList, FindingSearch selection,
			MColumn previousColumn) {
		super(cascadingList, selection, previousColumn);
	}

	@Override
	void init() {
		final CascadingList.IScrolledColumn m = new CascadingList.IScrolledColumn() {
			final Filter previousFilter = getFilterFromColumn(getPreviousColumn());

			public void createContents(Composite panel) {
				f_menu = new RadioArrowMenu(panel);
				if (previousFilter != null) {
					f_menu.addChoice("Show", null);
					f_menu.addSeparator();
				}
				for (ISelectionFilterFactory f : getSelection()
						.getAvailableFilters()) {
					f_menu.addChoice(f, null);
				}
				f_menu.addObserver(MRadioMenuColumn.this);
			}
		};
		f_column = getCascadingList().addColumn(m, true);
		initOfNextColumnComplete();
	}

	@Override
	void initOfNextColumnComplete() {
		f_menu.setEnabled(true);
		super.initOfNextColumnComplete();
	}

	@Override
	void dispose() {
		super.dispose();
		if (f_menu != null) {
			f_menu.removeObserver(this);
			getCascadingList().emptyFrom(f_column);
		}
	}

	void clearMenuSelection() {
		if (f_menu != null) {
			f_menu.clearSelection();
		}
	}

	void setSelection(Object choice) {
		f_menu.setSelection(choice);
	}

	public void selected(Object choice, RadioArrowMenu menu) {
		f_menu.setEnabled(false);

		/*
		 * Please wait...
		 */
		getCascadingList().addColumnAfter(new CascadingList.IScrolledColumn() {
			public void createContents(Composite panel) {
				final Color background = getCascadingList()
						.getContentsBackground();
				panel.setBackground(background);
				final Label waitLabel = new Label(panel, SWT.NONE);
				waitLabel.setText("Please wait...");
				waitLabel.setBackground(background);
			}
		}, f_column, false);

		getSelection().emptyAfter(getFilterFromColumn(getNextColumn()));

		if (choice instanceof ISelectionFilterFactory) {
			final ISelectionFilterFactory filter = (ISelectionFilterFactory) choice;
			getSelection().construct(filter, new DrawFilterAndMenu(f_column));
		} else if (choice.equals("Show")) {
			final MListOfFindingsColumn fsr = new MListOfFindingsColumn(
					getCascadingList(), getSelection(), this, f_column);
			fsr.init();
		}
	}

	class DrawFilterAndMenu extends AbstractFilterObserver {

		private final int f_waitMsgColumn;

		public DrawFilterAndMenu(int waitMsgColumn) {
			f_waitMsgColumn = waitMsgColumn;
		}

		@Override
		public void queryFailure(final Filter filter, final Exception e) {
			// System.out.println("failure");
			// beware the thread context this method call might be made in.
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				public void run() {
					final String msg = "Applying the '"
							+ filter.getFactory().getFilterLabel()
							+ "' filter to the current selection failed (bug).";
					ErrorDialog.openError(PlatformUI.getWorkbench()
							.getActiveWorkbenchWindow().getShell(),
							"Selection Error", msg, SLStatus.createErrorStatus(
									"Initialization of the filter failed.", e));
					SLLogger.getLogger().log(Level.SEVERE, msg, e);
				}
			});
			filter.removeObserver(this);
		}

		@Override
		public void contentsChanged(final Filter filter) {
			// System.out.println("contentsChanged " + filter + " " + this);
			constructFilterReport(filter);
		}

		@Override
		public void contentsEmpty(Filter filter) {
			constructFilterReport(filter);
		}

		private void constructFilterReport(final Filter filter) {
			// beware the thread context this method call might be made in.
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				public void run() {
					MFilterSelectionColumn fsc = new MFilterSelectionColumn(
							getCascadingList(), getSelection(),
							MRadioMenuColumn.this, f_waitMsgColumn, filter);
					fsc.init();
				}
			});
			filter.removeObserver(this);
		}

		@Override
		public void dispose(Filter filter) {
			SLLogger.getLogger().log(
					Level.SEVERE,
					"Unexpected dispose() callback from " + filter
							+ " while it was being created (bug)");
			filter.removeObserver(this);
		}

		@Override
		public String toString() {
			return "[DrawFilterAndMenu waitMsgColumn=" + f_waitMsgColumn + "]";
		}
	}

	private Filter getFilterFromColumn(final MColumn column) {
		if (column instanceof MFilterSelectionColumn) {
			final MFilterSelectionColumn filterSelectionColumn = (MFilterSelectionColumn) column;
			return filterSelectionColumn.getFilter();
		} else {
			return null;
		}
	}
}
