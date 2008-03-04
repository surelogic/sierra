package com.surelogic.sierra.gwt.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.surelogic.sierra.gwt.client.data.EmailInfo;
import com.surelogic.sierra.gwt.client.data.ServerInfo;
import com.surelogic.sierra.gwt.client.service.ManageServerServiceAsync;
import com.surelogic.sierra.gwt.client.service.ServiceHelper;

public class ServerInformationContent extends ContentComposite {
	private static final ServerInformationContent instance = new ServerInformationContent();

	private final HTML currentVersion = new HTML();
	private final HTML availableVersion = new HTML();
	private final TextBox adminEmailTextBox = new TextBox();
	private final TextBox serverEmailTextBox = new TextBox();
	private final TextBox smtpHostTextBox = new TextBox();
	private final TextBox smtpPortTextBox = new TextBox();
	private final TextBox smtpUserTextBox = new TextBox();
	private final TextBox smtpPassTextBox = new TextBox();
	private final Button updateEmailButton = new Button("Update Email Address");
	private final Button testEmailButton = new Button("Test Email Notification");
	private final HTML message = new HTML();

	public static ServerInformationContent getInstance() {
		return instance;
	}

	private ServerInformationContent() {
		super();
	}

	public String getContextName() {
		return "ServerSettings";
	}

	protected void onInitialize(DockPanel rootPanel) {
		final VerticalPanel panel = new VerticalPanel();
		panel.add(new HTML("<h3>Database Schema Version</h3>"));
		panel.add(currentVersion);
		panel.add(availableVersion);
		adminEmailTextBox.setWidth("40ex");
		serverEmailTextBox.setWidth("40ex");
		smtpHostTextBox.setWidth("40ex");
		smtpPortTextBox.setWidth("10ex");
		smtpUserTextBox.setWidth("40ex");
		smtpPassTextBox.setWidth("40ex");
		panel.add(new HTML("<h3>Admin Email</h3>"));
		panel.add(adminEmailTextBox);
		panel.add(new HTML("<h3>Server Email</h3>"));
		panel.add(serverEmailTextBox);
		panel.add(new HTML("<h3>SMTP Host</h3>"));
		HorizontalPanel hostPanel = new HorizontalPanel();
		hostPanel.add(smtpHostTextBox);
		hostPanel.add(new HTML("<span>  Port:</span"));
		hostPanel.add(smtpPortTextBox);
		panel.add(hostPanel);
		panel.add(new HTML("<h3>STMP User (Optional)</h3>"));
		panel.add(smtpUserTextBox);
		panel.add(new HTML("<h3>SMTP Password (Optional)</h3>"));
		panel.add(smtpPassTextBox);
		final HorizontalPanel hp = new HorizontalPanel();
		hp.add(updateEmailButton);
		hp.add(testEmailButton);
		panel.add(hp);
		panel.add(message);
		updateInfo(ServerInfo.getDefault());
		getRootPanel().add(panel, DockPanel.CENTER);
	}

	protected void onActivate() {
		message.setStyleName("success");
		final AsyncCallback updateServerInfo = new AsyncCallback() {
			public void onSuccess(Object result) {
				updateInfo((ServerInfo) result);
			}

			public void onFailure(Throwable caught) {
				// TODO do some UI stuff to show failure
			}
		};

		final ManageServerServiceAsync msService = ServiceHelper
				.getManageServerService();
		updateEmailButton.addClickListener(new ClickListener() {

			public void onClick(Widget sender) {
				final EmailInfo email = new EmailInfo(
						getString(smtpHostTextBox), getString(smtpPortTextBox),
						getString(smtpUserTextBox), getString(smtpPassTextBox),
						getString(serverEmailTextBox),
						getString(adminEmailTextBox));
				msService.setEmail(email, updateServerInfo);
				message.setHTML("Email updated.");
			}
		});
		testEmailButton.addClickListener(new ClickListener() {

			public void onClick(Widget sender) {
				msService.testAdminEmail(new AsyncCallback() {

					public void onFailure(Throwable caught) {
						// TODO Auto-generated method stub
					}

					public void onSuccess(Object result) {
						// TODO Auto-generated method stub
					}
				});
				message
						.setHTML("If notification is set up correctly, you should receive an email at the given address.");
			}
		});
		msService.getServerInfo(updateServerInfo);
	}

	protected boolean onDeactivate() {
		return true;
	}

	private void updateInfo(ServerInfo info) {
		currentVersion.setHTML("<div>Current Version: "
				+ info.getCurrentVersion() + "</div>");
		availableVersion.setHTML("<div>Available Version: "
				+ info.getAvailableVersion() + "</div>");
		EmailInfo email = info.getEmail();
		adminEmailTextBox.setText(email.getAdminEmail());
		serverEmailTextBox.setText(email.getServerEmail());
		smtpHostTextBox.setText(email.getHost());
		smtpUserTextBox.setText(email.getUser());
		smtpPassTextBox.setText(email.getPass());
		smtpPortTextBox.setText(email.getPort());
	}

	private static String getString(TextBox box) {
		final String text = box.getText();
		return ((text == null) || text.length() == 0) ? null : text;
	}
}
