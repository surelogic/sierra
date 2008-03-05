package com.surelogic.sierra.gwt.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.surelogic.sierra.gwt.client.data.Status;
import com.surelogic.sierra.gwt.client.data.UserAccount;
import com.surelogic.sierra.gwt.client.service.ServiceHelper;
import com.surelogic.sierra.gwt.client.util.ExceptionTracker;

public class ChangePasswordDialog extends DialogBox {
	private final VerticalPanel rootPanel = new VerticalPanel();
	private final FlexTable userGrid = new FlexTable();
	private final Label errorMessage = new Label("", false);
	private final PasswordTextBox userPassword = new PasswordTextBox();
	private final PasswordTextBox password = new PasswordTextBox();
	private final PasswordTextBox passwordAgain = new PasswordTextBox();
	private final UserAccount user;
	private boolean successful;

	public ChangePasswordDialog(UserAccount user) {
		super();
		this.user = user;
		setText("Change Password");

		errorMessage.addStyleName("error");
		userGrid.setText(1, 0, "Your Password");
		userGrid.setWidget(1, 1, userPassword);
		userGrid.setText(2, 0, "New Password"
				+ (ClientContext.getUser().getUserName().equals(
						user.getUserName()) ? "" : "for " + user.getUserName())
				+ ":");
		userGrid.setWidget(2, 1, password);
		userGrid.setText(3, 0, "Confirm Password:");
		userGrid.setWidget(3, 1, passwordAgain);
		rootPanel.add(userGrid);

		final HorizontalPanel buttonPanel = new HorizontalPanel();
		final Button ok = new Button("Ok");
		ok.addClickListener(new ClickListener() {

			public void onClick(Widget sender) {
				changePassword();
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
		userPassword.setFocus(true);
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

	private void changePassword() {
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

					new AsyncCallback() {

						public void onFailure(Throwable caught) {
							ExceptionTracker.logException(caught);

							setErrorMessage("Server unreachable, unable to create user");
						}

						public void onSuccess(Object result) {
							Status status = (Status) result;
							successful = status.isSuccess();
							if (!successful) {
								setErrorMessage(status.getMessage());
							} else {
								hide();
							}
						}
					});
		}
	}

}
