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
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;

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
		command.setText("Start Server");
		data = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 2);
		command.setLayoutData(data);

		final Label status = new Label(parent, SWT.NONE);
		status.setText("Running...");
		data = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
		status.setLayoutData(data);

		final Label portLabel = new Label(parent, SWT.RIGHT);
		portLabel.setText("Port:");
		data = new GridData(SWT.FILL, SWT.CENTER, false, false);
		portLabel.setLayoutData(data);

		final Text port = new Text(parent, SWT.SINGLE);
		port.setText("13376");
		data = new GridData(SWT.FILL, SWT.CENTER, true, false);
		port.setLayoutData(data);

		final Group statusGroup = new Group(parent, SWT.NONE);
		statusGroup.setText("Status");
		data = new GridData(SWT.FILL, SWT.FILL, false, true);
		data.widthHint = 100;
		statusGroup.setLayoutData(data);
		statusGroup.setLayout(new FillLayout());

		final Canvas trafficLight = new Canvas(statusGroup, SWT.NONE);

		final Group logGroup = new Group(parent, SWT.NONE);
		logGroup.setText("Log");
		data = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
		logGroup.setLayoutData(data);
		logGroup.setLayout(new FillLayout());

		final Text log = new Text(logGroup, SWT.MULTI);

		f_mediator = new TeamServerMediator(command, status, port,
				trafficLight, log);
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