package com.surelogic.sierra.client.eclipse.views;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import com.surelogic.common.eclipse.SLImages;
import com.surelogic.sierra.client.eclipse.preferences.PreferenceConstants;
import com.surelogic.sierra.client.eclipse.wizards.ServerExportWizard;
import com.surelogic.sierra.client.eclipse.wizards.ServerImportWizard;

public final class SierraServersView extends AbstractSierraView<SierraServersMediator> {

	public static final String ID = "com.surelogic.sierra.client.eclipse.views.SierraServersView";

	public static final int INFO_WIDTH_HINT = 70;

	@Override
	protected SierraServersMediator createMorePartControls(final Composite parent) {
		final Tree statusTree = new Tree(parent, SWT.MULTI);
		
		final Action sortByServerAction = 
			new Action("Sort by Server", IAction.AS_CHECK_BOX) {
			@Override
			public void run() {
				f_mediator.setSortByServer(isChecked());				
			}
		};
		sortByServerAction
		.setChecked(ServerStatusSort.BY_SERVER == PreferenceConstants.getServerStatusSort());
		addToViewMenu(sortByServerAction);		

		final Menu serverListMenu = new Menu(parent.getShell(), SWT.POP_UP);
		final MenuItem newServerItem = createMenuItem(serverListMenu, "New...",
				SLImages.getWorkbenchImage(ISharedImages.IMG_TOOL_NEW_WIZARD));

		final MenuItem duplicateServerItem = createMenuItem(serverListMenu,
				"Duplicate", SLImages
						.getWorkbenchImage(ISharedImages.IMG_TOOL_COPY));

		final MenuItem deleteServerItem = createMenuItem(serverListMenu,
				"Delete", SLImages
						.getWorkbenchImage(ISharedImages.IMG_TOOL_DELETE));

		new MenuItem(serverListMenu, SWT.SEPARATOR);

		final MenuItem serverConnectItem = createMenuItem(serverListMenu,
				"Connect...", SLImages.IMG_SIERRA_SERVER);

		new MenuItem(serverListMenu, SWT.SEPARATOR);

		final MenuItem scanAllConnectedProjects = createMenuItem(
				serverListMenu, "Scan All Connected Projects",
				SLImages.IMG_SIERRA_SCAN);

		final MenuItem rescanAllConnectedProjects = createMenuItem(
				serverListMenu, "Re-Scan Changes in All Connected Projects",
				SLImages.IMG_SIERRA_SCAN_DELTA);

		final MenuItem synchAllConnectedProjects = createMenuItem(
				serverListMenu, "Synchronize All Connected Projects",
				SLImages.IMG_SIERRA_SYNC);

		new MenuItem(serverListMenu, SWT.SEPARATOR);
		final MenuItem sendResultFilters = new MenuItem(serverListMenu,
				SWT.PUSH);
		sendResultFilters.setText("Send Scan Filter...");
		final MenuItem getResultFilters = new MenuItem(serverListMenu, SWT.PUSH);
		getResultFilters.setText("Get Scan Filter...");
		new MenuItem(serverListMenu, SWT.SEPARATOR);
		final MenuItem serverPropertiesItem = new MenuItem(serverListMenu,
				SWT.PUSH);
		serverPropertiesItem.setText("Properties...");
		//serverList.setMenu(serverListMenu);

		final Menu projectListMenu = new Menu(parent.getShell(),
				SWT.POP_UP);
		final MenuItem projectConnectItem = createMenuItem(projectListMenu,
				"Connect...", SLImages.IMG_SIERRA_SERVER);
		final MenuItem scanProjectItem = createMenuItem(projectListMenu,
				"Scan Project", SLImages.IMG_SIERRA_SCAN);
		final MenuItem rescanProjectItem = createMenuItem(projectListMenu,
				"Re-Scan Changes in Project", SLImages.IMG_SIERRA_SCAN_DELTA);
		final MenuItem disconnectProjectItem = createMenuItem(projectListMenu,
				"Disconnect", SLImages.IMG_SIERRA_DISCONNECT);
		//projectList.setMenu(projectListMenu);

		final Action importAction = new Action("Import Locations...") {
			@Override
			public void run() {
				final ServerImportWizard wizard = new ServerImportWizard();
				wizard.init(PlatformUI.getWorkbench(), null);
				WizardDialog dialog = new WizardDialog(PlatformUI
						.getWorkbench().getActiveWorkbenchWindow().getShell(),
						wizard);
				dialog.open();
			}
		};
		addToViewMenu(importAction);
		final Action exportAction = new Action("Export Locations...") {
			@Override
			public void run() {
				final ServerExportWizard wizard = new ServerExportWizard();
				wizard.init(PlatformUI.getWorkbench(), null);
				WizardDialog dialog = new WizardDialog(PlatformUI
						.getWorkbench().getActiveWorkbenchWindow().getShell(),
						wizard);
				dialog.open();
			}
		};
		addToViewMenu(exportAction);

		return new SierraServersMediator(this, statusTree, 
				serverListMenu, newServerItem,
				duplicateServerItem, deleteServerItem, serverConnectItem,
				scanAllConnectedProjects, rescanAllConnectedProjects,
				synchAllConnectedProjects, sendResultFilters, getResultFilters,
				serverPropertiesItem, 
				projectListMenu, projectConnectItem, scanProjectItem,
				rescanProjectItem, disconnectProjectItem);
	}
}
