package com.surelogic.sierra.client.eclipse.views;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

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
				SLImages.getImage(CommonImages.IMG_EDIT_NEW));
		final MenuItem browseServerItem = createMenuItem(contextMenu, "Browse",
				SLImages.getImage(CommonImages.IMG_SIERRA_SERVER));

		final MenuItem duplicateServerItem = createMenuItem(contextMenu,
				"Duplicate", SLImages.getImage(CommonImages.IMG_EDIT_COPY));

		final MenuItem deleteServerItem = createMenuItem(contextMenu, "Delete",
				SLImages.getImage(CommonImages.IMG_EDIT_DELETE));

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
				"Synchronize Project", CommonImages.IMG_SIERRA_SYNC);

		new MenuItem(contextMenu, SWT.SEPARATOR);
		final MenuItem publishScansItem = createMenuItem(contextMenu,
				"Publish Latest Scan", CommonImages.IMG_SIERRA_PUBLISH);

		final MenuItem disconnectProjectItem = createMenuItem(contextMenu,
				"Disconnect", CommonImages.IMG_SIERRA_DISCONNECT);

		new MenuItem(contextMenu, SWT.SEPARATOR);
		final MenuItem sendResultFilters = createMenuItem(contextMenu,
				"Send Local Scan Filter As ...", CommonImages.IMG_FILTER);
		final MenuItem getResultFilters = new MenuItem(contextMenu, SWT.PUSH);
		getResultFilters.setText("Overwrite Local Scan Filter");
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
