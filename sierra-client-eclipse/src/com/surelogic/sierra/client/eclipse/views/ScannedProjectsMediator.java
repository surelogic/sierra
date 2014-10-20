package com.surelogic.sierra.client.eclipse.views;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.SystemUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.progress.UIJob;

import com.surelogic.common.CommonImages;
import com.surelogic.common.SLUtility;
import com.surelogic.common.core.JDTUtility;
import com.surelogic.common.ui.SLImages;
import com.surelogic.common.ui.TableUtility;
import com.surelogic.common.ui.jobs.SLUIJob;
import com.surelogic.sierra.client.eclipse.actions.NewScan;
import com.surelogic.sierra.client.eclipse.actions.NewScanAction;
import com.surelogic.sierra.client.eclipse.jobs.DeleteDatabaseJob;
import com.surelogic.sierra.client.eclipse.jobs.DeleteProjectDataJob;
import com.surelogic.sierra.client.eclipse.model.IProjectsObserver;
import com.surelogic.sierra.client.eclipse.model.Projects;
import com.surelogic.sierra.client.eclipse.model.ScannedProject;
import com.surelogic.sierra.client.eclipse.model.selection.SelectionManager;

public class ScannedProjectsMediator extends AbstractSierraViewMediator implements IViewUpdater, IProjectsObserver {

  final SelectionManager f_manager = SelectionManager.getInstance();

  final ScannedProjectsView f_view;
  final Table f_table;
  final String[] f_columnTitles = { "Project", "Scan Time", "Prior Scan Time",
      "Exclusion Specification (surelogic-tools.properties)" };

  public ScannedProjectsMediator(ScannedProjectsView view, Table table) {
    super(view);
    f_view = view;
    f_table = table;
  }

  @Override
  public void init() {
    super.init();

    createTableColumns();
    f_table.setHeaderVisible(true);

    final Action deleteProjectScansAction = new Action() {
      @Override
      public void run() {
        final List<String> projectNames = new ArrayList<String>();
        for (ScannedProject sp : getSelectedScannedProjects()) {
          projectNames.add(sp.getName());
        }
        if (!projectNames.isEmpty()) {
          DeleteProjectDataJob.utility(projectNames, f_table.getShell(), false);
        }
      }
    };
    deleteProjectScansAction.setText("Delete");
    deleteProjectScansAction.setToolTipText("Delete selected scans");
    deleteProjectScansAction.setImageDescriptor(SLImages.getImageDescriptor(CommonImages.IMG_RED_X));

    final Action deleteDatabaseAction = new Action() {
      public void run() {
        final StringBuilder b = new StringBuilder();
        b.append("Are you sure you want to delete all the ");
        b.append("Sierra scans in your Eclipse workspace?\n\n");
        b.append("This action will not ");
        b.append("change or delete data on any Sierra server.");
        if (!MessageDialog.openConfirm(f_table.getShell(), "Confirm Delete All Scans", b.toString())) {
          return; // bail
        }
        final Job job = new DeleteDatabaseJob();
        job.setUser(true);
        job.schedule();
      }
    };
    deleteDatabaseAction.setText("Delete All Scans");

    final Action reScanProjectsAction = new Action() {
      @Override
      public void run() {
        final List<IJavaProject> javaProjects = new ArrayList<IJavaProject>();
        for (ScannedProject sp : getSelectedScannedProjects()) {
          IJavaProject project = JDTUtility.getJavaProject(sp.getName());
          if (project != null)
            javaProjects.add(project);
        }

        if (!javaProjects.isEmpty())
          (new NewScan()).scan(javaProjects);
      }
    };
    reScanProjectsAction.setText("Re-Scan");
    reScanProjectsAction.setToolTipText("Re-scan the selected project");
    reScanProjectsAction.setImageDescriptor(SLImages.getImageDescriptor(CommonImages.IMG_SIERRA_RE_SCAN));

    f_table.addSelectionListener(new SelectionListener() {
      public void widgetSelected(SelectionEvent e) {
        final boolean atLeastOneProjectSelected = !getSelectedScannedProjects().isEmpty();

        deleteProjectScansAction.setEnabled(atLeastOneProjectSelected);
        reScanProjectsAction.setEnabled(atLeastOneProjectSelected);
      }

      public void widgetDefaultSelected(SelectionEvent e) {
        // nothing to do
      }
    });

    f_view.addToViewMenu(new Separator());
    f_view.addToViewMenu(deleteDatabaseAction);
    f_view.addToViewMenu(new Separator());
    f_view.addToViewMenu(deleteProjectScansAction);
    f_view.addToViewMenu(reScanProjectsAction);

    f_view.addToActionBar(reScanProjectsAction);
    f_view.addToActionBar(deleteProjectScansAction);

    final Menu menu = new Menu(f_table.getShell(), SWT.POP_UP);
    f_table.setMenu(menu);

    // A bit of a hack because we can't just stick the action in the menu
    final MenuItem reScanProjectMenuItem = new MenuItem(menu, SWT.PUSH);
    reScanProjectMenuItem.setText(reScanProjectsAction.getText());
    reScanProjectMenuItem.setImage(SLImages.getImage(CommonImages.IMG_SIERRA_RE_SCAN));
    reScanProjectMenuItem.addListener(SWT.Selection, new Listener() {
      public void handleEvent(Event event) {
        reScanProjectsAction.run();
      }
    });

    // A bit of a hack because we can't just stick the action in the menu
    final MenuItem deleteProjectMenuItem = new MenuItem(menu, SWT.PUSH);
    deleteProjectMenuItem.setText(deleteProjectScansAction.getText());
    deleteProjectMenuItem.setImage(SLImages.getImage(CommonImages.IMG_RED_X));
    deleteProjectMenuItem.addListener(SWT.Selection, new Listener() {
      public void handleEvent(Event event) {
        deleteProjectScansAction.run();
      }
    });

    menu.addListener(SWT.Show, new Listener() {
      @Override
      public void handleEvent(final Event event) {
        deleteProjectMenuItem.setEnabled(deleteProjectScansAction.isEnabled());
      }
    });

    Projects.getInstance().addObserver(this);
    notify(Projects.getInstance());
  }

  public ArrayList<ScannedProject> getSelectedScannedProjects() {
    ArrayList<ScannedProject> result = new ArrayList<ScannedProject>();
    final TableItem[] selectedItems = f_table.getSelection();
    if (selectedItems != null) {
      for (TableItem item : selectedItems) {
        Object data = item.getData();
        if (data instanceof ScannedProject) {
          result.add((ScannedProject) data);
        }
      }
    }
    return result;
  }

  @Override
  public void dispose() {
    Projects.getInstance().removeObserver(this);
    super.dispose();
  }

  @Override
  public void notify(final Projects p) {
    /*
     * We are checking if there is anything in the database at all. If not we
     * show a helpful message, if so we display the scanned project information.
     */
    final boolean dataShouldShow = !p.isEmpty();

    // beware the thread context this method call might be made in.
    final UIJob job = new SLUIJob() {
      @Override
      public IStatus runInUIThread(IProgressMonitor monitor) {
        f_view.hasData(dataShouldShow);

        if (dataShouldShow)
          updateTableContents(p.getScannedProjects());

        return Status.OK_STATUS;
      }
    };
    job.schedule();
  }

  @Override
  public String getNoDataI18N() {
    return "sierra.eclipse.noDataProjectStatus";
  }

  @Override
  public Listener getNoDataListener() {
    return new Listener() {
      @Override
      public void handleEvent(final Event event) {
        new NewScanAction().run();
      }
    };
  }

  @Override
  public String getHelpId() {
    return "com.surelogic.sierra.client.eclipse.view-finding-details"; // TODO
  }

  @Override
  public void setFocus() {
    f_table.setFocus();
  }

  public void updateContentsForUI() {
    // nothing to do
  }

  void createTableColumns() {
    for (final String title : f_columnTitles) {
      final TableColumn tc = new TableColumn(f_table, SWT.NONE);
      tc.setText(title);
    }
  }

  void updateTableContents(final ArrayList<ScannedProject> scannedProjects) {
    if (f_table.isDisposed()) {
      return;
    }

    f_table.setRedraw(false);

    f_table.removeAll();

    final boolean hasFindings = scannedProjects.size() > 0;

    if (hasFindings) {
      // sort findings
      Collections.sort(scannedProjects);
    }

    for (final ScannedProject data : scannedProjects) {
      final TableItem item = new TableItem(f_table, SWT.NONE);

      item.setData(data);
      int ci = 0;
      item.setText(ci, data.getName());
      item.setImage(ci, SLImages.getImageForProject(data.getName()));
      ci++;
      item.setText(ci, SLUtility.toStringDayHMS(data.getWhenScanned()));
      ci++;
      Date priorScanTime = data.getWhenScannedPreviouslyOrNull();
      item.setText(ci, priorScanTime == null ? "" : SLUtility.toStringDayHMS(priorScanTime));
      ci++;
      item.setText(ci, data.getExclusionFilterOrEmptyString());
    }

    if (hasFindings)
      TableUtility.packColumns(f_table);

    f_table.setRedraw(true);
    /*
     * Fix to bug 1115 (an XP specific problem) where the table was redrawn with
     * lines through the row text. Aaron Silinskas found that a second call
     * seemed to fix the problem (with a bit of flicker).
     */
    if (SystemUtils.IS_OS_WINDOWS_XP) {
      f_table.setRedraw(true);
    }
  }
}
