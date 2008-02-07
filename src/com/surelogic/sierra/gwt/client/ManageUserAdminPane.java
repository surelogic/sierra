package com.surelogic.sierra.gwt.client;

import java.util.Iterator;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.surelogic.sierra.gwt.client.data.UserAccount;

public class ManageUserAdminPane extends Composite {

	private final Panel userListPanel = new VerticalPanel();
	private final TextBox userSearch = new TextBox();

	private final TextBox newUserName = new TextBox();
	private final PasswordTextBox password = new PasswordTextBox();
	private final PasswordTextBox passwordAgain = new PasswordTextBox();
	private final HTML userCreationMessage = new HTML();

	private final Panel userDetailPanel = new VerticalPanel();
	private String detailUser;
	private final HTML detailUserName = new HTML();
	private final PasswordTextBox detailPassword = new PasswordTextBox();
	private final PasswordTextBox detailPasswordAgain = new PasswordTextBox();
	private final CheckBox detailIsAdminBox = new CheckBox("Administrator");
	private final HTML detailUserUpdateMessage = new HTML();

	private final AsyncCallback userListCallback = new AsyncCallback() {

		public void onFailure(Throwable caught) {
			// TODO do something
		}

		public void onSuccess(Object result) {
			userListPanel.clear();
			List users = (List) result;
			if (users.isEmpty()) {
				userListPanel.add(new HTML("No results found"));
			} else {
				for (Iterator i = users.iterator(); i.hasNext();) {
					final String user = (String) i.next();
					final HTML html = new HTML("<span class=\"clickable\">"
							+ user + "</span");
					html.addClickListener(new ClickListener() {

						public void onClick(Widget sender) {
							getService().getUserInfo(user, userInfoCallback);
						}
					});
					userListPanel.add(html);
				}
			}
		}
	};

	private final AsyncCallback userInfoCallback = new AsyncCallback() {

		public void onFailure(Throwable caught) {
			// TODO handle gracefully
		}

		public void onSuccess(Object result) {
			updateUserInfo((UserAccount) result);
		}
	};

	ManageUserAdminPane() {
		/*
		 * User search panel initialization
		 */
		VerticalPanel panel = new VerticalPanel();
		HorizontalPanel searchPanel = new HorizontalPanel();
		searchPanel.add(userSearch);
		searchPanel.add(new Button("Search for User", new ClickListener() {
			public void onClick(Widget sender) {
				getService().findUser(userSearch.getText(), userListCallback);
			}
		}));
		searchPanel.add(new Button("List all Users", new ClickListener() {
			public void onClick(Widget sender) {
				getService().getUsers(userListCallback);
			}
		}));
		panel.add(searchPanel);
		panel.add(userListPanel);
		/*
		 * User Detail Info
		 */
		userDetailPanel.add(new HTML("<h2>User Detail</h2>"));
		userDetailPanel.add(detailUserName);
		HorizontalPanel hDetail = new HorizontalPanel();
		VerticalPanel vDetail = new VerticalPanel();
		vDetail.add(new HTML("<h3>Enter Password</h3"));
		vDetail.add(detailPassword);
		vDetail.add(new HTML("<h3>Enter Password Again</h3"));
		vDetail.add(detailPasswordAgain);
		hDetail.add(vDetail);
		hDetail.add(detailIsAdminBox);
		userDetailPanel.add(hDetail);
		userDetailPanel.add(new Button("Update User", new ClickListener() {

			public void onClick(Widget sender) {
				String password = null;
				if ((detailPassword.getText() != null)
						&& (detailPassword.getText().length() > 0)
						&& (detailPassword.getText().equals(detailPasswordAgain
								.getText()))) {
					password = detailPassword.getText();
				}
				getService().updateUser(detailUser, password,
						detailIsAdminBox.isChecked(), userInfoCallback);
			}
		}));
		userDetailPanel.add(detailUserUpdateMessage);
		userDetailPanel.setVisible(false);
		panel.add(userDetailPanel);
		/*
		 * User creation initialization
		 */
		panel.add(new HTML(
				"<h2>Create a new user</h2><h3>Enter your user name:</h3>"));
		panel.add(newUserName);
		panel.add(new HTML("<h3>Please enter your password twice:</h3>"));
		panel.add(password);
		panel.add(passwordAgain);
		panel.add(userCreationMessage);
		panel.add(new Button("Create User", new ClickListener() {

			public void onClick(Widget sender) {
				final String passText = password.getText();
				final String passTextAgain = passwordAgain.getText();
				final String userText = newUserName.getText();
				if (userText == null || (userText.length() == 0)) {
					userCreationMessage.setHTML("Please enter a user name.");
				} else if ((passText == null) || (userText.length() == 0)
						|| !passText.equals(passTextAgain)) {
					userCreationMessage
							.setHTML("The given password is invalid.  Please make sure that you have specified a password and that both entries match.");
				} else {
					getService().createUser(userText, passText,
							new AsyncCallback() {

								public void onFailure(Throwable caught) {
									// TODO handle failure
								}

								public void onSuccess(Object result) {
									userCreationMessage
											.setText((String) result);
								}
							});
				}
			}
		}));
		initWidget(panel);
	}

	private void updateUserInfo(UserAccount info) {
		detailUser = info.getUserName();
		detailUserName.setHTML("<h3>" + detailUser + "</h3>");
		detailIsAdminBox.setChecked(info.isAdministrator());
		detailPassword.setText("");
		detailPasswordAgain.setText("");
		userDetailPanel.setVisible(true);
	}

	private ManageUserAdminServiceAsync getService() {
		ManageUserAdminServiceAsync serverService = (ManageUserAdminServiceAsync) GWT
				.create(ManageUserAdminService.class);

		// (2) Specify the URL at which our service implementation is running.
		// Note that the target URL must reside on the same domain and port from
		// which the host page was served.
		//
		ServiceDefTarget endpoint = (ServiceDefTarget) serverService;
		String moduleRelativeURL = GWT.getModuleBaseURL() + "ManageUserService";
		endpoint.setServiceEntryPoint(moduleRelativeURL);
		return serverService;
	}
}
