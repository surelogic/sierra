package com.surelogic.sierra.gwt.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTMLTable;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.KeyboardListenerAdapter;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.surelogic.sierra.gwt.client.data.LoginResult;
import com.surelogic.sierra.gwt.client.service.ServiceHelper;
import com.surelogic.sierra.gwt.client.service.SessionServiceAsync;
import com.surelogic.sierra.gwt.client.util.ExceptionTracker;
import com.surelogic.sierra.gwt.client.util.ImageHelper;

public class LoginContent extends ContentComposite {
	private static final LoginContent instance = new LoginContent();

	private final VerticalPanel loginPanel = new VerticalPanel();
	private final Label message = new Label();
	private final Label errorMessage = new Label();
	private final Grid userPassGrid = new Grid(3, 2);

	private final TextBox username = new TextBox();
	private final Label usernameLabel = new Label("User");

	private final PasswordTextBox password = new PasswordTextBox();
	private final Label passwordLabel = new Label("Password");

	private final Image waitImage = ImageHelper.getWaitImage(24);
	private final HorizontalPanel actionPanel = new HorizontalPanel();
	private final SimplePanel waitPanel = new SimplePanel();
	private final Button login = new Button("Log In");

	public static final LoginContent getInstance() {
		return instance;
	}

	public static final LoginContent getInstance(String errorText) {
		instance.errorMessage.setText(errorText);
		return instance;
	}

	private LoginContent() {
		super();
	}

	public String getContextName() {
		return "Login";
	}

	public void login() {
		login.setEnabled(false);
		waitPanel.add(waitImage);

		final String usernameText = username.getText().trim();
		final String passwordText = password.getText().trim();

		SessionServiceAsync sessionService = ServiceHelper.getSessionService();
		sessionService.login(usernameText, passwordText, new AsyncCallback() {

			public void onFailure(Throwable caught) {
				ExceptionTracker.logException(caught);

				resetLoginAttempt();
				errorMessage
						.setText("Authentication service unavailable. (Server may be down)");
			}

			public void onSuccess(Object result) {
				resetLoginAttempt();
				LoginResult lr = (LoginResult) result;
				if (lr.getErrorMessage() != null) {
					errorMessage.setText(lr.getErrorMessage());
				} else if (lr.getUserAccount() == null) {
					errorMessage.setText("No user account available");
				} else {
					errorMessage.setText("");
					ClientContext.setUser(lr.getUserAccount());
				}
			}
		});
	}

	protected void onInitialize(DockPanel rootPanel) {
		// TODO redo style bindings to standard naming scheme
		loginPanel.addStyleName("login-panel");
		loginPanel.setHorizontalAlignment(VerticalPanel.ALIGN_CENTER);
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
		actionPanel.setHorizontalAlignment(HorizontalPanel.ALIGN_RIGHT);
		final HorizontalPanel grouper = new HorizontalPanel();
		grouper.add(waitPanel);
		grouper.add(login);
		actionPanel.add(grouper);
		login.setEnabled(false);
		login.addStyleName("login-button");
		userPassGrid.setWidget(2, 1, actionPanel);

		final HTMLTable.CellFormatter cellFormatter = userPassGrid
				.getCellFormatter();
		cellFormatter.setWidth(2, 1, "1px");
		cellFormatter.setHorizontalAlignment(2, 1, HasAlignment.ALIGN_RIGHT);

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

		rootPanel.addStyleName("login-root");
		rootPanel.setHorizontalAlignment(DockPanel.ALIGN_CENTER);
		rootPanel.setVerticalAlignment(DockPanel.ALIGN_MIDDLE);
		rootPanel.add(loginPanel, DockPanel.CENTER);
	}

	protected void onActivate() {
		resetLoginAttempt();
		if (username.getText().trim().length() > 0) {
			password.setFocus(true);
		} else {
			username.setFocus(true);
		}
	}

	protected boolean onDeactivate() {
		return true;
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