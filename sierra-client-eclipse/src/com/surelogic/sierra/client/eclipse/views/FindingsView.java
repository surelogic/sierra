package com.surelogic.sierra.client.eclipse.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

public class FindingsView extends AbstractSierraView<FindingsMediator> {

  public static final String ID = "com.surelogic.sierra.client.eclipse.views.FindingsView";

  @Override
  protected FindingsMediator createMorePartControls(final Composite findingPage) {
    GridLayout layout = new GridLayout();
    findingPage.setLayout(layout);
    GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
    findingPage.setLayoutData(layoutData);

    Composite c = new Composite(findingPage, SWT.NONE);
    c.setBackground(c.getDisplay().getSystemColor(SWT.COLOR_BLUE));
    c.setLayoutData(layoutData);

    return new FindingsMediator(this);
  }
}
