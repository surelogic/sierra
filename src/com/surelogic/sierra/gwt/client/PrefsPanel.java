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

	private PasswordTextBox oldPass;
	private PasswordTextBox pass;
	private PasswordTextBox passAgain;
	private HTML message;

	public void activate() {
		final DockPanel rootPanel = getRootPanel();
		rootPanel.clear();
		final VerticalPanel vp = new VerticalPanel();
		vp
				.add(new HTML(
						"<h2>Change Password</h3><h2>Enter your current password</h2>"));
		vp.add(oldPass = new PasswordTextBox());
		vp.add(new HTML("<h3>Enter your new password</h3>"));
		vp.add(pass = new PasswordTextBox());
		vp.add(new HTML("<h3>Enter your new password again</h3>"));
		vp.add(passAgain = new PasswordTextBox());
		vp.add(new Button("Change Password", new PasswordListener()));
		vp.add(message = new HTML());
		rootPanel.add(vp, DockPanel.CENTER);
	}

	private class PasswordListener implements ClickListener {

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
						oldPassText, passText, new PasswordCallback());
			} else {
				message
						.setHTML("<span class=\"error\">Your password fields do not match.  Please enter your new password twice.</span>");
			}
		}
	}

	private class PasswordCallback implements AsyncCallback {

		public void onFailure(Throwable caught) {
			// TODO handle failure
		}

		public void onSuccess(Object result) {
			Boolean success = (Boolean) result;
			if (success.booleanValue()) {
				message
						.setHTML("<span class=\"success\">Your password was changed.</span>");
				oldPass.setText("");
				pass.setText("");
				passAgain.setText("");
			} else {
				// TODO This should not happen. Consider handling this
				// differently.
				message
						.setHTML("<span class=\"error\">Your password could not be changed.  Please make sure that you have entered your old password correctly.</span>");
			}
		}

	}

	private static boolean isFilled(String str) {
		return !(str == null || (str.length() == 0));
	}
}
