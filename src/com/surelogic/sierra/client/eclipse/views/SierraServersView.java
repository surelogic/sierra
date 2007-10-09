package com.surelogic.sierra.client.eclipse.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.part.ViewPart;

import com.surelogic.common.eclipse.SLImages;

public final class SierraServersView extends ViewPart {

	public static final int INFO_WIDTH_HINT = 70;

	private SierraServersMediator f_mediator = null;

	@Override
	public void dispose() {
		if (f_mediator != null)
			f_mediator.dispose();
		super.dispose();
	}

	@Override
	public void createPartControl(final Composite parent) {
		GridData data;

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		parent.setLayout(gridLayout);

		final Composite rhs = new Composite(parent, SWT.NONE);
		data = new GridData(SWT.FILL, SWT.FILL, false, true);
		rhs.setLayoutData(data);
		gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		rhs.setLayout(gridLayout);

		Table serverList = new Table(rhs, SWT.FULL_SELECTION);
		serverList.setLayoutData(new GridData(GridData.FILL_BOTH));

		final Menu serverListMenu = new Menu(serverList.getShell(), SWT.POP_UP);
		final MenuItem newServerItem = new MenuItem(serverListMenu, SWT.PUSH);
		newServerItem.setImage(SLImages
				.getWorkbenchImage(ISharedImages.IMG_TOOL_NEW_WIZARD));
		newServerItem.setText("New...");
		final MenuItem duplicateServerItem = new MenuItem(serverListMenu,
				SWT.PUSH);
		duplicateServerItem.setImage(SLImages
				.getWorkbenchImage(ISharedImages.IMG_TOOL_COPY));
		duplicateServerItem.setText("Duplicate");
		final MenuItem deleteServerItem = new MenuItem(serverListMenu, SWT.PUSH);
		deleteServerItem.setImage(SLImages
				.getWorkbenchImage(ISharedImages.IMG_TOOL_DELETE));
		deleteServerItem.setText("Delete");
		new MenuItem(serverListMenu, SWT.SEPARATOR);
		final MenuItem serverPropertiesItem = new MenuItem(serverListMenu,
				SWT.PUSH);
		serverPropertiesItem.setText("Properties...");
		serverList.setMenu(serverListMenu);

		Composite c = new Composite(rhs, SWT.NONE);
		c.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		FillLayout fillLayout = new FillLayout(SWT.HORIZONTAL);
		c.setLayout(fillLayout);
		final ToolBar horizontalToolBar = new ToolBar(c, SWT.HORIZONTAL);
		final ToolItem newServer = new ToolItem(horizontalToolBar, SWT.PUSH);
		newServer.setImage(SLImages
				.getWorkbenchImage(ISharedImages.IMG_TOOL_NEW_WIZARD));
		newServer.setToolTipText("New team server location");
		final ToolItem duplicateServer = new ToolItem(horizontalToolBar,
				SWT.PUSH);
		duplicateServer.setImage(SLImages
				.getWorkbenchImage(ISharedImages.IMG_TOOL_COPY));
		duplicateServer
				.setToolTipText("Duplicates the selected team server location");
		final ToolItem deleteServer = new ToolItem(horizontalToolBar, SWT.PUSH);
		deleteServer.setImage(SLImages
				.getWorkbenchImage(ISharedImages.IMG_TOOL_DELETE));
		deleteServer
				.setToolTipText("Deletes the selected team server location");
		final Button openInBrowser = new Button(c, SWT.NONE);
		openInBrowser.setText("Browse");
		openInBrowser
				.setToolTipText("Open the selected team server in a Web browser");

		final Label banner = new Label(rhs, SWT.NONE);
		banner
				.setLayoutData(new GridData(SWT.CENTER, SWT.BOTTOM, false,
						false));
		banner.setImage(SLImages
				.getImage(SLImages.IMG_SIERRA_POWERED_BY_SURELOGIC_SHORT));
		/*
		 * Make the banner small if the view isn't given many vertical pixels.
		 */
		rhs.addListener(SWT.Resize, new Listener() {
			public void handleEvent(Event e) {
				final Point size = rhs.getSize();
				final Image image;
				if (size.y < 150) {
					image = SLImages
							.getImage(SLImages.IMG_SIERRA_POWERED_BY_SURELOGIC_REALLY_SHORT);
				} else {
					image = SLImages
							.getImage(SLImages.IMG_SIERRA_POWERED_BY_SURELOGIC_SHORT);
				}
				banner.setImage(image);
				parent.layout();
			}
		});

		/*
		 * Server Information (left-hand side)
		 */

		final Composite infoGroup = new Composite(parent, SWT.NONE);
		infoGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		infoGroup.setLayout(gridLayout);

		final Label serverImg = new Label(infoGroup, SWT.NONE);
		serverImg.setImage(SLImages.getImage(SLImages.IMG_SIERRA_SERVER));
		serverImg.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
				false));
		final Label serverURL = new Label(infoGroup, SWT.WRAP);
		serverURL
				.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		final Group projectsGroup = new Group(infoGroup, SWT.NONE);
		projectsGroup.setText("Connected Projects");
		projectsGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
				true, 2, 1));
		gridLayout = new GridLayout();
		projectsGroup.setLayout(gridLayout);

		Table projectList = new Table(projectsGroup, SWT.MULTI);
		projectList.setLayoutData(new GridData(GridData.FILL_BOTH));

		final Menu projectListMenu = new Menu(projectList.getShell(),
				SWT.POP_UP);
		final MenuItem connectProjectItem = new MenuItem(projectListMenu,
				SWT.PUSH);
		connectProjectItem.setText("Connect...");
		connectProjectItem.setImage(SLImages
				.getImage(SLImages.IMG_SIERRA_SERVER));
		final MenuItem disconnectProjectItem = new MenuItem(projectListMenu,
				SWT.PUSH);
		disconnectProjectItem.setText("Disconnect");
		disconnectProjectItem.setImage(SLImages
				.getImage(SLImages.IMG_SIERRA_DISCONNECT));
		projectList.setMenu(projectListMenu);

		f_mediator = new SierraServersMediator(serverList, newServer,
				duplicateServer, deleteServer, newServerItem,
				duplicateServerItem, deleteServerItem, serverPropertiesItem,
				openInBrowser, infoGroup, serverURL, projectList,
				connectProjectItem, disconnectProjectItem);
		f_mediator.init();
	}

	@Override
	public void setFocus() {
		f_mediator.setFocus();
	}
}
