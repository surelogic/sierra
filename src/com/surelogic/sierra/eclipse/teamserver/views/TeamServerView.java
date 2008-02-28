package com.surelogic.sierra.eclipse.teamserver.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.part.ViewPart;

import com.surelogic.common.eclipse.SLImages;

public class TeamServerView extends ViewPart {

	private TeamServerMediator f_mediator = null;

	public TeamServerView() {
	}

	public void createPartControl(Composite parent) {
		GridData data;

		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		parent.setLayout(gridLayout);

		final Button command = new Button(parent, SWT.NONE);
		data = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 3);
		command.setLayoutData(data);

		final Link status = new Link(parent, SWT.NONE);
		data = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
		status.setLayoutData(data);

		final Label hostLabel = new Label(parent, SWT.RIGHT);
		hostLabel.setText("Host:");
		data = new GridData(SWT.FILL, SWT.CENTER, false, false);
		hostLabel.setLayoutData(data);

		final Text host = new Text(parent, SWT.SINGLE);
		host.setEditable(false); // can't change the text
		data = new GridData(SWT.FILL, SWT.CENTER, true, false);
		host.setLayoutData(data);

		final Label portLabel = new Label(parent, SWT.RIGHT);
		portLabel.setText("Port:");
		data = new GridData(SWT.FILL, SWT.CENTER, false, false);
		portLabel.setLayoutData(data);

		final Text port = new Text(parent, SWT.SINGLE);
		port.setText("13376");
		port.setEditable(false); // can't change the port :-(
		data = new GridData(SWT.FILL, SWT.CENTER, true, false);
		port.setLayoutData(data);

		final Group statusGroup = new Group(parent, SWT.NONE);
		statusGroup.setText("Status");
		data = new GridData(SWT.FILL, SWT.FILL, false, true);
		data.widthHint = 120;
		statusGroup.setLayoutData(data);
		statusGroup.setLayout(new FillLayout());

		final Canvas trafficLight = new Canvas(statusGroup, SWT.NONE);

		final Group logGroup = new Group(parent, SWT.NONE);
		logGroup.setText("Log");
		data = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
		logGroup.setLayoutData(data);
		final GridLayout logLayout = new GridLayout();
		logLayout.numColumns = 2;
		logGroup.setLayout(logLayout);

		final ToolBar logBar = new ToolBar(logGroup, SWT.VERTICAL);
		data = new GridData(SWT.CENTER, SWT.TOP, false, true);
		logBar.setLayoutData(data);
		final ToolItem jettyRequestLogItem = new ToolItem(logBar, SWT.RADIO);
		jettyRequestLogItem.setImage(SLImages.getImage(SLImages.IMG_JETTY_LOG));
		jettyRequestLogItem.setToolTipText("Show Jetty Request Log");
		final ToolItem portalLogItem = new ToolItem(logBar, SWT.RADIO);
		portalLogItem.setImage(SLImages.getImage(SLImages.IMG_SIERRA_LOGO));
		portalLogItem.setToolTipText("Show Sierra Portal Log");
		final ToolItem servicesLogItem = new ToolItem(logBar, SWT.RADIO);
		servicesLogItem.setImage(SLImages.getImage(SLImages.IMG_SIERRA_SYNC));
		servicesLogItem.setToolTipText("Show Sierra Client Services Log");

		final Text log = new Text(logGroup, SWT.MULTI | SWT.H_SCROLL
				| SWT.V_SCROLL);
		log.setEditable(false);
		data = new GridData(SWT.FILL, SWT.FILL, true, true);
		log.setLayoutData(data);

		f_mediator = new TeamServerMediator(command, status, host, port,
				trafficLight, jettyRequestLogItem, portalLogItem,
				servicesLogItem, log);
		f_mediator.init();
	}

	@Override
	public void dispose() {
		if (f_mediator != null)
			f_mediator.dispose();
		super.dispose();
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	@Override
	public void setFocus() {
		if (f_mediator != null)
			f_mediator.setFocus();
	}
}