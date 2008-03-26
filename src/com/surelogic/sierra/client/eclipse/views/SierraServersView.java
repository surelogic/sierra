package com.surelogic.sierra.client.eclipse.views;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import com.surelogic.common.eclipse.SLImages;
import com.surelogic.sierra.client.eclipse.preferences.PreferenceConstants;
import com.surelogic.sierra.client.eclipse.wizards.ServerExportWizard;
import com.surelogic.sierra.client.eclipse.wizards.ServerImportWizard;

public final class SierraServersView extends
		AbstractSierraView<SierraServersMediator> {

	public static final String ID = "com.surelogic.sierra.client.eclipse.views.SierraServersView";

	public static final int INFO_WIDTH_HINT = 70;

	@Override
	protected SierraServersMediator createMorePartControls(
			final Composite parent) {
		final Tree statusTree = new Tree(parent, SWT.MULTI);
		final Menu contextMenu = new Menu(parent.getShell(), SWT.POP_UP);

		final MenuItem newServerItem = createMenuItem(contextMenu, "New...",
				SLImages.getWorkbenchImage(ISharedImages.IMG_TOOL_NEW_WIZARD));
		final MenuItem browseServerItem = createMenuItem(contextMenu, "Browse",
				SLImages.getImage(SLImages.IMG_SIERRA_SERVER));

		final MenuItem duplicateServerItem = createMenuItem(contextMenu,
				"Duplicate", SLImages
						.getWorkbenchImage(ISharedImages.IMG_TOOL_COPY));

		final MenuItem deleteServerItem = createMenuItem(contextMenu, "Delete",
				SLImages.getWorkbenchImage(ISharedImages.IMG_TOOL_DELETE));

		new MenuItem(contextMenu, SWT.SEPARATOR);

		final MenuItem serverConnectItem = createMenuItem(contextMenu,
				"Connect...", SLImages.IMG_SIERRA_SERVER);
		new MenuItem(contextMenu, SWT.SEPARATOR);

		final MenuItem scanProjectItem = createMenuItem(contextMenu,
				"Scan Project", SLImages.IMG_SIERRA_SCAN);
		final MenuItem rescanProjectItem = createMenuItem(contextMenu,
				"Re-Scan Changes in Project", SLImages.IMG_SIERRA_SCAN_DELTA);
		final MenuItem synchProjects = createMenuItem(contextMenu,
				"Synchronize Projects", SLImages.IMG_SIERRA_SYNC);

		new MenuItem(contextMenu, SWT.SEPARATOR);
		final MenuItem disconnectProjectItem = createMenuItem(contextMenu,
				"Disconnect", SLImages.IMG_SIERRA_DISCONNECT);

		new MenuItem(contextMenu, SWT.SEPARATOR);
		final MenuItem sendResultFilters = 
			createMenuItem(contextMenu, "Send Scan Filter...", SLImages.IMG_FILTER);
		final MenuItem getResultFilters = new MenuItem(contextMenu, SWT.PUSH);
		getResultFilters.setText("Get Scan Filter...");
		new MenuItem(contextMenu, SWT.SEPARATOR);
		final MenuItem serverPropertiesItem = new MenuItem(contextMenu,
				SWT.PUSH);
		serverPropertiesItem.setText("Properties...");
		statusTree.setMenu(contextMenu);

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
		addToViewMenu(new Separator());

		final ServerStatusSort sort = PreferenceConstants.getServerStatusSort();
		final Action sortByServerAction = 
			new Action("Show by Team Server", IAction.AS_RADIO_BUTTON) {
			@Override
			public void run() {
				f_mediator.setSortByServer(isChecked());
			}
		};
		final Action sortByProjectAction = 
			new Action("Show by Project", IAction.AS_RADIO_BUTTON) {
			@Override
			public void run() {
				f_mediator.setSortByServer(!isChecked());
			}
		};
		sortByServerAction.setChecked(ServerStatusSort.BY_SERVER == sort);
		sortByProjectAction.setChecked(ServerStatusSort.BY_PROJECT == sort);
		addToViewMenu(sortByProjectAction);
		addToViewMenu(sortByServerAction);
		
		return new SierraServersMediator(this, statusTree, contextMenu,
				newServerItem, browseServerItem, duplicateServerItem, deleteServerItem,
				serverConnectItem, synchProjects, sendResultFilters,
				getResultFilters, serverPropertiesItem, scanProjectItem,
				rescanProjectItem, disconnectProjectItem);
	}
}
