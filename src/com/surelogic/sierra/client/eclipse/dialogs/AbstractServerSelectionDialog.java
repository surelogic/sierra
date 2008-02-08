package com.surelogic.sierra.client.eclipse.dialogs;

import java.util.logging.Level;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import com.surelogic.common.eclipse.Activator;
import com.surelogic.common.eclipse.SLImages;
import com.surelogic.common.eclipse.dialogs.ExceptionDetailsDialog;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.client.eclipse.model.SierraServer;
import com.surelogic.sierra.client.eclipse.model.SierraServerManager;

public abstract class AbstractServerSelectionDialog extends Dialog {

	private final SierraServerManager f_manager = SierraServerManager
			.getInstance();

	private Table f_serverTable;

	private SierraServer f_server = null;

	private final String label;

	public final SierraServer getServer() {
		return f_server;
	}

	protected AbstractServerSelectionDialog(Shell parentShell, String label) {
		super(parentShell);
		this.label = label;
		setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX);
	}

	@Override
	protected final void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setImage(SLImages.getImage(SLImages.IMG_SIERRA_SERVER));
		newShell.setText("Select Sierra Server");
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite panel = (Composite) super.createDialogArea(parent);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		panel.setLayout(gridLayout);

		Label banner = new Label(panel, SWT.NONE);
		banner.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, false, true, 1,
				1));
		banner.setImage(SLImages
				.getImage(SLImages.IMG_SIERRA_POWERED_BY_SURELOGIC));

		final Composite entryPanel = new Composite(panel, SWT.NONE);
		entryPanel.setLayoutData(new GridData(GridData.FILL_BOTH));
		gridLayout = new GridLayout();
		entryPanel.setLayout(gridLayout);

		final Label l = new Label(entryPanel, SWT.WRAP);
		l.setText(label);

		f_serverTable = new Table(entryPanel, SWT.FULL_SELECTION);
		f_serverTable.setLayoutData(new GridData(GridData.FILL_BOTH));

		for (SierraServer server : f_manager.getServers()) {
			TableItem item = new TableItem(f_serverTable, SWT.NONE);
			item.setText(server.getLabel());
			item.setImage(SLImages.getImage(SLImages.IMG_SIERRA_SERVER));
			item.setData(server);
		}

		addToEntryPanel(entryPanel);

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

	protected void addToEntryPanel(Composite entryPanel) {
		// Do nothing
	}

	@Override
	protected final Control createContents(Composite parent) {
		final Control contents = super.createContents(parent);
		setOKState();
		return contents;
	}

	private final void setOKState() {
		getButton(IDialogConstants.OK_ID).setEnabled(f_server != null);
	}

	public final boolean confirmNonnullServer() {
		if (f_server == null) {
			final String msg = I18N.err(18);
			final ExceptionDetailsDialog report = new ExceptionDetailsDialog(
					getParentShell(), "Sierra server must be non-null", null,
					msg, new Exception(), Activator.getDefault());
			report.open();
			SLLogger.getLogger().log(Level.SEVERE, msg);
			return false;
		}
		return true;
	}
}
