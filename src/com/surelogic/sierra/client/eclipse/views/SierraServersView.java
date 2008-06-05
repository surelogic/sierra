package com.surelogic.sierra.client.eclipse.views;

import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.ISharedImages;

import com.surelogic.common.eclipse.SLImages;
import com.surelogic.common.images.CommonImages;

public final class SierraServersView extends
		AbstractSierraView<SierraServersMediator> {

	public static final String ID = "com.surelogic.sierra.client.eclipse.views.SierraServersView";

	public static final int INFO_WIDTH_HINT = 70;

	@Override
	protected SierraServersMediator createMorePartControls(
			final Composite parent) {
		final TreeViewer statusTree = new TreeViewer(parent, SWT.MULTI);
		final Menu contextMenu = new Menu(parent.getShell(), SWT.POP_UP);

		final MenuItem newServerItem = createMenuItem(contextMenu, "New...",
				SLImages.getWorkbenchImage(ISharedImages.IMG_TOOL_NEW_WIZARD));
		final MenuItem browseServerItem = createMenuItem(contextMenu, "Browse",
				SLImages.getImage(CommonImages.IMG_SIERRA_SERVER));

		final MenuItem duplicateServerItem = createMenuItem(contextMenu,
				"Duplicate", SLImages
						.getWorkbenchImage(ISharedImages.IMG_TOOL_COPY));

		final MenuItem deleteServerItem = createMenuItem(contextMenu, "Delete",
				SLImages.getWorkbenchImage(ISharedImages.IMG_TOOL_DELETE));

		new MenuItem(contextMenu, SWT.SEPARATOR);

		final MenuItem serverConnectItem = createMenuItem(contextMenu,
				"Connect...", CommonImages.IMG_SIERRA_SERVER);
		new MenuItem(contextMenu, SWT.SEPARATOR);

		final MenuItem scanProjectItem = createMenuItem(contextMenu,
				"Scan Project", CommonImages.IMG_SIERRA_SCAN);
		final MenuItem rescanProjectItem = createMenuItem(contextMenu,
				"Re-Scan Changes in Project",
				CommonImages.IMG_SIERRA_SCAN_DELTA);
		final MenuItem synchProjects = createMenuItem(contextMenu,
				"Synchronize Projects", CommonImages.IMG_SIERRA_SYNC);

		new MenuItem(contextMenu, SWT.SEPARATOR);
		final MenuItem publishScansItem = createMenuItem(contextMenu,
				"Publish Scans", CommonImages.IMG_SIERRA_PUBLISH);

		final MenuItem disconnectProjectItem = createMenuItem(contextMenu,
				"Disconnect", CommonImages.IMG_SIERRA_DISCONNECT);

		new MenuItem(contextMenu, SWT.SEPARATOR);
		final MenuItem sendResultFilters = createMenuItem(contextMenu,
				"Send Scan Filter...", CommonImages.IMG_FILTER);
		final MenuItem getResultFilters = new MenuItem(contextMenu, SWT.PUSH);
		getResultFilters.setText("Get Scan Filter...");
		new MenuItem(contextMenu, SWT.SEPARATOR);
		final MenuItem serverPropertiesItem = new MenuItem(contextMenu,
				SWT.PUSH);
		serverPropertiesItem.setText("Server Properties...");
		statusTree.getTree().setMenu(contextMenu);

		return new SierraServersMediator(this, statusTree, contextMenu,
				newServerItem, browseServerItem, duplicateServerItem,
				deleteServerItem, serverConnectItem, synchProjects,
				sendResultFilters, getResultFilters, serverPropertiesItem,
				scanProjectItem, rescanProjectItem, publishScansItem,
				disconnectProjectItem);
	}
}
