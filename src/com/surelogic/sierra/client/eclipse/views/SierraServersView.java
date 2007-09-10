package com.surelogic.sierra.client.eclipse.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
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
	public void createPartControl(Composite parent) {
		GridData data;

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		parent.setLayout(gridLayout);

		Composite rhs = new Composite(parent, SWT.NONE);
		data = new GridData(SWT.FILL, SWT.FILL, false, true);
		rhs.setLayoutData(data);
		gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		rhs.setLayout(gridLayout);

		Table serverList = new Table(rhs, SWT.FULL_SELECTION);
		serverList.setLayoutData(new GridData(GridData.FILL_BOTH));

		Composite c = new Composite(rhs, SWT.NONE);
		c.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		FillLayout fillLayout = new FillLayout(SWT.HORIZONTAL);
		c.setLayout(fillLayout);
		final ToolBar horizontalToolBar = new ToolBar(c, SWT.HORIZONTAL);
		final ToolItem newServer = new ToolItem(horizontalToolBar, SWT.PUSH);
		newServer.setImage(PlatformUI.getWorkbench().getSharedImages()
				.getImage(ISharedImages.IMG_TOOL_NEW_WIZARD));
		newServer.setToolTipText("New server location");
		final ToolItem duplicateServer = new ToolItem(horizontalToolBar,
				SWT.PUSH);
		duplicateServer.setImage(PlatformUI.getWorkbench().getSharedImages()
				.getImage(ISharedImages.IMG_TOOL_COPY));
		duplicateServer
				.setToolTipText("Duplicates the selected server location");
		final ToolItem deleteServer = new ToolItem(horizontalToolBar, SWT.PUSH);
		deleteServer.setImage(PlatformUI.getWorkbench().getSharedImages()
				.getImage(ISharedImages.IMG_TOOL_DELETE));
		deleteServer.setToolTipText("Deletes the selected server location");
		final Button editServer = new Button(c, SWT.NONE);
		editServer.setText("Edit");
		editServer.setToolTipText("Edit the selected server location");
		final Button openInBrowser = new Button(c, SWT.NONE);
		openInBrowser.setText("Open");
		openInBrowser
				.setToolTipText("Open the selected server in a Web browser");

		Label banner = new Label(rhs, SWT.NONE);
		banner
				.setLayoutData(new GridData(SWT.CENTER, SWT.BOTTOM, false,
						false));
		banner.setImage(SLImages
				.getImage(SLImages.IMG_SIERRA_POWERED_BY_SURELOGIC_SHORT));

		/*
		 * Server Information (left-hand side)
		 */

		final Group infoGroup = new Group(parent, SWT.NONE);
		infoGroup.setText("Server Information");
		infoGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		infoGroup.setLayout(gridLayout);

		final Label serverImg = new Label(infoGroup, SWT.NONE);
		serverImg.setImage(SLImages.getImage(SLImages.IMG_SIERRA_SERVER));
		serverImg.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
				false));
		final Label serverURL = new Label(infoGroup, SWT.NONE);
		serverURL
				.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		f_mediator = new SierraServersMediator(serverList, newServer,
				duplicateServer, deleteServer, editServer, openInBrowser,
				serverURL);
		f_mediator.init();
	}

	@Override
	public void setFocus() {
		f_mediator.setFocus();
	}
}
