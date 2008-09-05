package com.surelogic.sierra.gwt.client.content.login;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTMLTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.KeyboardListenerAdapter;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.surelogic.sierra.gwt.client.Context;
import com.surelogic.sierra.gwt.client.ContextManager;
import com.surelogic.sierra.gwt.client.SessionManager;
import com.surelogic.sierra.gwt.client.UserListener;
import com.surelogic.sierra.gwt.client.content.ContentComposite;
import com.surelogic.sierra.gwt.client.content.overview.OverviewContent;
import com.surelogic.sierra.gwt.client.data.UserAccount;
import com.surelogic.sierra.gwt.client.ui.ImageHelper;

public final class LoginContent extends ContentComposite {
	private static final LoginContent instance = new LoginContent();

	private final VerticalPanel loginPanel = new VerticalPanel();
	private final Label message = new Label();
	private final Label errorMessage = new Label();
	private final Grid userPassGrid = new Grid(3, 2);

	private final TextBox username = new TextBox();
	private final Label usernameLabel = new Label("User");

	private final PasswordTextBox password = new PasswordTextBox();
	private final Label passwordLabel = new Label("Password");

	private final Image waitImage = ImageHelper.getWaitImage(16);
	private final HorizontalPanel actionPanel = new HorizontalPanel();
	private final SimplePanel waitPanel = new SimplePanel();
	private final Button login = new Button("Log In");

	public static final LoginContent getInstance() {
		return instance;
	}

	private LoginContent() {
		// singleton
	}

	public void login() {
		login.setEnabled(false);
		waitPanel.add(waitImage);

		final String usernameText = username.getText().trim();
		final String passwordText = password.getText().trim();

		SessionManager.addUserListener(new UserListener() {

			public void onLogin(final UserAccount user) {
				resetLoginAttempt();
				errorMessage.setText("");
				if (ContextManager.isContent(LoginContent.getInstance())) {
					OverviewContent.getInstance().show();
				}
			}

			public void onLoginFailure(final String message) {
				resetLoginAttempt();
				errorMessage.setText(message);
			}

			public void onUpdate(final UserAccount user) {
				// do nothing
			}

			public void onLogout(final UserAccount user,
					final String errorMessageText) {
				resetLoginAttempt();
				errorMessage.setText(errorMessageText);
			}
		});

		SessionManager.login(usernameText, passwordText);
	}

	@Override
	protected void onInitialize(final DockPanel rootPanel) {
		// TODO redo style bindings to standard naming scheme
		loginPanel.addStyleName("login-panel");
		loginPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		loginPanel.add(message);
		message.addStyleName("login-message");
		loginPanel.add(errorMessage);
		errorMessage.addStyleName("login-errormessage");
		loginPanel.add(userPassGrid);

		userPassGrid.setWidget(0, 0, usernameLabel);
		usernameLabel.addStyleName("login-label");
		userPassGrid.setWidget(0, 1, username);
		username.addStyleName("login-textbox");

		userPassGrid.setWidget(1, 0, passwordLabel);
		passwordLabel.addStyleName("login-label");
		userPassGrid.setWidget(1, 1, password);
		password.addStyleName("login-textbox");

		actionPanel.setWidth("100%");
		actionPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
		final HorizontalPanel grouper = new HorizontalPanel();
		grouper.add(waitPanel);
		grouper.setCellVerticalAlignment(waitPanel,
				HasVerticalAlignment.ALIGN_MIDDLE);
		grouper.add(login);
		actionPanel.add(grouper);
		login.setEnabled(false);
		login.addStyleName("login-button");
		userPassGrid.setWidget(2, 1, actionPanel);

		final HTMLTable.CellFormatter cellFormatter = userPassGrid
				.getCellFormatter();
		cellFormatter.setWidth(2, 1, "1px");
		cellFormatter.setHorizontalAlignment(2, 1,
				HasHorizontalAlignment.ALIGN_RIGHT);

		usernameLabel.addClickListener(new ClickListener() {
			public void onClick(final Widget sender) {
				username.setFocus(true);
			}
		});

		passwordLabel.addClickListener(new ClickListener() {
			public void onClick(final Widget sender) {
				password.setFocus(true);
			}
		});

		final KeyboardListenerAdapter keyboardListener = new KeyboardListenerAdapter() {
			@Override
			public void onKeyUp(final Widget sender, final char keyCode,
					final int modifiers) {
				updateLoginEnabled();
				if (keyCode == KEY_ENTER) {
					if (login.isEnabled()) {
						login.click();
					}
				}
			}
		};
		username.addKeyboardListener(keyboardListener);
		password.addKeyboardListener(keyboardListener);

		final ChangeListener loginChangeListener = new ChangeListener() {
			public void onChange(final Widget sender) {
				updateLoginEnabled();
			}
		};
		username.addChangeListener(loginChangeListener);
		password.addChangeListener(loginChangeListener);

		login.addClickListener(new ClickListener() {
			public void onClick(final Widget sender) {
				login();
			}
		});

		rootPanel.add(loginPanel, DockPanel.CENTER);
	}

	@Override
	protected void onUpdate(final Context context) {
		// TODO accept an error message param in the context?
		resetLoginAttempt();
		if (username.getText().trim().length() > 0) {
			password.setFocus(true);
		} else {
			username.setFocus(true);
		}
	}

	@Override
	protected void onDeactivate() {
		// nothing to do
	}

	private void resetLoginAttempt() {
		password.setText("");
		waitPanel.clear();
		updateLoginEnabled();
	}

	private void updateLoginEnabled() {
		login.setEnabled(!username.getText().trim().equals(""));
	}

}