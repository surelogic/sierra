package com.surelogic.sierra.gwt.client;

import com.google.gwt.core.client.GWT;
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

public class LoginPanel extends ContentComposite {
	private final VerticalPanel loginPanel = new VerticalPanel();
	private final Label message = new Label();
	private final Label errorMessage = new Label();
	private final Grid userPassGrid = new Grid(3, 2);

	private final TextBox username = new TextBox();
	private final Label usernameLabel = new Label("User");

	private final PasswordTextBox password = new PasswordTextBox();
	private final Label passwordLabel = new Label("Password");

	private final Image wait = new Image(GWT.getModuleBaseURL()
			+ "images/wait-24x24.gif");
	private final HorizontalPanel actionPanel = new HorizontalPanel();
	private final SimplePanel waitPanel = new SimplePanel();
	private final Button login = new Button("Log In");

	public LoginPanel() {
		super();
		final DockPanel rootPanel = getRootPanel();
		rootPanel.setHorizontalAlignment(DockPanel.ALIGN_CENTER);
		rootPanel.setVerticalAlignment(DockPanel.ALIGN_MIDDLE);

		loginPanel.setHorizontalAlignment(VerticalPanel.ALIGN_CENTER);
		loginPanel.add(message);
		loginPanel.add(errorMessage);
		loginPanel.add(userPassGrid);
		rootPanel.add(loginPanel, DockPanel.CENTER);

		userPassGrid.setWidget(0, 0, usernameLabel);
		userPassGrid.setWidget(0, 1, username);

		userPassGrid.setWidget(1, 0, passwordLabel);
		userPassGrid.setWidget(1, 1, password);

		actionPanel.setWidth("100%");
		actionPanel.setHorizontalAlignment(HorizontalPanel.ALIGN_RIGHT);
		final HorizontalPanel grouper = new HorizontalPanel();
		grouper.add(waitPanel);
		grouper.add(login);
		actionPanel.add(grouper);
		login.setEnabled(false);
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

		final LoginKeyboardListener keyboardListener = new LoginKeyboardListener();
		username.addKeyboardListener(keyboardListener);
		password.addKeyboardListener(keyboardListener);

		final LoginChangeListener loginChangeListener = new LoginChangeListener();
		username.addChangeListener(loginChangeListener);
		password.addChangeListener(loginChangeListener);

		login.addClickListener(new ClickListener() {
			public void onClick(final Widget sender) {
				login();
			}
		});

		rootPanel.addStyleName("login-root");
		loginPanel.addStyleName("login-panel");
		message.addStyleName("login-message");
		errorMessage.addStyleName("login-errormessage");
		username.addStyleName("login-textbox");
		password.addStyleName("login-textbox");
		usernameLabel.addStyleName("login-label");
		passwordLabel.addStyleName("login-label");
		login.addStyleName("login-button");
	}

	public void login() {
		login.setEnabled(false);
		waitPanel.add(wait);

		final String usernameText = username.getText().trim();
		final String passwordText = password.getText().trim();

		SessionServiceAsync sessionService = ServiceHelper.getSessionService();
		sessionService.login(usernameText, passwordText, new AsyncCallback() {

			public void onFailure(Throwable caught) {
				ExceptionTracker.logException(caught);

				reset();
				String errorMsg = "Authentication service unavailable";
				final String caughtMsg = caught.getMessage();
				if (caughtMsg != null && !"".equals(caughtMsg)) {
					errorMsg += ": " + caughtMsg;
				}
				errorMessage.setText(errorMsg);
			}

			public void onSuccess(Object result) {
				reset();
				LoginResult lr = (LoginResult) result;
				if (lr.getErrorMessage() != null) {
					errorMessage.setText(lr.getErrorMessage());
				} else if (lr.getUserAccount() == null) {
					errorMessage.setText("No user account available");
				} else {
					ClientContext.setUser(lr.getUserAccount());
					ContentPanel.getInstance().showDefault();
				}
			}
		});
	}

	public void reset() {
		password.setText("");
		errorMessage.setText("");
		waitPanel.clear();
		updateLoginEnabled();
	}

	public void activate() {
		reset();
		if (username.getText().trim().length() > 0) {
			password.setFocus(true);
		} else {
			username.setFocus(true);
		}
	}

	private void updateLoginEnabled() {
		login.setEnabled(!username.getText().trim().equals(""));
	}

	private class LoginKeyboardListener extends KeyboardListenerAdapter {
		public void onKeyUp(final Widget sender, final char keyCode,
				final int modifiers) {
			updateLoginEnabled();
			if (keyCode == KEY_ENTER) {
				if (login.isEnabled()) {
					login.click();
				}
			}
		}
	}

	private class LoginChangeListener implements ChangeListener {
		public void onChange(final Widget sender) {
			updateLoginEnabled();
		}
	}
}