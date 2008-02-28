package com.surelogic.sierra.client.eclipse.views.selection;

import java.util.logging.Level;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.progress.UIJob;

import com.surelogic.common.eclipse.CascadingList;
import com.surelogic.common.eclipse.PageBook;
import com.surelogic.common.eclipse.SLImages;
import com.surelogic.common.eclipse.jobs.SLUIJob;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.client.eclipse.dialogs.DeleteSearchDialog;
import com.surelogic.sierra.client.eclipse.dialogs.OpenSearchDialog;
import com.surelogic.sierra.client.eclipse.dialogs.SaveSearchAsDialog;
import com.surelogic.sierra.client.eclipse.model.IProjectsObserver;
import com.surelogic.sierra.client.eclipse.model.Projects;
import com.surelogic.sierra.client.eclipse.model.selection.Filter;
import com.surelogic.sierra.client.eclipse.model.selection.ISelectionManagerObserver;
import com.surelogic.sierra.client.eclipse.model.selection.Selection;
import com.surelogic.sierra.client.eclipse.model.selection.SelectionManager;
import com.surelogic.sierra.client.eclipse.preferences.PreferenceConstants;

public final class FindingsSelectionMediator implements IProjectsObserver,
		CascadingList.ICascadingListObserver, ISelectionManagerObserver, IFindingsObserver {

	private final PageBook f_pages;
	private final Control f_noFindingsPage;
	private final Composite f_findingsPage;
	private final CascadingList f_cascadingList;
	private final ToolItem f_clearSelectionItem;
	private final Link f_breadcrumbs;
	private final ToolItem f_openSearchItem;
	private final ToolItem f_deleteSearchItem;
	private final ToolItem f_saveSearchesAsItem;
	private final Link f_savedSelections;
  private final Label f_findingsIcon;
  private final Label f_findingsStatus;

	private final SelectionManager f_manager = SelectionManager.getInstance();

	private Selection f_workingSelection = null;

	private MColumn f_first = null;

	FindingsSelectionMediator(PageBook pages, Control noFindingsPage,
			Composite findingsPage, CascadingList cascadingList,
			ToolItem clearSelectionItem, Link breadcrumbs,
			Label findingsIcon, Label findingsStatus, 
			ToolItem openSearchItem, ToolItem saveSearchesAsItem,
			ToolItem deleteSearchItem, Link savedSelections) {
		f_pages = pages;
		f_noFindingsPage = noFindingsPage;
		f_findingsPage = findingsPage;
		f_cascadingList = cascadingList;
		f_clearSelectionItem = clearSelectionItem;
		f_breadcrumbs = breadcrumbs;
		f_openSearchItem = openSearchItem;
		f_saveSearchesAsItem = saveSearchesAsItem;
		f_deleteSearchItem = deleteSearchItem;
		f_savedSelections = savedSelections;
		f_findingsIcon = findingsIcon;
    f_findingsStatus = findingsStatus;
	}

	public void init() {
		f_clearSelectionItem.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				clearToNewWorkingSelection();
			}
		});

		f_breadcrumbs.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				final int column = Integer.parseInt(event.text);
				f_cascadingList.show(column);
			}
		});

		f_openSearchItem.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				OpenSearchDialog dialog = new OpenSearchDialog(f_cascadingList
						.getShell());
				if (Window.CANCEL != dialog.open()) {
					/*
					 * Save the selection
					 */
					Selection newSelection = dialog.getSelection();
					if (newSelection == null)
						return;
					openSelection(newSelection);
				}
			}
		});

		f_saveSearchesAsItem.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				SaveSearchAsDialog dialog = new SaveSearchAsDialog(
						f_cascadingList.getShell());
				if (Window.CANCEL != dialog.open()) {
					/*
					 * Save the selection
					 */
					String name = dialog.getName();
					if (name == null)
						return;
					name = name.trim();
					if ("".equals(name))
						return;
					f_manager.saveSelection(name, f_workingSelection);
				}
			}
		});

		f_deleteSearchItem.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				DeleteSearchDialog dialog = new DeleteSearchDialog(
						f_cascadingList.getShell());
				dialog.open();
			}
		});

		f_savedSelections.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				final String selectionName = event.text;
				/*
				 * open the current selection.
				 */
				final Selection newSelection = f_manager
						.getSavedSelection(selectionName);
				if (newSelection == null) {
					SLLogger.getLogger().log(Level.SEVERE,
							"Search '" + selectionName + "' is unknown (bug).",
							new Exception());
					return;
				}
				if (newSelection.getFilterCount() < 1) {
					SLLogger.getLogger().log(
							Level.SEVERE,
							"Search '" + selectionName
									+ "' defines no filters (bug).",
							new Exception());
					return;
				}
				openSelection(newSelection);
			}
		});

		f_cascadingList.addObserver(this);
		f_manager.addObserver(this);
		Projects.getInstance().addObserver(this);
		notify(Projects.getInstance());
	}

	public void setFocus() {
		f_cascadingList.setFocus();
	}

	public void dispose() {
		f_cascadingList.removeObserver(this);
		f_manager.removeObserver(this);
		if (f_workingSelection != null) {
			f_manager.saveViewState(f_workingSelection);
		}
		Projects.getInstance().removeObserver(this);
	}

	public void notify(Projects p) {
		/*
		 * We are checking if there is anything in the database at all. If not
		 * we show a helpful message, if so we display the findings selection
		 * page.
		 */
		final Control page;
		if (p.isEmpty()) {
			page = f_noFindingsPage;
		} else {
			page = f_findingsPage;
		}
		// beware the thread context this method call might be made in.
		final UIJob job = new SLUIJob() {
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				if (page != f_pages.getPage()) {
					/*
					 * Only gets run when the page actually has changed.
					 */
					f_pages.showPage(page);
					if (page == f_findingsPage) {
						clearToPersistedViewState();
					}
				}
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}

	private void disposeWorkingSelection() {
		if (f_first != null)
			f_first.dispose();
		f_breadcrumbs.setText("");
		if (f_workingSelection != null)
			f_workingSelection.dispose();
	}

	private void clearToPersistedViewState() {
		final Selection persistedViewState = f_manager.getViewState();
		if (persistedViewState != null) {
			/*
			 * We only want to restore the view state once per session of
			 * Eclipse so now we clear the view state out of the selection
			 * manager.
			 */
			f_manager.removeViewState();
			openSelection(persistedViewState);
		} else {
			clearToNewWorkingSelection();
		}
	}

	private void clearToNewWorkingSelection() {
		disposeWorkingSelection();
		f_workingSelection = f_manager.construct();
		f_workingSelection.initAndSyncToDatabase();
		updateSavedSelections();
		f_first = new MRadioMenuColumn(f_cascadingList, f_workingSelection,
				null);
		f_first.init();
	}

	private void openSelection(final Selection newSelection) {
		disposeWorkingSelection();
		f_workingSelection = newSelection;
		f_workingSelection.initAndSyncToDatabase();
		f_first = new MRadioMenuColumn(f_cascadingList, f_workingSelection,
				null);
		f_first.init();
		f_workingSelection.refreshFilters();

		MRadioMenuColumn prevMenu = (MRadioMenuColumn) f_first;
		for (Filter filter : f_workingSelection.getFilters()) {
			/*
			 * Set the right choice on the previous menu
			 */
			prevMenu.setSelection(filter.getFactory());
			/*
			 * Create a filter selection
			 */
			MFilterSelectionColumn fCol = new MFilterSelectionColumn(
					f_cascadingList, f_workingSelection, prevMenu, filter);
			fCol.init();
			/*
			 * Create a menu
			 */
			prevMenu = new MRadioMenuColumn(f_cascadingList,
					f_workingSelection, fCol);
			prevMenu.init();
		}
		if (f_workingSelection.isShowingFindings()) {
			prevMenu.setSelection("Show");
			MListOfFindingsColumn list = new MListOfFindingsColumn(
					f_cascadingList, f_workingSelection, prevMenu);
			list.addObserver(this);
			list.init();
		}
	}

	public void notify(CascadingList cascadingList) {
		updateBreadcrumbs();
		updateSavedSelections();
	}

	public void savedSelectionsChanged(SelectionManager manager) {
		updateSavedSelections();
	}

	private void updateBreadcrumbs() {
		final StringBuilder b = new StringBuilder();
		int column = 0;
		boolean first = true;
		MColumn clColumn = f_first;
		do {
			if (clColumn instanceof MFilterSelectionColumn) {
				MFilterSelectionColumn fsc = (MFilterSelectionColumn) clColumn;
				final Filter filter = fsc.getFilter();
				final String name = filter.getFactory().getFilterLabel();
				if (first) {
					first = false;
					b.append(" ");
				} else {
					b.append(" | ");
				}
				b.append("<a href=\"").append(column).append("\">");
				b.append(name).append("</a>");
				column += 2; // selector and menu
			} else if (clColumn instanceof MListOfFindingsColumn) {
				b.append(" | <a href=\"").append(column).append("\">Show</a>");
				((MListOfFindingsColumn) clColumn).addObserver(this);
			}
			clColumn = clColumn.getNextColumn();
		} while (clColumn != null);
		f_breadcrumbs.setText(b.toString());
		final boolean somethingToClear = b.length() > 0;
		f_clearSelectionItem.setEnabled(somethingToClear);
		f_breadcrumbs.getParent().layout();
		f_findingsPage.layout();
	}

	private void updateSavedSelections() {
		StringBuilder b = new StringBuilder();
		final boolean saveable = f_workingSelection != null
				&& f_workingSelection.getFilterCount() > 0;
		f_saveSearchesAsItem.setEnabled(saveable);
		final boolean hasSavedSelections = !f_manager.isEmpty();
		f_openSearchItem.setEnabled(hasSavedSelections);
		f_deleteSearchItem.setEnabled(hasSavedSelections);

		if (hasSavedSelections) {
			b.append("Saved Searches:");

			for (String link : f_manager.getSavedSelectionNames()) {
				b.append("  <a href=\"");
				b.append(link);
				b.append("\">");
				b.append(link);
				b.append("</a>");
			}
		} else {
			b.append("(no saved searches)");
		}
		f_savedSelections.setText(b.toString());
		f_savedSelections.getParent().layout();
		f_findingsPage.layout();
	}

	public void selectionChanged(Selection selecton) {
		/*
		 * Nothing to do.
		 */
	}
	
	public void selectAll() {
	  f_first.selectAll();
	}

  public void findingsLimited(boolean isLimited) {
    if (isLimited) {
      final int findingsListLimit = PreferenceConstants.getFindingsListLimit();
      final Image warning = SLImages.getWorkbenchImage(org.eclipse.ui.ISharedImages.IMG_OBJS_WARN_TSK);
      f_findingsIcon.setImage(warning);
      f_findingsStatus.setText("Limited to "+findingsListLimit+" findings");
    } else {
      f_findingsIcon.setImage(null);
      f_findingsStatus.setText("");
    }
    f_findingsIcon.getParent().layout();
    f_findingsPage.layout();
  }

  public void findingsDisposed() {
    findingsLimited(false);
  }
}
