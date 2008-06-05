package com.surelogic.sierra.gwt.client.usermgmt;

import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasFocus;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;
import com.surelogic.sierra.gwt.client.data.Status;
import com.surelogic.sierra.gwt.client.data.UserAccount;
import com.surelogic.sierra.gwt.client.service.Callback;
import com.surelogic.sierra.gwt.client.service.ServiceHelper;
import com.surelogic.sierra.gwt.client.ui.FormDialog;

public class CreateUserDialog extends FormDialog {
	private final TextBox userName = new TextBox();
	private final PasswordTextBox password = new PasswordTextBox();
	private final PasswordTextBox passwordAgain = new PasswordTextBox();
	private final CheckBox isAdmin = new CheckBox();

	@Override
	protected void doInitialize(FlexTable contentTable) {
		setText("Create a user");
		addField("User Name:", userName);
		addField("Password:", password);
		addField("Confirm Password:", passwordAgain);
		addField("Administrator", isAdmin);
	}

	@Override
	protected HasFocus getInitialFocus() {
		return userName;
	}

	@Override
	protected void doOkClick() {
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
			final UserAccount account = new UserAccount();
			account.setAdministrator(isAdmin.isChecked());
			account.setUserName(userText);
			ServiceHelper.getManageUserService().createUser(account, passText,
					new Callback<String>() {

						@Override
						protected void onException(Throwable caught) {
							setErrorMessage("Unable to create user. (Server may be down)");
						}

						@Override
						protected void onFailure(String message, String result) {
							setStatus(Status.failure(message));
							hide();
						}

						@Override
						protected void onSuccess(String message, String result) {
							setStatus(Status.success(message));
							hide();
						}
					});
		}
	}
}
