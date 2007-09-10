package com.surelogic.sierra.client.eclipse.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
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
import com.surelogic.sierra.client.eclipse.model.SierraServerManager;
import com.surelogic.sierra.client.eclipse.model.SierraServerModel;

/**
 * Dialog to allow the user to enter or edit the location and authentication
 * information for a Sierra server.
 */
public final class ServerLocationDialog extends TitleAreaDialog {

	public static final String SAVE_PW_WARNING = "Saved secret data is stored on your computer in a format that's difficult, but not impossible, for an intruder to read.";

	private static final String TITLE = "Enter Sierra Server Location Information";
	private static final String INFO_MSG = "Define the location and authentication information for the Sierra server you want to interact with.";
	private static final int INFO_WIDTH_HINT = 70;

	private final SierraServerModel f_server;

	private final boolean f_newLocation;

	private boolean f_isSecure;
	private boolean f_savePassword;
	private boolean f_validateLocation = true;

	private Mediator f_mediator;

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
		f_isSecure = f_server.isSecure();
		f_savePassword = f_server.savePassword();
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
		final Composite contents = (Composite) super.createDialogArea(parent);

		Composite panel = new Composite(contents, SWT.NONE);
		panel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		panel.setLayout(gridLayout);

		final Label label = new Label(panel, SWT.RIGHT);
		label.setText("Label:");
		label.setLayoutData(new GridData(SWT.RIGHT));
		Text labelText = new Text(panel, SWT.SINGLE | SWT.BORDER);
		labelText.setText(f_server.getLabel());
		labelText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		labelText.addListener(SWT.Verify, new Listener() {
			public void handleEvent(Event event) {
				String text = event.text;
				char[] chars = new char[text.length()];
				text.getChars(0, chars.length, chars, 0);
				for (char c : chars) {
					boolean number = '0' <= c && c <= '9';
					boolean alpha = 'A' <= c && c <= 'z';
					boolean spec = c == '?' || c == ' ' || c == '(' || c == ')';
					if (!(number || alpha || spec)) {
						event.doit = false;
						return;
					}
				}
			}
		});

		final Group locGroup = new Group(panel, SWT.NONE);
		locGroup.setText("Location");
		locGroup.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2,
				1));
		gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		locGroup.setLayout(gridLayout);

		final Label hostLabel = new Label(locGroup, SWT.RIGHT);
		hostLabel.setText("Host:");
		GridData data = new GridData(SWT.RIGHT);
		data.widthHint = INFO_WIDTH_HINT;
		hostLabel.setLayoutData(data);
		Text hostText = new Text(locGroup, SWT.SINGLE | SWT.BORDER);
		hostText.setText(f_server.getHost());
		hostText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		final Label portLabel = new Label(locGroup, SWT.RIGHT);
		portLabel.setText("Port:");
		data = new GridData(SWT.RIGHT);
		data.widthHint = INFO_WIDTH_HINT;
		portLabel.setLayoutData(data);
		Text portText = new Text(locGroup, SWT.SINGLE | SWT.BORDER);
		portText.setText(f_server.getPort() + "");
		portText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		portText.addListener(SWT.Verify, new Listener() {
			public void handleEvent(Event event) {
				String text = event.text;
				char[] chars = new char[text.length()];
				text.getChars(0, chars.length, chars, 0);
				for (char c : chars) {
					boolean number = '0' <= c && c <= '9';

					if (!number) {
						event.doit = false;
						return;
					}
				}
			}
		});

		final Label protLabel = new Label(locGroup, SWT.RIGHT);
		protLabel.setText("Protocol:");
		data = new GridData(SWT.RIGHT);
		data.widthHint = INFO_WIDTH_HINT;
		protLabel.setLayoutData(data);
		final Composite radio = new Composite(locGroup, SWT.NONE);
		final FillLayout fill = new FillLayout(SWT.HORIZONTAL);
		radio.setLayout(fill);
		final Button http = new Button(radio, SWT.RADIO);
		http.setText("http");
		final Button https = new Button(radio, SWT.RADIO);
		https.setText("https");
		if (f_isSecure) {
			https.setSelection(true);
		} else {
			http.setSelection(true);
		}
		final Listener protListener = new Listener() {
			public void handleEvent(Event event) {
				f_isSecure = https.getSelection();
			}
		};
		http.addListener(SWT.Selection, protListener);
		https.addListener(SWT.Selection, protListener);

		final Group authGroup = new Group(panel, SWT.NONE);
		authGroup.setText("Authentication");
		authGroup.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2,
				1));
		gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		authGroup.setLayout(gridLayout);

		final Label userLabel = new Label(authGroup, SWT.RIGHT);
		userLabel.setText("User:");
		data = new GridData(SWT.RIGHT);
		data.widthHint = INFO_WIDTH_HINT;
		userLabel.setLayoutData(data);
		Text userText = new Text(authGroup, SWT.SINGLE | SWT.BORDER);
		userText.setText(f_server.getUser());
		userText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		final Label passwordLabel = new Label(authGroup, SWT.RIGHT);
		passwordLabel.setText("Password:");
		data = new GridData(SWT.RIGHT);
		data.widthHint = INFO_WIDTH_HINT;
		passwordLabel.setLayoutData(data);
		Text passwordText = new Text(authGroup, SWT.SINGLE | SWT.BORDER);
		passwordText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		passwordText.setText(f_server.getPassword());
		passwordText.setEchoChar('\u25CF');

		final Button savePasswordButton = new Button(authGroup, SWT.CHECK);
		savePasswordButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER,
				false, false, 2, 1));
		savePasswordButton.setText("Save Password");
		savePasswordButton.setSelection(f_savePassword);
		savePasswordButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				f_savePassword = savePasswordButton.getSelection();
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
		data.widthHint = 100;
		saveWarning.setLayoutData(data);
		saveWarning.setText(SAVE_PW_WARNING);

		final Button validateButton = new Button(panel, SWT.CHECK);
		validateButton.setText("Validate server location on completion");
		validateButton.setSelection(f_validateLocation);
		validateButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				f_validateLocation = validateButton.getSelection();
			}
		});
		validateButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 2, 1));

		setTitle(TITLE);

		f_mediator = new Mediator(labelText, hostText, portText, userText,
				passwordText);
		f_mediator.init();

		Dialog.applyDialogFont(panel);

		return panel;
	}

	@Override
	protected Control createContents(Composite parent) {
		final Control contents = super.createContents(parent);
		parent.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		getShell().pack();
		f_mediator.checkDialogContents();
		return contents;
	}

	@Override
	protected void okPressed() {
		f_mediator.okPressed();
		/*
		 * Because we probably changed something about the server, notify all
		 * observers of server information.
		 */
		f_server.getManager().notifyObservers();
		super.okPressed();
	}

	public SierraServerModel getServer() {
		return f_server;
	}

	private final class Mediator {

		private final SierraServerManager f_manager = f_server.getManager();

		private final Text f_labelText;
		private final Text f_hostText;
		private final Text f_portText;
		private final Text f_userText;
		private final Text f_passwordText;

		Mediator(Text labelText, Text hostText, Text portText, Text userText,
				Text passwordText) {
			f_labelText = labelText;
			f_hostText = hostText;
			f_portText = portText;
			f_userText = userText;
			f_passwordText = passwordText;
		}

		void init() {
			final Listener checkContentsListener = new Listener() {
				public void handleEvent(Event event) {
					checkDialogContents();
				}
			};
			f_labelText.addListener(SWT.Modify, checkContentsListener);
			f_hostText.addListener(SWT.Modify, checkContentsListener);
			f_portText.addListener(SWT.Modify, checkContentsListener);
			f_userText.addListener(SWT.Modify, checkContentsListener);
			f_labelText.addListener(SWT.Modify, checkContentsListener);
		}

		void checkDialogContents() {
			boolean valid = true;
			boolean showInfo = true;

			if (f_hostText.getText().equals("")) {
				valid = false;
			}

			if (f_portText.getText().equals("")) {
				valid = false;
			}

			if (f_userText.getText().equals("")) {
				valid = false;
			}

			final String labelText = f_labelText.getText();
			if (labelText.equals("")) {
				valid = false;
			} else if (!f_server.getLabel().equals(labelText)
					&& f_manager.exists(labelText)) {
				System.out.println("Exists");
				valid = false;
				showInfo = false;
				setMessage("The label '" + labelText
						+ "' is used by another server configuration",
						IMessageProvider.ERROR);
			}

			if (showInfo)
				setMessage(INFO_MSG, IMessageProvider.INFORMATION);
			getButton(IDialogConstants.OK_ID).setEnabled(valid);
		}

		public void okPressed() {
			f_server.setLabel(f_labelText.getText());
			f_server.setHost(f_hostText.getText());
			f_server.setPort(Integer.parseInt(f_portText.getText()));
			f_server.setSecure(f_isSecure);
			f_server.setUser(f_userText.getText());
			f_server.setPassword(f_passwordText.getText());
			f_server.setSavePassword(f_savePassword);
		}
	}
}
