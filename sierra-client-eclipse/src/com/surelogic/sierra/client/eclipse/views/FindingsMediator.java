package com.surelogic.sierra.client.eclipse.views;

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import com.surelogic.common.ui.EclipseUIUtility;
import com.surelogic.sierra.client.eclipse.actions.NewScanAction;
import com.surelogic.sierra.client.eclipse.views.selection.FindingsSelectionView;

public class FindingsMediator extends AbstractSierraViewMediator implements IViewUpdater {

  protected FindingsMediator(final FindingsView view) {
    super(view);
  }

  @Override
  public void init() {
    super.init();
    updateContentsForUI();
  }

  @Override
  public void dispose() {
    super.dispose();
  }

  @Override
  public String getNoDataI18N() {
    return "sierra.eclipse.noDataFindings";
  }

  @Override
  public Listener getNoDataListener() {
    return new Listener() {
      @Override
      public void handleEvent(final Event event) {
        if ("view".equals(event.text))
          EclipseUIUtility.showView(FindingsSelectionView.ID);
        else
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
    // TODO

  }

  @Override
  public void updateContentsForUI() {
    f_view.hasData(false);
  }

}
