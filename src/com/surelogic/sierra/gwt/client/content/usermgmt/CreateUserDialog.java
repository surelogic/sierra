package com.surelogic.sierra.gwt.client.content.usermgmt;

import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasFocus;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;
import com.surelogic.sierra.gwt.client.data.Status;
import com.surelogic.sierra.gwt.client.data.UserAccount;
import com.surelogic.sierra.gwt.client.service.ServiceHelper;
import com.surelogic.sierra.gwt.client.service.callback.ResultCallback;
import com.surelogic.sierra.gwt.client.ui.dialog.FormDialog;

public class CreateUserDialog extends FormDialog {
	private final TextBox userName = new TextBox();
	private final PasswordTextBox password = new PasswordTextBox();
	private final PasswordTextBox passwordAgain = new PasswordTextBox();
	private final CheckBox isAdmin = new CheckBox();

	public CreateUserDialog() {
		super("Create a User", null);
	}

	@Override
	protected void doInitialize(final FlexTable contentTable) {
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
					new ResultCallback<String>() {

						@Override
						protected void doException(final Throwable caught) {
							hide();
						}

						@Override
						protected void doFailure(final String message,
								final String result) {
							setStatus(Status.failure(message));
							hide();
						}

						@Override
						protected void doSuccess(final String message,
								final String result) {
							setStatus(Status.success(message));
							hide();
						}
					});
		}
	}
}
