package com.surelogic.sierra.client.eclipse.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISharedImages;

import com.surelogic.common.eclipse.SLImages;
import com.surelogic.sierra.client.eclipse.model.SierraServer;

/**
 * Dialog to prompt the user for a user name and password to a Sierra server.
 */
public final class ServerAuthenticationDialog extends Dialog {

	private final SierraServer f_server;

	private Text f_userText;
	private Text f_passwordText;
	private boolean f_savePassword = false;
	private Button f_savePasswordButton;

	/**
	 * Constructs a new dialog to prompt the user for a user name and password
	 * for a particular Sierra server.
	 * 
	 * @param parentShell
	 *            a shell.
	 * @param server
	 *            the information about the Sierra server.
	 */
	public ServerAuthenticationDialog(Shell parentShell,
			SierraServer server) {
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		assert server != null;
		f_server = server;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setImage(SLImages.getImage(SLImages.IMG_SIERRA_LOGO));
		newShell.setText("Sierra Server Authentication");
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		final Composite panel = (Composite) super.createDialogArea(parent);

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		panel.setLayout(gridLayout);

		Label banner = new Label(panel, SWT.NONE);
		banner.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, false, true, 1,
				1));
		banner.setImage(SLImages
				.getImage(SLImages.IMG_SIERRA_POWERED_BY_SURELOGIC));

		final Composite entryPanel = new Composite(panel, SWT.NONE);
		gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		entryPanel.setLayout(gridLayout);

		final Label directions = new Label(entryPanel, SWT.WRAP);
		GridData data = new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1);
		data.heightHint = 20;
		directions.setLayoutData(data);
		directions.setText("Enter your authentication for '"
				+ f_server.getLabel() + "'");

		final Label serverImg = new Label(entryPanel, SWT.NONE);
		serverImg.setImage(SLImages.getImage(SLImages.IMG_SIERRA_SERVER));
		data = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
		data.heightHint = 25;
		serverImg.setLayoutData(data);
		final Label serverlabel = new Label(entryPanel, SWT.NONE);
		serverlabel.setText(f_server.getProtocol() + "://" + f_server.getHost()
				+ " on port " + f_server.getPort());
		serverlabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false,
				true));

		final Label userLabel = new Label(entryPanel, SWT.NONE);
		userLabel.setText("User:");
		userLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
				false));
		f_userText = new Text(entryPanel, SWT.SINGLE | SWT.BORDER);
		f_userText
				.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, true));
		f_userText.setText(f_server.getUser());

		final Label passwordLabel = new Label(entryPanel, SWT.NONE);
		passwordLabel.setText("Password:");
		passwordLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
				false));
		f_passwordText = new Text(entryPanel, SWT.SINGLE | SWT.BORDER);
		f_passwordText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false,
				true));
		f_passwordText.setText("");
		f_passwordText.setEchoChar('\u25CF');

		f_savePasswordButton = new Button(entryPanel, SWT.CHECK);
		f_savePasswordButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER,
				false, false, 2, 1));
		f_savePasswordButton.setText("Save Password");
		f_savePasswordButton.setSelection(f_savePassword);
		f_savePasswordButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				f_savePassword = f_savePasswordButton.getSelection();
			}
		});

		final Composite warning = new Composite(entryPanel, SWT.NONE);
		warning.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false,
				2, 1));
		gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		warning.setLayout(gridLayout);
		final Label saveWarningImg = new Label(warning, SWT.NONE);
		saveWarningImg.setImage(SLImages
				.getWorkbenchImage(ISharedImages.IMG_OBJS_WARN_TSK));
		saveWarningImg.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false,
				false));
		final Label saveWarning = new Label(warning, SWT.WRAP);
		data = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		data.widthHint = 300;
		saveWarning.setLayoutData(data);
		saveWarning.setText(ServerLocationDialog.SAVE_PW_WARNING);

		return panel;
	}

	@Override
	protected void okPressed() {
		f_server.setUser(f_userText.getText());
		f_server.setPassword(f_passwordText.getText());
		f_server.setSavePassword(f_savePassword);
		super.okPressed();
	}

	public SierraServer getServer() {
		return f_server;
	}
}
