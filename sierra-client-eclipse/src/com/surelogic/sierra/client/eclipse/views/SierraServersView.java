package com.surelogic.sierra.client.eclipse.views;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

public final class SierraServersView extends AbstractSierraView<SierraServersMediator> {

  public static final String ID = "com.surelogic.sierra.client.eclipse.views.SierraServersView";

  public static final int INFO_WIDTH_HINT = 70;

  @Override
  protected SierraServersMediator createMorePartControls(final Composite parent) {
    final TreeViewer statusTree = new TreeViewer(parent, SWT.MULTI);
    return new SierraServersMediator(this, statusTree);
  }
}
