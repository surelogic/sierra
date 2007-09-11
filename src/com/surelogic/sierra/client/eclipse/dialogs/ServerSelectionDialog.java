package com.surelogic.sierra.client.eclipse.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import com.surelogic.common.eclipse.SLImages;
import com.surelogic.sierra.client.eclipse.model.SierraServer;
import com.surelogic.sierra.client.eclipse.model.SierraServerManager;

public final class ServerSelectionDialog extends Dialog {

	private final SierraServerManager f_manager = SierraServerManager
			.getInstance();

	private final String f_projectName;

	private Table f_serverTable;

	private SierraServer f_server = null;

	public SierraServer getServer() {
		return f_server;
	}

	public ServerSelectionDialog(Shell parentShell, String projectName) {
		super(parentShell);
		f_projectName = projectName;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setImage(SLImages.getImage(SLImages.IMG_SIERRA_SERVER));
		newShell.setText("Select Sierra Server");
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite panel = (Composite) super.createDialogArea(parent);
		panel.setLayout(new GridLayout());

		final Label l = new Label(panel, SWT.WRAP);
		l.setText("Select a Sierra server to connect to the project '"
				+ f_projectName + "':");

		f_serverTable = new Table(panel, SWT.FULL_SELECTION);
		f_serverTable.setLayoutData(new GridData(GridData.FILL_BOTH));

		for (SierraServer server : f_manager.getServers()) {
			TableItem item = new TableItem(f_serverTable, SWT.NONE);
			item.setText(server.getLabel());
			item.setImage(SLImages.getImage(SLImages.IMG_SIERRA_SERVER));
			item.setData(server);
		}

		f_serverTable.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				final TableItem[] sa = f_serverTable.getSelection();
				if (sa.length > 0) {
					final TableItem selection = sa[0];
					f_server = (SierraServer) selection.getData();
				}
				setOKState();
			}
		});

		// add controls to composite as necessary
		return panel;
	}

	@Override
	protected Control createContents(Composite parent) {
		final Control contents = super.createContents(parent);
		setOKState();
		return contents;
	}

	private void setOKState() {
		getButton(IDialogConstants.OK_ID).setEnabled(f_server != null);
	}
}
