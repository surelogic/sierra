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

/**
 * Dialog to prompt the user for a user name and password to a Sierra server.
 */
public final class UserPasswordDialog extends Dialog {

	private final String f_serverName;
	private final String f_protocol;
	private final String f_host;
	private final int f_port;

	private String f_user = "";
	private Text f_userText;

	private String f_password = "";
	private Text f_passwordText;

	private boolean f_savePassword = false;
	private Button f_savePasswordButton;

	/**
	 * Constructs a new dialog to prompt the user for a user name and password
	 * for a particular Sierra server.
	 * 
	 * @param parentShell
	 *            a shell.
	 * @param serverName
	 *            the logical name of the Sierra server.
	 * @param protocol
	 *            the protocol <code>http</code> or <code>https</code>.
	 * @param host
	 *            a non-null host name or IP address that a Sierra server is
	 *            running on.
	 * @param port
	 *            the port the Sierra server is running on.
	 * @param user
	 *            a user name, or <code>null</code> if the user name is not
	 *            known.
	 */
	public UserPasswordDialog(Shell parentShell, String serverName,
			String protocol, String host, int port, String user) {
		super(parentShell);
		assert serverName != null;
		assert protocol != null;
		assert host != null;
		f_serverName = serverName;
		f_protocol = protocol;
		f_host = host;
		f_port = port;
		if (user != null)
			f_user = user;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setImage(SLImages.getImage(SLImages.IMG_SIERRA_LOGO));
		newShell.setText("Sierra Server Authentication");
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite orig = (Composite) super.createDialogArea(parent);

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		orig.setLayout(gridLayout);

		Label banner = new Label(orig, SWT.NONE);
		GridData data1 = new GridData(SWT.CENTER, SWT.CENTER, false, true, 1, 1);
		banner.setLayoutData(data1);
		banner.setImage(SLImages
				.getImage(SLImages.IMG_SIERRA_POWERED_BY_SURELOGIC));

		Composite panel = new Composite(orig, SWT.NONE);
		gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		panel.setLayout(gridLayout);

		final Label directions = new Label(panel, SWT.WRAP);
		GridData data = new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1);
		data.heightHint = 30;
		directions.setLayoutData(data);
		directions.setText("Enter your authentication for the server '"
				+ f_serverName + "'");

		final Label serverImg = new Label(panel, SWT.NONE);
		serverImg.setImage(SLImages.getImage(SLImages.IMG_SIERRA_SERVER));
		serverImg.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
				false));
		final Label serverlabel = new Label(panel, SWT.NONE);
		serverlabel.setText(f_protocol + "://" + f_host + " on port " + f_port);
		serverlabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false,
				true));

		final Label userLabel = new Label(panel, SWT.NONE);
		userLabel.setText("User:");
		// userLabel.setForeground(getShell().getDisplay().getSystemColor(
		// SWT.COLOR_BLUE));
		userLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
				false));
		f_userText = new Text(panel, SWT.SINGLE | SWT.BORDER);
		f_userText
				.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, true));
		f_userText.setText(f_user);

		final Label passwordLabel = new Label(panel, SWT.NONE);
		passwordLabel.setText("Password:");
		// passwordLabel.setForeground(getShell().getDisplay().getSystemColor(
		// SWT.COLOR_BLUE));
		passwordLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
				false));
		f_passwordText = new Text(panel, SWT.SINGLE | SWT.BORDER);
		f_passwordText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false,
				true));
		f_passwordText.setText("");
		f_passwordText.setEchoChar('\u25CF');

		f_savePasswordButton = new Button(panel, SWT.CHECK);
		f_savePasswordButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER,
				false, false, 2, 1));
		f_savePasswordButton.setText("Save Password");
		f_savePasswordButton.setSelection(f_savePassword);
		f_savePasswordButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				f_savePassword = f_savePasswordButton.getSelection();
			}
		});

		final Composite warning = new Composite(panel, SWT.NONE);
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
		saveWarning
				.setText("Saved secret data is stored on your computer in a format that's difficult, but not impossible, for an intruder to read.");

		return panel;
	}

	@Override
	protected void okPressed() {
		f_user = f_userText.getText();
		f_password = f_passwordText.getText();
		super.okPressed();
	}

	public String getUser() {
		return f_user;
	}

	public String getPassword() {
		return f_password;
	}

	public boolean savePassword() {
		return f_savePassword;
	}
}
