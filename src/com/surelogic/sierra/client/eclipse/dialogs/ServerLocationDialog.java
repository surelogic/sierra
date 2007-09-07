package com.surelogic.sierra.client.eclipse.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISharedImages;

import com.surelogic.common.eclipse.SLImages;
import com.surelogic.sierra.client.eclipse.model.SierraServerModel;

/**
 * Dialog to allow the user to enter or edit the location and authentication
 * information for a Sierra server.
 */
public final class ServerLocationDialog extends Dialog {

	public static final String SAVE_PW_WARNING = "Saved secret data is stored on your computer in a format that's difficult, but not impossible, for an intruder to read.";

	public static final int INFO_WIDTH_HINT = 70;

	private final SierraServerModel f_server;

	private final boolean f_newLocation;

	private Label f_serverURL;

	/**
	 * Constructs a new dialog to allow the user to enter or edit the location
	 * and authentication information for a Sierra server.
	 * 
	 * @param parentShell
	 *            a shell.
	 * @param server
	 *            the information about the Sierra server.
	 * @param newLocation
	 *            indicates that this location was just created. This controls
	 *            the dialog title.
	 */
	public ServerLocationDialog(Shell parentShell, SierraServerModel server,
			boolean newLocation) {
		super(parentShell);
		assert server != null;
		f_server = server;
		f_newLocation = newLocation;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setImage(SLImages.getImage(SLImages.IMG_SIERRA_LOGO));
		newShell.setText((f_newLocation ? "New " : "Edit")
				+ "Sierra Server Location");
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
		data.heightHint = 40;
		directions.setLayoutData(data);
		directions.setText("Enter your authentication for the server '"
				+ f_server.getName() + "'");

		final Label serverImg = new Label(entryPanel, SWT.NONE);
		serverImg.setImage(SLImages.getImage(SLImages.IMG_SIERRA_SERVER));
		serverImg.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
				false));
		final Label f_serverURL = new Label(entryPanel, SWT.NONE);
		f_serverURL.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false,
				true));

		final Group locGroup = new Group(entryPanel, SWT.NONE);
		locGroup.setText("Location");
		locGroup.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2,
				1));
		gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		locGroup.setLayout(gridLayout);

		final Label hostLabel = new Label(locGroup, SWT.RIGHT);
		hostLabel.setText("Host:");
		data = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
		data.widthHint = INFO_WIDTH_HINT;
		hostLabel.setLayoutData(data);
		Text hostText = new Text(locGroup, SWT.SINGLE | SWT.BORDER);
		hostText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		final Label portLabel = new Label(locGroup, SWT.RIGHT);
		portLabel.setText("Port:");
		portLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
				false));
		Text portText = new Text(locGroup, SWT.SINGLE | SWT.BORDER);
		portText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		final Label protLabel = new Label(locGroup, SWT.RIGHT);
		protLabel.setText("Protocol:");
		protLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
				false));

		Composite radio = new Composite(locGroup, SWT.NONE);
		FillLayout fill = new FillLayout(SWT.VERTICAL);
		radio.setLayout(fill);
		Button http = new Button(radio, SWT.RADIO);
		http.setText("http");
		Button https = new Button(radio, SWT.RADIO);
		https.setText("https");

		final Group authGroup = new Group(entryPanel, SWT.NONE);
		authGroup.setText("Authentication");
		authGroup.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2,
				1));
		gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		authGroup.setLayout(gridLayout);

		final Label userLabel = new Label(authGroup, SWT.RIGHT);
		userLabel.setText("User:");
		// userLabel.setForeground(getShell().getDisplay().getSystemColor(
		// SWT.COLOR_BLUE));
		data = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
		data.widthHint = INFO_WIDTH_HINT;
		userLabel.setLayoutData(data);
		Text f_userText = new Text(authGroup, SWT.SINGLE | SWT.BORDER);
		f_userText
				.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		final Label passwordLabel = new Label(authGroup, SWT.RIGHT);
		passwordLabel.setText("Password:");
		// passwordLabel.setForeground(getShell().getDisplay().getSystemColor(
		// SWT.COLOR_BLUE));
		passwordLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
				false));
		Text f_passwordText = new Text(authGroup, SWT.SINGLE | SWT.BORDER);
		f_passwordText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false));
		f_passwordText.setText("");
		f_passwordText.setEchoChar('\u25CF');

		Button f_savePasswordButton = new Button(authGroup, SWT.CHECK);
		f_savePasswordButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER,
				false, false, 2, 1));
		f_savePasswordButton.setText("Save Password");
		f_savePasswordButton.setSelection(false/* TODO boolean field */);
		f_savePasswordButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				// TODO f_savePassword = f_savePasswordButton.getSelection();
			}
		});

		final Composite warning = new Composite(authGroup, SWT.NONE);
		warning.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2,
				1));
		gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		warning.setLayout(gridLayout);
		final Label saveWarningImg = new Label(warning, SWT.NONE);
		saveWarningImg.setImage(SLImages
				.getWorkbenchImage(ISharedImages.IMG_OBJS_WARN_TSK));
		saveWarningImg.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false,
				false));

		final Label saveWarning = new Label(warning, SWT.WRAP);
		data = new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1);
		data.widthHint = 250;
		saveWarning.setLayoutData(data);
		saveWarning
				.setText("Saved secret data is stored on your computer in a format that's difficult, but not impossible, for an intruder to read.");

		return entryPanel;
	}

	private void updateServerURL() {
		f_serverURL.setText(f_server.getProtocol() + "://" + f_server.getHost()
				+ " on port " + f_server.getPort());
	}

	public SierraServerModel getServer() {
		return f_server;
	}
}
