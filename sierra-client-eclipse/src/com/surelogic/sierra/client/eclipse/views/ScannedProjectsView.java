package com.surelogic.sierra.client.eclipse.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;

public class ScannedProjectsView extends AbstractSierraView<ScannedProjectsMediator> {

  public static final String ID = "com.surelogic.sierra.client.eclipse.views.ScannedProjectsView";

  @Override
  protected ScannedProjectsMediator createMorePartControls(final Composite findingsPage) {
    findingsPage.setLayout(new FillLayout());

    final Table resultsTable = new Table(findingsPage, SWT.FULL_SELECTION | SWT.MULTI);
    resultsTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    resultsTable.setLinesVisible(true);

    return new ScannedProjectsMediator(this, resultsTable);
  }
}
