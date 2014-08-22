package com.surelogic.sierra.client.eclipse.views.selection;

import java.util.logging.Level;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.progress.UIJob;

import com.surelogic.NonNull;
import com.surelogic.Nullable;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.common.ui.CascadingList;
import com.surelogic.common.ui.jobs.SLUIJob;
import com.surelogic.sierra.client.eclipse.actions.NewScanAction;
import com.surelogic.sierra.client.eclipse.dialogs.DeleteSearchDialog;
import com.surelogic.sierra.client.eclipse.dialogs.OpenSearchDialog;
import com.surelogic.sierra.client.eclipse.dialogs.SaveSearchAsDialog;
import com.surelogic.sierra.client.eclipse.model.IProjectsObserver;
import com.surelogic.sierra.client.eclipse.model.Projects;
import com.surelogic.sierra.client.eclipse.model.selection.Filter;
import com.surelogic.sierra.client.eclipse.model.selection.ISelectionManagerObserver;
import com.surelogic.sierra.client.eclipse.model.selection.ISelectionObserver;
import com.surelogic.sierra.client.eclipse.model.selection.Selection;
import com.surelogic.sierra.client.eclipse.model.selection.SelectionManager;
import com.surelogic.sierra.client.eclipse.views.IViewCallback;
import com.surelogic.sierra.client.eclipse.views.IViewMediator;

public final class FindingsSelectionMediator implements IProjectsObserver, CascadingList.ICascadingListObserver,
    ISelectionManagerObserver, IViewMediator, ISelectionObserver {

  private final IViewCallback f_view;
  private final Composite f_findingsPage;
  private final CascadingList f_cascadingList;
  private final ToolItem f_clearSelectionItem;
  private final Composite f_breadcrumbsPanel;
  private final Link f_breadcrumbsLink;
  private final ToolItem f_openSearchItem;
  private final ToolItem f_deleteSearchItem;
  private final ToolItem f_saveSearchesAsItem;
  private final Link f_savedSelections;
  private final Link f_porousCountLink;

  private final SelectionManager f_manager = SelectionManager.getInstance();

  private MColumn f_first = null;

  FindingsSelectionMediator(FindingsSelectionView view, Composite findingsPage, CascadingList cascadingList,
      ToolItem clearSelectionItem, Composite breadcrumbsPanel, Link breadcrumbsLink, Link porousCountLink, ToolItem openSearchItem,
      ToolItem saveSearchesAsItem, ToolItem deleteSearchItem, Link savedSelections) {
    f_view = view;
    f_findingsPage = findingsPage;
    f_cascadingList = cascadingList;
    f_clearSelectionItem = clearSelectionItem;
    f_breadcrumbsPanel = breadcrumbsPanel;
    f_breadcrumbsLink = breadcrumbsLink;
    f_openSearchItem = openSearchItem;
    f_saveSearchesAsItem = saveSearchesAsItem;
    f_deleteSearchItem = deleteSearchItem;
    f_savedSelections = savedSelections;
    f_porousCountLink = porousCountLink;
  }

  @Override
  public String getHelpId() {
    return "com.surelogic.sierra.client.eclipse.view-findings-quick-search";
  }

  @Override
  public String getNoDataI18N() {
    return "sierra.eclipse.noDataFindingsQuickSearch";
  }

  @Override
  public Listener getNoDataListener() {
    return new Listener() {
      @Override
      public void handleEvent(Event event) {
        new NewScanAction().run();
      }
    };
  }

  @Override
  public void init() {
    f_clearSelectionItem.addListener(SWT.Selection, new Listener() {
      @Override
      public void handleEvent(Event event) {
        clearToNewWorkingSelection();
      }
    });

    f_breadcrumbsLink.addListener(SWT.Selection, new Listener() {
      @Override
      public void handleEvent(Event event) {
        final int column = Integer.parseInt(event.text);
        f_cascadingList.show(column);
      }
    });

    f_openSearchItem.addListener(SWT.Selection, new Listener() {
      @Override
      public void handleEvent(Event event) {
        OpenSearchDialog dialog = new OpenSearchDialog(f_cascadingList.getShell());
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
      @Override
      public void handleEvent(Event event) {
        SaveSearchAsDialog dialog = new SaveSearchAsDialog(f_cascadingList.getShell());
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
          f_manager.saveSelection(name, f_manager.getWorkingSelection());
        }
      }
    });

    f_deleteSearchItem.addListener(SWT.Selection, new Listener() {
      @Override
      public void handleEvent(Event event) {
        DeleteSearchDialog dialog = new DeleteSearchDialog(f_cascadingList.getShell());
        dialog.open();
      }
    });

    f_savedSelections.addListener(SWT.Selection, new Listener() {
      @Override
      public void handleEvent(Event event) {
        final String selectionName = event.text;
        /*
         * open the current selection.
         */
        final Selection newSelection = f_manager.getSavedSelection(selectionName);
        if (newSelection == null) {
          SLLogger.getLogger().log(Level.SEVERE, "Search '" + selectionName + "' is unknown (bug).", new Exception());
          return;
        }
        if (newSelection.getFilterCount() < 1) {
          SLLogger.getLogger().log(Level.SEVERE, "Search '" + selectionName + "' defines no filters (bug).", new Exception());
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

  @Override
  public void setFocus() {
    f_cascadingList.setFocus();
  }

  @Override
  public void dispose() {
    f_cascadingList.removeObserver(this);
    f_manager.removeObserver(this);
    final Selection workingSelection = f_manager.getWorkingSelection();
    if (workingSelection != null) {
      f_manager.removeObserver(this);
      f_manager.saveViewState(workingSelection);
    }
    Projects.getInstance().removeObserver(this);
  }

  @Override
  public void notify(Projects p) {
    /*
     * We are checking if there is anything in the database at all. If not we
     * show a helpful message, if so we display the findings selection page.
     */
    final boolean dataShouldShow = !p.isEmpty();

    // beware the thread context this method call might be made in.
    final UIJob job = new SLUIJob() {
      @Override
      public IStatus runInUIThread(IProgressMonitor monitor) {
        if (!f_view.matchesStatus(dataShouldShow)) {
          /*
           * Only gets run when the page actually has changed.
           */
          f_view.hasData(dataShouldShow);

          if (dataShouldShow) {
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
    f_breadcrumbsLink.setText("");
    final Selection workingSelection = f_manager.getWorkingSelection();
    if (workingSelection != null)
      workingSelection.dispose();
    f_manager.setWorkingSelection(null);
  }

  private void clearToPersistedViewState() {
    final Selection persistedViewState = f_manager.getViewState();
    if (persistedViewState != null) {
      /*
       * We only want to restore the view state once per session of Eclipse so
       * now we clear the view state out of the selection manager.
       */
      f_manager.removeViewState();
      openSelection(persistedViewState);
    } else {
      clearToNewWorkingSelection();
    }
  }

  private void clearToNewWorkingSelection() {
    disposeWorkingSelection();
    final Selection workingSelection = f_manager.construct();
    workingSelection.initAndSyncToDatabase();
    f_manager.setWorkingSelection(workingSelection);
    updateSavedSelections();
    f_first = new MRadioMenuColumn(f_cascadingList, workingSelection, null);
    f_first.init();
  }

  private void openSelection(@NonNull final Selection newSelection) {
    disposeWorkingSelection();
    f_manager.setWorkingSelection(newSelection);
    newSelection.initAndSyncToDatabase();
    newSelection.refreshFilters();
    f_first = new MRadioMenuColumn(f_cascadingList, newSelection, null);
    f_first.init();

    MRadioMenuColumn prevMenu = (MRadioMenuColumn) f_first;
    for (Filter filter : newSelection.getFilters()) {
      /*
       * Set the right choice on the previous menu
       */
      prevMenu.setSelection(filter.getFactory());
      /*
       * Create a filter selection
       */
      MFilterSelectionColumn fCol = new MFilterSelectionColumn(f_cascadingList, newSelection, prevMenu, filter);
      fCol.init();
      /*
       * Create a menu
       */
      prevMenu = new MRadioMenuColumn(f_cascadingList, newSelection, fCol);
      prevMenu.init();
    }
  }

  @Override
  public void notify(CascadingList cascadingList) {
    updateBreadcrumbs();
    updateSavedSelections();
  }

  @Override
  public void savedSelectionsChanged() {
    updateSavedSelections();
  }

  @Override
  public void workingSelectionChanged(Selection newWorkingSelection, Selection oldWorkingSelection) {
    if (oldWorkingSelection != null)
      oldWorkingSelection.removeObserver(this);
    if (newWorkingSelection != null)
      newWorkingSelection.addObserver(this);
    selectionChanged(newWorkingSelection);
  }

  /**
   * Updates the trail of filter breadcrumbs for the user to animate around this
   * control.
   */
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
      }
      clColumn = clColumn.getNextColumn();
    } while (clColumn != null);
    f_breadcrumbsLink.setText(b.toString());
    final boolean somethingToClear = b.length() > 0;
    f_clearSelectionItem.setEnabled(somethingToClear);
    f_breadcrumbsPanel.layout();
    f_findingsPage.layout();
  }

  /**
   * Updates the porous count displayed to the user for the current working
   * selection.
   */
  private void updatePorus() {
    final Selection workingSelection = f_manager.getWorkingSelection();
    if (workingSelection != null) {
      final int c = workingSelection.getFindingCountPorous();
      if (workingSelection.getFilterCount() == 0) {
        f_porousCountLink.setText("Filter Off (All Findings Selected)");
      } else {
        final boolean plural = c != 1;
        f_porousCountLink.setText(workingSelection.getFindingCountPorous() + " Finding" + (plural ? "s Selected" : " Selected"));
      }
    } else
      f_porousCountLink.setText("");
    f_breadcrumbsPanel.layout();
    f_findingsPage.layout();
  }

  private void updateSavedSelections() {
    StringBuilder b = new StringBuilder();
    @Nullable
    final Selection workingSelection = f_manager.getWorkingSelection();
    final boolean saveable = workingSelection != null && workingSelection.getFilterCount() > 0;
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

  public void selectAll() {
    f_first.selectAll();
  }

  @Override
  public void selectionChanged(Selection ignore) {
    final SLUIJob job = new SLUIJob() {
      @Override
      public IStatus runInUIThread(IProgressMonitor monitor) {
        updatePorus();
        return Status.OK_STATUS;
      }
    };
    job.schedule();
  }
}
