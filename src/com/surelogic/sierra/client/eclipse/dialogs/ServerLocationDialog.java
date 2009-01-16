package com.surelogic.sierra.client.eclipse.dialogs;

import org.eclipse.core.runtime.jobs.Job;
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
import com.surelogic.common.eclipse.SWTUtility;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.images.CommonImages;
import com.surelogic.sierra.client.eclipse.jobs.ValidateServerLocationJob;
import com.surelogic.sierra.client.eclipse.model.ConnectedServerManager;
import com.surelogic.sierra.jdbc.settings.ConnectedServer;
import com.surelogic.sierra.tool.message.ServerLocation;

/**
 * Dialog to allow the user to enter or edit the location and authentication
 * information for a Sierra server.
 */
public final class ServerLocationDialog extends TitleAreaDialog {

	/**
	 * Method to create a new team server location and open a dialog to allow
	 * the user to edit its connection information.
	 * 
	 * @param shell
	 *            the shell to use, may be {@code null} in which case
	 *            {@link SWTUtility#getShell()} is used.
	 */
	public static void newServer(Shell shell) {
		if (shell == null)
			shell = SWTUtility.getShell();

		final String title = I18N.msg("sierra.dialog.serverlocation.newTitle");
		final ServerLocationDialog dialog = new ServerLocationDialog(shell,
				new ServerLocation(), title, true, false);
		if (dialog.open() == Window.OK) {
			final ServerLocation location = dialog.f_location;
			final Job job = new ValidateServerLocationJob(location,
					dialog.f_savePassword, dialog.f_autoSync);
			job.schedule();
		}
	}

	/**
	 * Method to open a dialog to allow the user to edit the passed team server
	 * connection information.
	 * 
	 * @param shell
	 *            the shell to use, may be {@code null} in which case
	 *            {@link SWTUtility#getShell()} is used.
	 * @param server
	 *            the non-null team server location to edit.
	 */
	public static void editServer(final Shell shell,
			final ConnectedServer server) {
		final String title = I18N.msg("sierra.dialog.serverlocation.editTitle");
		final ServerLocationDialog dialog = new ServerLocationDialog(shell,
				server.getLocation(), title);
		if (dialog.open() == Window.OK) {
			final ServerLocation location = dialog.f_location;
			final Job job = new ValidateServerLocationJob(location,
					dialog.f_savePassword, dialog.f_autoSync);
			job.schedule();
		}
	}

	private static final int CONTENTS_WIDTH_HINT = 350;
	private static final int INFO_WIDTH_HINT = 70;

	private ServerLocation f_location;
	private final String f_title;
	private boolean f_isSecure;
	private boolean f_savePassword;
	private boolean f_autoSync;
	private boolean f_validateServer = true;

	private Mediator f_mediator;

	/**
	 * Constructs a new dialog to allow the user to enter or edit the location
	 * and authentication information for a Sierra server.
	 * 
	 * @param parentShell
	 *            a shell.
	 * @param location
	 *            the location information about a Sierra server.
	 * @param title
	 *            the title to use for this dialog.
	 * @param autoSync
	 */
	private ServerLocationDialog(final Shell parentShell,
			final ServerLocation location, final String title,
			final boolean savePassword, final boolean autoSync) {
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX);
		assert location != null;
		f_location = location;
		f_title = title;
		f_isSecure = f_location.isSecure();
		f_savePassword = savePassword;
		f_autoSync = autoSync;
	}

	private ServerLocationDialog(final Shell parentShell,
			final ServerLocation server, final String title) {
		this(parentShell, server, title, server.isSavePassword(), server
				.isAutoSync());
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

		/* Location group */
		final Group locGroup = new Group(panel, SWT.NONE);
		locGroup.setText(I18N.msg("sierra.dialog.serverlocation.location"));
		locGroup.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2,
				1));
		gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		locGroup.setLayout(gridLayout);

		final Label hostLabel = new Label(locGroup, SWT.RIGHT);
		hostLabel.setText(I18N.msg("sierra.dialog.serverlocation.host"));
		data = new GridData(SWT.RIGHT);
		data.widthHint = INFO_WIDTH_HINT;
		hostLabel.setLayoutData(data);
		final Text hostText = new Text(locGroup, SWT.SINGLE | SWT.BORDER);
		hostText.setText(f_location.getHost());
		hostText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		final Label portLabel = new Label(locGroup, SWT.RIGHT);
		portLabel.setText(I18N.msg("sierra.dialog.serverlocation.port"));
		data = new GridData(SWT.RIGHT);
		data.widthHint = INFO_WIDTH_HINT;
		portLabel.setLayoutData(data);
		final Text portText = new Text(locGroup, SWT.SINGLE | SWT.BORDER);
		portText.setText(Integer.toString(f_location.getPort()));
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
		contextPathLabel.setText(I18N
				.msg("sierra.dialog.serverlocation.context"));
		data = new GridData(SWT.RIGHT);
		data.widthHint = INFO_WIDTH_HINT;
		contextPathLabel.setLayoutData(data);
		final Text contextPathText = new Text(locGroup, SWT.SINGLE | SWT.BORDER);
		contextPathText.setText(f_location.getContextPath());
		contextPathText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		final Label protLabel = new Label(locGroup, SWT.RIGHT);
		protLabel.setText(I18N.msg("sierra.dialog.serverlocation.protocol"));
		data = new GridData(SWT.RIGHT);
		data.widthHint = INFO_WIDTH_HINT;
		protLabel.setLayoutData(data);
		final Composite radio = new Composite(locGroup, SWT.NONE);
		final FillLayout fill = new FillLayout(SWT.HORIZONTAL);
		radio.setLayout(fill);
		final Button http = new Button(radio, SWT.RADIO);
		http.setText(I18N.msg("sierra.dialog.serverlocation.protocol.http"));
		final Button https = new Button(radio, SWT.RADIO);
		https.setText(I18N.msg("sierra.dialog.serverlocation.protocol.https"));
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
		authGroup.setText(I18N
				.msg("sierra.dialog.serverlocation.authentication"));
		authGroup.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2,
				1));
		gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		authGroup.setLayout(gridLayout);

		final Label userLabel = new Label(authGroup, SWT.RIGHT);
		userLabel.setText(I18N
				.msg("sierra.dialog.serverlocation.authentication.user"));
		data = new GridData(SWT.RIGHT);
		data.widthHint = INFO_WIDTH_HINT;
		userLabel.setLayoutData(data);
		final Text userText = new Text(authGroup, SWT.SINGLE | SWT.BORDER);
		userText.setText(f_location.getUser());
		userText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		final Label passwordLabel = new Label(authGroup, SWT.RIGHT);
		passwordLabel.setText(I18N
				.msg("sierra.dialog.serverlocation.authentication.password"));
		data = new GridData(SWT.RIGHT);
		data.widthHint = INFO_WIDTH_HINT;
		passwordLabel.setLayoutData(data);
		final Text passwordText = new Text(authGroup, SWT.SINGLE | SWT.BORDER);
		passwordText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		if (f_location.getPass() == null) {
			passwordText.setText("");
		} else {
			passwordText.setText(f_location.getPass());
		}
		passwordText.setEchoChar('\u25CF');

		final Button savePasswordButton = makeCheckButton(panel, I18N
				.msg("sierra.dialog.serverlocation.savePassword"),
				f_savePassword);
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
		saveWarning.setLayoutData(data);
		saveWarning.setText(I18N.msg("sierra.dialog.savePasswordWarning"));

		final Button validateButton = makeCheckButton(panel, I18N
				.msg("sierra.dialog.serverlocation.validateOnFinish"),
				f_validateServer);
		validateButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(final Event event) {
				f_validateServer = validateButton.getSelection();
			}
		});

		final Button autoSyncButton = makeCheckButton(panel, I18N
				.msg("sierra.dialog.serverlocation.enableAutoSync"), f_autoSync);
		autoSyncButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(final Event event) {
				f_autoSync = autoSyncButton.getSelection();
			}
		});

		setTitle(I18N.msg("sierra.dialog.serverlocation.title"));

		f_mediator = new Mediator(hostText, portText, contextPathText,
				userText, passwordText);
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
		if (f_mediator != null)
			f_mediator.okPressed();
		super.okPressed();
	}

	private final class Mediator {

		private final ConnectedServerManager f_manager = ConnectedServerManager
				.getInstance();

		private final Text f_hostText;
		private final Text f_portText;
		private final Text f_contextPathText;
		private final Text f_userText;
		private final Text f_passwordText;

		Mediator(final Text hostText, final Text portText,
				final Text contextPathText, final Text userText,
				final Text passwordText) {
			f_hostText = hostText;
			f_portText = portText;
			f_contextPathText = contextPathText;
			f_userText = userText;
			f_passwordText = passwordText;
		}

		void init() {
			final Listener checkContentsListener = new Listener() {
				public void handleEvent(final Event event) {
					checkDialogContents();
				}
			};
			f_hostText.addListener(SWT.Modify, checkContentsListener);
			f_portText.addListener(SWT.Modify, checkContentsListener);
			f_contextPathText.addListener(SWT.Modify, checkContentsListener);
			f_userText.addListener(SWT.Modify, checkContentsListener);
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

			if (showInfo) {
				setMessage(I18N.msg("sierra.dialog.serverlocation.msg.info"),
						IMessageProvider.INFORMATION);
			}

			getButton(IDialogConstants.OK_ID).setEnabled(valid);
		}

		public void okPressed() {
			f_location = new ServerLocation(f_hostText.getText().trim(),
					f_isSecure, Integer.parseInt(f_portText.getText().trim()),
					f_contextPathText.getText().trim(), f_userText.getText()
							.trim(), f_passwordText.getText(), f_savePassword,
					f_autoSync);
		}
	}
}
