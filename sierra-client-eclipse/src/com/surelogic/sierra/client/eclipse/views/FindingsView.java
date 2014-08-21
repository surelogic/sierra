package com.surelogic.sierra.client.eclipse.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;

public class FindingsView extends AbstractSierraView<FindingsMediator> {

  public static final String ID = "com.surelogic.sierra.client.eclipse.views.FindingsView";

  @Override
  protected FindingsMediator createMorePartControls(final Composite findingsPage) {
    GridLayout layout = new GridLayout();
    layout.marginHeight = 0;
    layout.marginWidth = 0;
    layout.verticalSpacing = 0;
    findingsPage.setLayout(layout);

    final Table resultsTable = new Table(findingsPage, SWT.FULL_SELECTION | SWT.MULTI);
    resultsTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    resultsTable.setLinesVisible(true);

    return new FindingsMediator(this, findingsPage, resultsTable);
  }
}
