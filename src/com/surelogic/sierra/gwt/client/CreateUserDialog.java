package com.surelogic.sierra.gwt.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.surelogic.sierra.gwt.client.data.UserAccount;
import com.surelogic.sierra.gwt.client.service.ServiceHelper;
import com.surelogic.sierra.gwt.client.util.ExceptionTracker;

public class CreateUserDialog extends DialogBox {
	private final VerticalPanel rootPanel = new VerticalPanel();
	private final FlexTable userGrid = new FlexTable();
	private final Label errorMessage = new Label("", false);
	private final TextBox userName = new TextBox();
	private final PasswordTextBox password = new PasswordTextBox();
	private final PasswordTextBox passwordAgain = new PasswordTextBox();
	private final CheckBox isAdmin = new CheckBox();
	private boolean successful;

	public CreateUserDialog() {
		super();
		setText("Create a user");

		errorMessage.addStyleName("error");

		userGrid.setText(0, 0, "User Name: ");
		userGrid.setWidget(0, 1, userName);
		userGrid.setText(1, 0, "Password:");
		userGrid.setWidget(1, 1, password);
		userGrid.setText(2, 0, "Confirm Password:");
		userGrid.setWidget(2, 1, passwordAgain);
		userGrid.setText(3, 0, "Administrator:");
		userGrid.setWidget(3, 1, isAdmin);
		rootPanel.add(userGrid);

		final HorizontalPanel buttonPanel = new HorizontalPanel();
		final Button ok = new Button("Ok");
		ok.addClickListener(new ClickListener() {

			public void onClick(Widget sender) {
				createUser();
			}
		});
		buttonPanel.add(ok);
		final Button cancel = new Button("Cancel");
		cancel.addClickListener(new ClickListener() {

			public void onClick(Widget sender) {
				hide();
			}
		});
		buttonPanel.add(cancel);
		rootPanel.add(buttonPanel);
		rootPanel.setCellHorizontalAlignment(buttonPanel,
				VerticalPanel.ALIGN_RIGHT);

		setWidget(rootPanel);
	}

	public void show() {
		super.show();
		userName.setFocus(true);
	}

	public boolean isSuccessful() {
		return successful;
	}

	private void setErrorMessage(String text) {
		if (rootPanel.getWidgetIndex(errorMessage) == -1) {
			rootPanel.insert(errorMessage, 0);
		}
		errorMessage.setText(text);
	}

	private void clearErrorMessage() {
		rootPanel.remove(errorMessage);
	}

	private void createUser() {
		clearErrorMessage();
		final String passText = password.getText();
		final String passTextAgain = passwordAgain.getText();
		final String userText = userName.getText();
		if (userText == null || (userText.length() == 0)) {
			setErrorMessage("Please enter a user name.");
		} else if (passText == null || passText.length() == 0) {
			setErrorMessage("Please enter a password.");
		} else if (!passText.equals(passTextAgain)) {
			setErrorMessage("Password mismatch. Please re-type passwords.");
		} else {
			// TODO add admin setting
			final UserAccount account = new UserAccount();
			account.setAdministrator(isAdmin.isChecked());
			account.setUserName(userText);
			ServiceHelper.getManageUserService().createUser(account, passText,
					new AsyncCallback() {

						public void onFailure(Throwable caught) {
							ExceptionTracker.logException(caught);

							setErrorMessage("Server unreachable, unable to create user");
						}

						public void onSuccess(Object result) {
							successful = true;
							hide();
						}
					});
		}
	}
}
