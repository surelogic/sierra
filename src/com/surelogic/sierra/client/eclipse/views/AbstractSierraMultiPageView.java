/*
 * Created on Jun 6, 2008
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package com.surelogic.sierra.client.eclipse.views;

import org.eclipse.swt.widgets.Composite;

public abstract class AbstractSierraMultiPageView<M extends IViewMediator>
extends AbstractSierraView<M> {
  protected AbstractSierraMultiPageView(int numPages) {
    super(numPages);
  }

  @Override
  protected M createMorePartControls(Composite parent) {
    throw new UnsupportedOperationException("Should not be called");
  }

  @Override
  protected abstract M createMorePartControls(Composite[] parents);
}
