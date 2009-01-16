package com.surelogic.sierra.client.eclipse.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.window.Window;
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

import com.surelogic.common.eclipse.SLImages;
import com.surelogic.common.eclipse.SWTUtility;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.images.CommonImages;
import com.surelogic.sierra.tool.message.ServerLocation;

/**
 * Dialog to prompt the user for a user name and password to a Sierra server.
 */
public final class ServerAuthenticationDialog extends Dialog {

	/**
	 * Prompts the user for authentication based upon the passed location and
	 * returns the new location object.
	 * <p>
	 * Clients must take appropriate action with the new location object.
	 * 
	 * @param parentShell
	 *            a shell.
	 * @param location
	 *            the server location object to prompt for authentication
	 *            information.
	 * @return a new server location object that reflects the changes made by
	 *         the user. If the returned object is the same object that was
	 *         passed via <tt>location</tt> then the user canceled the dialog.
	 */
	public static ServerLocation open(Shell parentShell, ServerLocation location) {
		if (location == null)
			throw new IllegalArgumentException(I18N.err(44, "location"));
		if (parentShell == null)
			parentShell = SWTUtility.getShell();

		final ServerAuthenticationDialog dialog = new ServerAuthenticationDialog(
				parentShell, location);

		if (dialog.open() == Window.OK) {
			return dialog.getMutatedLocation();
		} else {
			return location;
		}
	}

	/**
	 * This field is mutated when OK is pressed.
	 */
	private ServerLocation f_location;

	private Text f_userText;
	private Text f_passwordText;
	private boolean f_savePassword = false;
	private Button f_savePasswordButton;

	/**
	 * Gets the changes made to the location by this dialog. Changes are only
	 * made to the location if the user pressed OK.
	 * 
	 * @return the changes made to the location by this dialog.
	 */
	private ServerLocation getMutatedLocation() {
		return f_location;
	}

	/**
	 * Constructs a new dialog to prompt the user for a user name and password
	 * for a particular Sierra server.
	 * 
	 * @param parentShell
	 *            a shell.
	 * @param location
	 *            the location information about a Sierra server.
	 */
	public ServerAuthenticationDialog(Shell parentShell, ServerLocation location) {
		super(parentShell);
		assert location != null;
		f_location = location;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setImage(SLImages.getImage(CommonImages.IMG_SIERRA_LOGO));
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
				.getImage(CommonImages.IMG_SIERRA_POWERED_BY_SURELOGIC));

		final Composite entryPanel = new Composite(panel, SWT.NONE);
		gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		entryPanel.setLayout(gridLayout);

		final Label directions = new Label(entryPanel, SWT.WRAP);
		GridData data = new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1);
		data.heightHint = 20;
		directions.setLayoutData(data);
		directions.setText("Enter your authentication for");

		final Label serverImg = new Label(entryPanel, SWT.NONE);
		serverImg.setImage(SLImages.getImage(CommonImages.IMG_SIERRA_SERVER));
		data = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
		data.heightHint = 25;
		serverImg.setLayoutData(data);
		final Label serverlabel = new Label(entryPanel, SWT.NONE);
		serverlabel.setText(f_location.createHomeURL().toString());
		serverlabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false,
				true));

		final Label userLabel = new Label(entryPanel, SWT.NONE);
		userLabel.setText("User:");
		userLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
				false));
		f_userText = new Text(entryPanel, SWT.SINGLE | SWT.BORDER);
		f_userText
				.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, true));
		f_userText.setText(f_location.getUserOrEmptyString());

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
		saveWarningImg.setImage(SLImages.getImage(CommonImages.IMG_WARNING));
		saveWarningImg.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false,
				false));
		final Label saveWarning = new Label(warning, SWT.WRAP);
		data = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		data.widthHint = 300;
		saveWarning.setLayoutData(data);
		saveWarning.setText(I18N.msg("sierra.dialog.savePasswordWarning"));

		return panel;
	}

	@Override
	protected void okPressed() {
		f_location = f_location.changeAuthorization(f_userText.getText(),
				f_passwordText.getText(), f_savePassword);
		super.okPressed();
	}
}
