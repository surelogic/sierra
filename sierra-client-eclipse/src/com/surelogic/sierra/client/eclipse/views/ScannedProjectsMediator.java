package com.surelogic.sierra.client.eclipse.views;

import java.util.ArrayList;
import java.util.Collections;

import org.apache.commons.lang3.SystemUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.progress.UIJob;

import com.surelogic.common.SLUtility;
import com.surelogic.common.ui.SLImages;
import com.surelogic.common.ui.TableUtility;
import com.surelogic.common.ui.jobs.SLUIJob;
import com.surelogic.sierra.client.eclipse.actions.NewScanAction;
import com.surelogic.sierra.client.eclipse.model.IProjectsObserver;
import com.surelogic.sierra.client.eclipse.model.Projects;
import com.surelogic.sierra.client.eclipse.model.ScannedProject;
import com.surelogic.sierra.client.eclipse.model.selection.SelectionManager;

public class ScannedProjectsMediator extends AbstractSierraViewMediator implements IViewUpdater, IProjectsObserver {

  final SelectionManager f_manager = SelectionManager.getInstance();

  final ScannedProjectsView f_view;
  final Table f_table;
  final String[] f_columnTitles = { "Time", "Project", "Exclusion Specification (surelogic-tools.properties)" };

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

    Projects.getInstance().addObserver(this);
    notify(Projects.getInstance());
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
        if (!f_view.matchesStatus(dataShouldShow)) {
          /*
           * Only gets run when the page actually has changed.
           */
          f_view.hasData(dataShouldShow);

          if (dataShouldShow) {
            updateTableContents(p.getScannedProjects());
          }
        }
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
      item.setText(ci, SLUtility.toStringDayHMS(data.getWhenScanned()));
      ci++;
      item.setText(ci, data.getName());
      item.setImage(ci, SLImages.getImageForProject(data.getName()));
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
