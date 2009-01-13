package com.surelogic.sierra.client.eclipse.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.window.Window;
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

import com.surelogic.common.eclipse.SLImages;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.images.CommonImages;
import com.surelogic.sierra.client.eclipse.actions.SynchronizeBugLinkServerAction;
import com.surelogic.sierra.client.eclipse.model.SierraServer;
import com.surelogic.sierra.client.eclipse.model.SierraServerManager;
import com.surelogic.sierra.client.eclipse.preferences.ServerFailureReport;
import com.surelogic.sierra.tool.message.ServerInfoReply;
import com.surelogic.sierra.tool.message.ServerInfoRequest;
import com.surelogic.sierra.tool.message.ServerInfoService;
import com.surelogic.sierra.tool.message.ServerInfoServiceClient;
import com.surelogic.sierra.tool.message.SierraServerLocation;

/**
 * Dialog to allow the user to enter or edit the location and authentication
 * information for a Sierra server.
 */
public final class ServerLocationDialog extends TitleAreaDialog {
	private static final int RETRY = 2;

	public static final String NEW_TITLE = "New Sierra Team Server Location";
	public static final String EDIT_TITLE = "Edit Sierra Team Server Location";

	private static final int CONTENTS_WIDTH_HINT = 350;

	static final String SAVE_PW_WARNING = "Saved secret data is stored on your computer in a format that's difficult, but not impossible, for an intruder to read.";

	private static final String TITLE = "Enter Sierra Team Server Location Information";
	private static final String INFO_MSG = "Define the information for the Sierra team server you want to interact with.";
	private static final String VALIDATE_MSG = "Please check the information below.  We could not contact the Sierra team server.";
	private static final int INFO_WIDTH_HINT = 70;

	private SierraServerLocation f_server;
	private ServerInfoReply f_serverReply;

	private final String f_title;

	private boolean f_isSecure;
	private boolean f_savePassword;
	private boolean f_autoSync;

	private Mediator f_mediator;

	private boolean f_validateServer = true;
	private boolean f_syncServer = true;
	private boolean f_serverValidated = true;

	/**
	 * Constructs a new dialog to allow the user to enter or edit the location
	 * and authentication information for a Sierra server.
	 * 
	 * @param parentShell
	 *            a shell.
	 * @param server
	 *            the information about the Sierra server.
	 * @param title
	 *            the title to use for this dialog.
	 * @param autoSync
	 */
	private ServerLocationDialog(final Shell parentShell,
			final SierraServerLocation server, final String title,
			final boolean savePassword, final boolean autoSync) {
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX);
		assert server != null;
		f_server = server;
		f_title = title;
		f_isSecure = f_server.isSecure();
		f_savePassword = savePassword;
		f_autoSync = autoSync;
	}

	@Override
	protected void configureShell(final Shell newShell) {
		super.configureShell(newShell);
		newShell.setImage(SLImages.getImage(CommonImages.IMG_SIERRA_SERVER));
		newShell.setText(f_title);
	}

	@Override
	protected Control createDialogArea(final Composite parent) {
		final Composite contents = (Composite) super.createDialogArea(parent);

		final Composite panel = new Composite(contents, SWT.NONE);

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;

		panel.setLayout(gridLayout);
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, false);
		data.widthHint = CONTENTS_WIDTH_HINT;
		panel.setLayoutData(data);

		final Label label = new Label(panel, SWT.RIGHT);
		label.setText("Label:");
		label.setLayoutData(new GridData(SWT.RIGHT));
		final Text labelText = new Text(panel, SWT.SINGLE | SWT.BORDER);
		labelText.setText(f_server.getLabel());
		labelText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		labelText.addListener(SWT.Verify, new Listener() {
			public void handleEvent(final Event event) {
				final String text = event.text;
				final char[] chars = new char[text.length()];
				text.getChars(0, chars.length, chars, 0);
				for (final char c : chars) {
					final boolean number = ('0' <= c) && (c <= '9');
					final boolean alpha = ('A' <= c) && (c <= 'z');
					final boolean spec = (c == '?') || (c == ' ') || (c == '(')
							|| (c == ')') || (c == '.');
					if (!(number || alpha || spec)) {
						event.doit = false;
						return;
					}
				}
			}
		});

		/* Location group */
		final Group locGroup = new Group(panel, SWT.NONE);
		locGroup.setText("Location");
		locGroup.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2,
				1));
		gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		locGroup.setLayout(gridLayout);

		final Label hostLabel = new Label(locGroup, SWT.RIGHT);
		hostLabel.setText("Host:");
		data = new GridData(SWT.RIGHT);
		data.widthHint = INFO_WIDTH_HINT;
		hostLabel.setLayoutData(data);
		final Text hostText = new Text(locGroup, SWT.SINGLE | SWT.BORDER);
		hostText.setText(f_server.getHost());
		hostText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		final Label portLabel = new Label(locGroup, SWT.RIGHT);
		portLabel.setText("Port:");
		data = new GridData(SWT.RIGHT);
		data.widthHint = INFO_WIDTH_HINT;
		portLabel.setLayoutData(data);
		final Text portText = new Text(locGroup, SWT.SINGLE | SWT.BORDER);
		portText.setText(Integer.toString(f_server.getPort()));
		portText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		portText.addListener(SWT.Verify, new Listener() {
			public void handleEvent(final Event event) {
				final String text = event.text;
				final char[] chars = new char[text.length()];
				text.getChars(0, chars.length, chars, 0);
				for (final char c : chars) {
					final boolean number = ('0' <= c) && (c <= '9');

					if (!number) {
						event.doit = false;
						return;
					}
				}
			}
		});

		final Label contextPathLabel = new Label(locGroup, SWT.RIGHT);
		contextPathLabel.setText("Context:");
		data = new GridData(SWT.RIGHT);
		data.widthHint = INFO_WIDTH_HINT;
		contextPathLabel.setLayoutData(data);
		final Text contextPathText = new Text(locGroup, SWT.SINGLE | SWT.BORDER);
		contextPathText.setText(f_server.getContextPath());
		contextPathText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

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
			public void handleEvent(final Event event) {
				f_isSecure = https.getSelection();
			}
		};
		http.addListener(SWT.Selection, protListener);
		https.addListener(SWT.Selection, protListener);

		/* Authorization group */
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
		final Text userText = new Text(authGroup, SWT.SINGLE | SWT.BORDER);
		userText.setText(f_server.getUser());
		userText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		final Label passwordLabel = new Label(authGroup, SWT.RIGHT);
		passwordLabel.setText("Password:");
		data = new GridData(SWT.RIGHT);
		data.widthHint = INFO_WIDTH_HINT;
		passwordLabel.setLayoutData(data);
		final Text passwordText = new Text(authGroup, SWT.SINGLE | SWT.BORDER);
		passwordText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		if (f_server.getPass() == null) {
			passwordText.setText("");
		} else {
			passwordText.setText(f_server.getPass());
		}
		passwordText.setEchoChar('\u25CF');

		final Button savePasswordButton = makeCheckButton(panel,
				"Save Password", f_savePassword);
		final Button validateButton = makeCheckButton(panel,
				"Validate connection on finish", f_validateServer);
		final Button syncButton = makeCheckButton(panel,
				"Synchronize BugLink data on finish", f_syncServer);
		syncButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(final Event event) {
				f_syncServer = syncButton.getSelection();
			}
		});

		final Button autoSyncButton = makeCheckButton(panel,
				"Enable auto-sync", f_autoSync);
		autoSyncButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(final Event event) {
				f_autoSync = autoSyncButton.getSelection();
			}
		});

		savePasswordButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(final Event event) {
				f_savePassword = savePasswordButton.getSelection();
			}
		});

		final Composite warning = new Composite(panel, SWT.NONE);
		warning.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false,
				2, 1));
		gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		warning.setLayout(gridLayout);
		final Label saveWarningImg = new Label(warning, SWT.NONE);
		saveWarningImg.setImage(SLImages.getImage(CommonImages.IMG_WARNING));
		saveWarningImg.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false,
				false));

		final Label saveWarning = new Label(warning, SWT.WRAP);
		data = new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1);
		// data.widthHint = 100;
		saveWarning.setLayoutData(data);
		saveWarning.setText(SAVE_PW_WARNING);

		setTitle(TITLE);

		f_mediator = new Mediator(labelText, hostText, portText,
				contextPathText, userText, passwordText, validateButton);
		f_mediator.init();

		Dialog.applyDialogFont(panel);

		return contents;
	}

	private static Button makeCheckButton(final Composite panel,
			final String msg, final boolean defaultVal) {
		final Button button = new Button(panel, SWT.CHECK);
		button.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false,
				2, 1));
		button.setText(msg);
		button.setSelection(defaultVal);
		return button;
	}

	@Override
	protected Control createContents(final Composite parent) {
		final Control contents = super.createContents(parent);
		parent.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1,
				1));
		getShell().pack();
		f_mediator.checkDialogContents();
		return contents;
	}

	@Override
	protected void okPressed() {
		f_mediator.okPressed();
		super.okPressed();
	}

	/*
	 * public SierraServer getServer() { return f_server; }
	 */

	private final class Mediator {
		private final SierraServerManager f_manager = SierraServerManager
				.getInstance();

		private final Text f_labelText;
		private final Text f_hostText;
		private final Text f_portText;
		private final Text f_contextPathText;
		private final Text f_userText;
		private final Text f_passwordText;
		private final Button f_validateButton;

		Mediator(final Text labelText, final Text hostText,
				final Text portText, final Text contextPathText,
				final Text userText, final Text passwordText,
				final Button validateButton) {
			f_labelText = labelText;
			f_hostText = hostText;
			f_portText = portText;
			f_contextPathText = contextPathText;
			f_userText = userText;
			f_passwordText = passwordText;
			f_validateButton = validateButton;
		}

		void init() {
			final Listener checkContentsListener = new Listener() {
				public void handleEvent(final Event event) {
					checkDialogContents();
				}
			};
			f_labelText.addListener(SWT.Modify, checkContentsListener);
			f_hostText.addListener(SWT.Modify, checkContentsListener);
			f_portText.addListener(SWT.Modify, checkContentsListener);
			f_contextPathText.addListener(SWT.Modify, checkContentsListener);
			f_userText.addListener(SWT.Modify, checkContentsListener);
			f_labelText.addListener(SWT.Modify, checkContentsListener);
			f_validateButton.addListener(SWT.Selection, checkContentsListener);
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

			final String cp = f_contextPathText.getText().trim();
			final boolean slashStartEnd = cp.startsWith("/")
					&& cp.endsWith("/");
			final boolean noSpaces = cp.indexOf(' ') == -1;
			final boolean validCP = slashStartEnd && noSpaces;
			if (!validCP) {
				valid = false;
				showInfo = false;
				setMessage(I18N
						.msg("sierra.eclipse.badServerLocationContextPath"),
						IMessageProvider.ERROR);
			}

			final String labelText = f_labelText.getText().trim();
			if ("".equals(labelText)) {
				valid = false;
			} else if (!f_server.getLabel().equals(labelText)
					&& f_manager.exists(labelText)) {
				valid = false;
				showInfo = false;
				setMessage(I18N.msg("sierra.eclipse.badServerLocationLabel",
						labelText), IMessageProvider.ERROR);
			}

			if (showInfo) {
				setMessage(INFO_MSG, IMessageProvider.INFORMATION);
			}
			if (!f_serverValidated) {
				setMessage(VALIDATE_MSG, IMessageProvider.WARNING);
			}

			getButton(IDialogConstants.OK_ID).setEnabled(valid);
		}

		public void okPressed() {
			f_server = new SierraServerLocation(f_labelText.getText().trim(),
					f_hostText.getText().trim(), f_isSecure, Integer
							.parseInt(f_portText.getText().trim()),
					f_contextPathText.getText().trim(), f_userText.getText()
							.trim(), f_passwordText.getText(), f_autoSync);
			// f_server.setSavePassword(f_savePassword);
			f_validateServer = f_validateButton.getSelection();
		}
	}

	@Override
	public int open() {
		final int rv = super.open();
		if (rv == Window.OK) {
			return validateServer();
		}
		return rv;
	}

	private int validateServer() {
		f_serverReply = null;

		if (!f_validateServer) {
			f_serverValidated = true;
			return Window.OK;
		}

		final ServerInfoService ss = ServerInfoServiceClient.create(f_server);
		String uid = null;
		try {
			f_serverReply = ss.getServerInfo(new ServerInfoRequest());
			uid = f_serverReply.getUid();
			if (uid == null) {
				f_serverValidated = false;
				return RETRY;
			} else {
				f_serverValidated = true;
				return Window.OK;
			}
		} catch (final Exception e) {
			f_serverValidated = false;
			return RETRY;
		}
	}

	// FIX change back to using SierraServer directly,
	// since we manually notify observers
	public static void newServer(final Shell shell) {
		final SierraServerManager manager = SierraServerManager.getInstance();
		final ServerLocationDialog dialog = editServer(shell, manager
				.createLocation(), ServerLocationDialog.NEW_TITLE, true, false);
		if (dialog != null) {
			final SierraServer newServer = manager.create();
			updateServer(dialog, newServer);
		}
	}

	public static void editServer(final Shell shell, final SierraServer server) {
		final ServerLocationDialog dialog = editServer(shell, server
				.getServer(), ServerLocationDialog.EDIT_TITLE, server
				.savePassword(), server.autoSync());
		updateServer(dialog, server);
	}

	private static ServerLocationDialog editServer(final Shell shell,
			final SierraServerLocation loc, final String title,
			final boolean savePassword, final boolean autoSync) {
		final ServerLocationDialog dialog = new ServerLocationDialog(shell,
				loc, title, savePassword, autoSync);
		int rv = RETRY;
		while (rv == RETRY) {
			rv = dialog.open();
		}
		return rv == Window.OK ? dialog : null;
	}

	private static void updateServer(final ServerLocationDialog dialog,
			final SierraServer server) {
		if (dialog != null) {
			boolean changed = server.setServer(dialog.f_server,
					dialog.f_serverReply);
			changed = changed
					|| (server.savePassword() != dialog.f_savePassword);

			server.setSavePassword(dialog.f_savePassword);

			changed = changed || (server.autoSync() != dialog.f_autoSync);
			server.setAutoSync(dialog.f_autoSync);

			if (changed) {
				/*
				 * Because we probably changed something about the server,
				 * notify all observers of server information.
				 */
				server.getManager().notifyObservers();
			}
			if (dialog.f_syncServer) {
				final SynchronizeBugLinkServerAction sync = new SynchronizeBugLinkServerAction(
						ServerFailureReport.SHOW_DIALOG, true);
				sync.run(server);
			}
		}
	}
}
