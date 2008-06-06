package com.surelogic.sierra.client.eclipse.jsure;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;

import com.surelogic.common.eclipse.ViewUtility;
import com.surelogic.sierra.client.eclipse.views.*;
import com.surelogic.sierra.jdbc.finding.FindingDetail;

public class JSureFindingDetailsView extends AbstractSierraMultiPageView<JSureFindingDetailsMediator> {

	public static final String ID = "com.surelogic.sierra.client.eclipse.views.JSureFindingDetailsView";
	
	public JSureFindingDetailsView() {
	  super(2);
	}
	
	@Override
	protected JSureFindingDetailsMediator createMorePartControls(Composite[] parents) {
	  for(Composite parent : parents) {
	    final TreeViewer tree = new TreeViewer(parent, SWT.SINGLE);
	    // TODO add menu items
	    /*
    final Menu contextMenu = new Menu(parent.getShell(), SWT.POP_UP);

    final MenuItem deleteServerItem = createMenuItem(contextMenu, "Delete",
        SLImages.getWorkbenchImage(ISharedImages.IMG_TOOL_DELETE));

    new MenuItem(contextMenu, SWT.SEPARATOR);
	     */    
	  }
		return new JSureFindingDetailsMediator(this, parents); // FIX tree);
	}
		
	public static void findingSelected(FindingDetail detail, boolean moveFocus) {
		JSureFindingDetailsView view;
		if (moveFocus) {
			view = (JSureFindingDetailsView) ViewUtility.showView(ID);
		} else {
			view = (JSureFindingDetailsView) ViewUtility.showView(ID, null, IWorkbenchPage.VIEW_VISIBLE);
		}
		view.f_mediator.asyncQueryAndShow(detail);
	}
}
