package com.surelogic.sierra.client.eclipse.views.selection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.progress.UIJob;

import com.surelogic.common.eclipse.CascadingList;
import com.surelogic.common.eclipse.RadioArrowMenu;
import com.surelogic.common.eclipse.RadioArrowMenu.IRadioMenuObserver;
import com.surelogic.common.eclipse.dialogs.ErrorDialogUtility;
import com.surelogic.common.eclipse.jobs.SLUIJob;
import com.surelogic.common.eclipse.logging.SLStatusUtility;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.client.eclipse.model.selection.Filter;
import com.surelogic.sierra.client.eclipse.model.selection.IFilterObserver;
import com.surelogic.sierra.client.eclipse.model.selection.ISelectionFilterFactory;
import com.surelogic.sierra.client.eclipse.model.selection.Selection;

public final class MRadioMenuColumn extends MColumn implements
		IRadioMenuObserver {

	private RadioArrowMenu f_menu = null;

	MRadioMenuColumn(CascadingList cascadingList, Selection selection,
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
				for (ISelectionFilterFactory f : getFilterChoicesForThisMenu()) {
					f_menu.addChoice(f, null);
				}
				f_menu.addObserver(MRadioMenuColumn.this);
			}
		};
		getCascadingList().addColumn(m, true);
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
			final int column = getColumnIndex();
			if (column != -1)
				getCascadingList().emptyFrom(column);
		}
	}

	@Override
	int getColumnIndex() {
		if (f_menu == null)
			return -1;
		final Composite panel = f_menu.getPanel();
		if (panel.isDisposed())
			return -1;
		else
			return getCascadingList().getColumnIndexOf(panel);
	}

	void setSelection(Object choice) {
		f_menu.setSelection(choice);
	}
	
	void clearSelection() {
	  f_menu.clearSelection();
	}

	@Override
	public void forceFocus() {
	  f_menu.forceFocus();
	  getCascadingList().show(index);
	}
	
	public void selected(Object choice, RadioArrowMenu menu) {
		f_menu.setEnabled(false);
		final int column = getColumnIndex();

		/*
		 * Please wait...
		 */
		getCascadingList().addColumnAfter(new CascadingList.IColumn() {

			public Composite createContents(Composite cascadingListContents) {
				final Composite panel = new Composite(cascadingListContents,
						SWT.NONE);
				final Color background = getCascadingList()
						.getContentsBackground();

				panel.setBackground(background);
				panel.setLayout(new FillLayout());
				final Label waitLabel = new Label(panel, SWT.CENTER);
				waitLabel.setText("Please wait...");
				waitLabel.setBackground(background);
				return panel;
			}
		}, column, false);

		getSelection().emptyAfter(getFilterFromColumn(getNextColumn()));

		if (choice instanceof ISelectionFilterFactory) {
			final ISelectionFilterFactory filter = (ISelectionFilterFactory) choice;
			getSelection().construct(filter, new DrawFilterAndMenu());
			getSelection().changed();
		} else if ("Show".equals(choice)) {
			final MListOfFindingsColumn fsr = new MListOfFindingsColumn(
					getCascadingList(), getSelection(), this);
			fsr.init();
		}
	}

	public void goBack(RadioArrowMenu menu) {
	  MColumn c = getPreviousColumn();
	  if (c != null) {
	    c.forceFocus();
	  }
	}
	
	public void goNext(RadioArrowMenu menu) {
    MColumn c = getNextColumn();
    if (c != null) {
      c.forceFocus();
    }
  }
	
	public void escape(RadioArrowMenu menu) {
	  if (getNextColumn() == null) {
	    MColumn prev  = getPreviousColumn();
	    if (prev != null) {
	      // Not the first menu
	      MColumn prev2 = prev.getPreviousColumn();
	      if (prev2 instanceof MRadioMenuColumn) {
	        MRadioMenuColumn menuColumn = (MRadioMenuColumn) prev2;
	        menuColumn.escape(menuColumn.f_menu);
	      }
	    }
	  } else {
	    getSelection().emptyAfter(getFilterFromColumn(getNextColumn()));	    
	    clearSelection();
	    emptyAfter();
	    forceFocus();
	  }
	}
	
	class DrawFilterAndMenu implements IFilterObserver {

		public void filterQueryFailure(final Filter filter, final Exception e) {
			filter.removeObserver(this);
			// beware the thread context this method call might be made in.
			final UIJob job = new SLUIJob() {
				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					final int errNo = 27;
					final String msg = I18N.err(errNo, filter.getFactory()
							.getFilterLabel());
					final IStatus reason = SLStatusUtility.createErrorStatus(errNo,
							msg, e);
					ErrorDialogUtility.open(null, "Selection Error", reason);
					return Status.OK_STATUS;
				}
			};
			job.schedule();
		}

		public void filterChanged(final Filter filter) {
			filter.removeObserver(this);
			// beware the thread context this method call might be made in.
			final UIJob job = new SLUIJob() {
				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					MFilterSelectionColumn fsc = new MFilterSelectionColumn(
							getCascadingList(), getSelection(),
							MRadioMenuColumn.this, filter);
					fsc.init();
					/*
					 * Add the radio menu after this item.
					 */
					final MRadioMenuColumn rmc = new MRadioMenuColumn(
							getCascadingList(), getSelection(), fsc);
					rmc.init();
					return Status.OK_STATUS;
				}
			};
			job.schedule();
		}

		public void filterDisposed(Filter filter) {
			filter.removeObserver(this);
			SLLogger.getLogger().log(Level.SEVERE, I18N.err(28, filter),
					new Exception());
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

	private List<ISelectionFilterFactory> getFilterChoicesForThisMenu() {
		List<ISelectionFilterFactory> result = new ArrayList<ISelectionFilterFactory>(
				Selection.getAllFilters());
		MColumn column = this;
		do {
			column = column.getPreviousColumn();
			Filter f = getFilterFromColumn(column);
			if (f != null) {
				result.remove(f.getFactory());
			}
		} while (column != null);
		Collections.sort(result);
		return result;
	}
	
  @Override 
  void selectAll() {
    if (f_menu.isFocusControl()) {
      // Do nothing
    } else {
      super.selectAll();
    }
  }
}
