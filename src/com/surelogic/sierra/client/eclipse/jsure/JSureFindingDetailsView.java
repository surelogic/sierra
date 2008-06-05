package com.surelogic.sierra.client.eclipse.jsure;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;

import com.surelogic.common.eclipse.ViewUtility;
import com.surelogic.sierra.client.eclipse.views.AbstractSierraView;

public class JSureFindingDetailsView extends AbstractSierraView<JSureFindingDetailsMediator> {

	public static final String ID = "com.surelogic.sierra.client.eclipse.views.JSureFindingDetailsView";
	private static final int VIEW_DEPENDENT_ON_THIS = 0;
	private static final int VIEW_OWN_DEPENDENCIES = 1;
	
	public JSureFindingDetailsView() {
	  super(2);
	}
	
	@Override
	protected JSureFindingDetailsMediator createMorePartControls(Composite parent) {
		FillLayout layout = new FillLayout();
		parent.setLayout(layout);
		
    final TreeViewer tree = new TreeViewer(parent, SWT.SINGLE);
    final Menu contextMenu = new Menu(parent.getShell(), SWT.POP_UP);
    // TODO add menu items
    /*
    final MenuItem deleteServerItem = createMenuItem(contextMenu, "Delete",
        SLImages.getWorkbenchImage(ISharedImages.IMG_TOOL_DELETE));

    new MenuItem(contextMenu, SWT.SEPARATOR);
    */    
		return new JSureFindingDetailsMediator(this, parent); // FIX tree);
	}
		
	public static void findingSelected(long findingID, boolean moveFocus) {
		JSureFindingDetailsView view;
		if (moveFocus) {
			view = (JSureFindingDetailsView) ViewUtility.showView(ID);
		} else {
			view = (JSureFindingDetailsView) ViewUtility.showView(ID, null, IWorkbenchPage.VIEW_VISIBLE);
		}
		view.f_mediator.asyncQueryAndShow(findingID);
	}
}
