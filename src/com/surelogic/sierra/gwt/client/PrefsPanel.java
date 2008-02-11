package com.surelogic.sierra.gwt.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.surelogic.sierra.gwt.client.service.ServiceHelper;

public class PrefsPanel extends ContentComposite {

	private final PasswordTextBox oldPass = new PasswordTextBox();
	private final PasswordTextBox pass = new PasswordTextBox();
	private final PasswordTextBox passAgain = new PasswordTextBox();
	private final HTML message = new HTML();

	public PrefsPanel() {
		super();
		final VerticalPanel vp = new VerticalPanel();
		vp
				.add(new HTML(
						"<h2>Change Password</h2><h3>Enter your current password</h3>"));
		vp.add(oldPass);
		vp.add(new HTML("<h3>Enter your new password</h3>"));
		vp.add(pass);
		vp.add(new HTML("<h3>Enter your new password again</h3>"));
		vp.add(passAgain);
		vp.add(new Button("Change Password", new ClickListener() {

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
										// TODO This should not happen. Consider
										// handling this
										// differently.
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

		vp.add(message);

		final DockPanel rootPanel = getRootPanel();
		rootPanel.clear();
		rootPanel.add(vp, DockPanel.CENTER);
	}

	public void activate() {
		reset();
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
