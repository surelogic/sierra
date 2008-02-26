package com.surelogic.sierra.gwt.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.surelogic.sierra.gwt.client.service.ServiceHelper;

public class ChangePasswordContent extends ContentComposite {
	private static final ChangePasswordContent instance = new ChangePasswordContent();

	private final PasswordTextBox oldPass = new PasswordTextBox();
	private final PasswordTextBox pass = new PasswordTextBox();
	private final PasswordTextBox passAgain = new PasswordTextBox();
	private final HTML message = new HTML();

	public static final ChangePasswordContent getInstance() {
		return instance;
	}

	private ChangePasswordContent() {
		super();
	}

	public String getContextName() {
		return "ChangePassword";
	}

	protected void onInitialize(DockPanel rootPanel) {
		final VerticalPanel vp = new VerticalPanel();
		vp.add(new HTML("Enter your current password<br />"));
		vp.add(oldPass);
		vp.add(new HTML("Enter your new password<br />"));
		vp.add(pass);
		vp.add(new HTML("Enter your new password again<br />"));
		vp.add(passAgain);
		HorizontalPanel hp = new HorizontalPanel();
		hp.add(new Button("Clear", new ClickListener() {
			public void onClick(Widget sender) {
				oldPass.setText("");
				pass.setText("");
				passAgain.setText("");
			}
		}));
		hp.add(new Button("Change Password", new ClickListener() {
			public void onClick(Widget sender) {
				final String oldPassText = oldPass.getText();
				final String passText = pass.getText();
				final String passAgainText = passAgain.getText();
				if (!isFilled(oldPassText) || !isFilled(passText)
						|| !isFilled(passAgainText)) {
					message
							.setHTML("<span class=\"error\">Please fill out all of the password fields.</span>");
				} else if (passText.equals(passAgainText)) {
					ServiceHelper.getManagePrefsService().changePassword(
							oldPassText, passText, new AsyncCallback() {

								public void onFailure(Throwable caught) {
									// TODO handle failure
								}

								public void onSuccess(Object result) {
									Boolean success = (Boolean) result;
									if (success.booleanValue()) {
										reset();
										message
												.setHTML("<span class=\"success\">Your password was changed.</span>");
									} else {
										message
												.setHTML("<span class=\"error\">Your password could not be changed.  Please make sure that you have entered your old password correctly.</span>");
									}
								}

							});
				} else {
					message
							.setHTML("<span class=\"error\">Your password fields do not match.  Please enter your new password twice.</span>");
				}
			}
		}));
		vp.add(hp);
		vp.add(message);

		rootPanel.clear();
		rootPanel.add(vp, DockPanel.CENTER);
	}

	protected void onActivate() {
		reset();
	}

	protected boolean onDeactivate() {
		return true;
	}

	private void reset() {
		oldPass.setText("");
		pass.setText("");
		passAgain.setText("");
		message.setHTML("");
	}

	private static boolean isFilled(String str) {
		return !(str == null || (str.length() == 0));
	}
}
