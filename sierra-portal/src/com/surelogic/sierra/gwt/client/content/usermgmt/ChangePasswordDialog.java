package com.surelogic.sierra.gwt.client.content.usermgmt;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasFocus;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.surelogic.sierra.gwt.client.SessionManager;
import com.surelogic.sierra.gwt.client.data.UserAccount;
import com.surelogic.sierra.gwt.client.service.ServiceHelper;
import com.surelogic.sierra.gwt.client.service.callback.ResultCallback;
import com.surelogic.sierra.gwt.client.ui.dialog.FormDialog;

public class ChangePasswordDialog extends FormDialog {
	private final Label userPasswordTitle = new Label("", false);
	private final PasswordTextBox userPassword = new PasswordTextBox();
	private final PasswordTextBox password = new PasswordTextBox();
	private final PasswordTextBox passwordAgain = new PasswordTextBox();
	private UserAccount user;

	public ChangePasswordDialog() {
		super("Change Password", null);
	}

	public void update(final UserAccount user) {
		this.user = user;

		final StringBuffer pwdTitle = new StringBuffer("New Password");
		if (!SessionManager.getUser().getUserName().equals(user.getUserName())) {
			pwdTitle.append(" for ").append(user.getUserName());
		}
		pwdTitle.append(":");
		userPasswordTitle.setText(pwdTitle.toString());
	}

	@Override
	protected void doInitialize(final FlexTable contentTable) {
		addField("Your Password:", userPassword);
		addField(userPasswordTitle, password);
		addField("Confirm Password:", passwordAgain);
	}

	@Override
	protected HasFocus getInitialFocus() {
		return userPassword;
	}

	@Override
	protected void doOkClick() {
		clearErrorMessage();
		final String currentPassText = userPassword.getText();
		final String passText = password.getText();
		final String passTextAgain = passwordAgain.getText();
		if (currentPassText.length() == 0 || passText.length() == 0
				|| passTextAgain.length() == 0) {
			setErrorMessage("Please fill out all required fields");
		} else if (!passText.equals(passTextAgain)) {
			setErrorMessage("Password mismatch. Please ensure both passwords match.");
		} else {
			ServiceHelper.getManageUserService().changeUserPassword(
					user.getUserName(), currentPassText, passText,

					new ResultCallback<String>() {

						@Override
						protected void doFailure(final String message,
								final String result) {
							setErrorMessage("Unable to change password: "
									+ message);
						}

						@Override
						protected void doSuccess(final String message,
								final String result) {
							hide();
						}
					});
		}
	}

}
