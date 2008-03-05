package com.surelogic.sierra.gwt.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.TextBoxBase;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.surelogic.sierra.gwt.client.data.EmailInfo;
import com.surelogic.sierra.gwt.client.data.ServerInfo;
import com.surelogic.sierra.gwt.client.data.Status;
import com.surelogic.sierra.gwt.client.service.ManageServerServiceAsync;
import com.surelogic.sierra.gwt.client.service.ServiceHelper;
import com.surelogic.sierra.gwt.client.ui.StatusBox;

public class ServerInformationContent extends ContentComposite {
	private static final ServerInformationContent instance = new ServerInformationContent();

	private final HTML currentVersion = new HTML();
	private final HTML availableVersion = new HTML();
	private final TextBox adminEmailTextBox = new TextBox();
	private final TextBox serverEmailTextBox = new TextBox();
	private final TextBox smtpHostTextBox = new TextBox();
	private final TextBox smtpPortTextBox = new TextBox();
	private final TextBox smtpUserTextBox = new TextBox();
	private final PasswordTextBox smtpPassTextBox = new PasswordTextBox();
	private final Button updateEmailButton = new Button("Update Email Address");
	private final Button testEmailButton = new Button("Test Email Notification");
	private final StatusBox status = new StatusBox();

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
		panel
				.add(new HTML(
						"<h3>Version Information</h3>The below table reports version information about this team server."));
		HorizontalPanel hp1 = new HorizontalPanel();
		VerticalPanel vp1 = new VerticalPanel();
		vp1.add(new HTML(""));
		vp1.add(new HTML("Software"));
		vp1.add(new HTML(""));
		hp1.add(vp1);
		VerticalPanel vp2 = new VerticalPanel();
		vp2.add(new HTML("Database Schema"));
		HorizontalPanel hp2 = new HorizontalPanel();
		VerticalPanel vp3 = new VerticalPanel();
		vp3.add(new HTML("Current"));
		vp3.add(currentVersion);
		VerticalPanel vp4 = new VerticalPanel();
		vp4.add(new HTML("Available"));
		vp4.add(availableVersion);
		hp2.add(vp3);
		hp2.add(vp4);
		vp2.add(hp2);
		hp1.add(vp2);
		panel.add(hp1);
		adminEmailTextBox.setWidth("40ex");
		serverEmailTextBox.setWidth("40ex");
		smtpHostTextBox.setWidth("40ex");
		smtpPortTextBox.setWidth("10ex");
		smtpUserTextBox.setWidth("40ex");
		smtpPassTextBox.setWidth("40ex");
		panel
				.add(new HTML(
						"<h3>Administration Email</h3>The below settings configure this team server to send email about any problems it encounters to a designated administrator."));
		HorizontalPanel hp = new HorizontalPanel();
		hp.add(new HTML("To:"));
		hp.add(adminEmailTextBox);
		panel.add(hp);
		hp = new HorizontalPanel();
		hp.add(new HTML("From:"));
		hp.add(serverEmailTextBox);
		panel.add(hp);
		hp = new HorizontalPanel();
		hp.add(new HTML("SMTP Host:"));
		hp.add(smtpHostTextBox);
		hp.add(new HTML("Port:"));
		hp.add(smtpPortTextBox);
		panel.add(hp);
		hp = new HorizontalPanel();
		hp.add(new HTML("STMP User (Optional):"));
		hp.add(smtpUserTextBox);
		panel.add(hp);
		hp = new HorizontalPanel();
		hp.add(new HTML("SMTP Password (Optional):"));
		hp.add(smtpPassTextBox);
		panel.add(hp);
		hp = new HorizontalPanel();
		hp.add(updateEmailButton);
		hp.add(testEmailButton);
		panel.add(hp);
		panel.add(status);
		updateInfo(ServerInfo.getDefault());
		getRootPanel().add(panel, DockPanel.CENTER);
	}

	protected void onActivate() {
		final AsyncCallback updateServerInfo = new AsyncCallback() {
			public void onSuccess(Object result) {
				updateInfo((ServerInfo) result);
				status.setStatus(new Status(true, "Information updated."));
			}

			public void onFailure(Throwable caught) {
				status.setStatus(new Status(false,
						"Error communicating with server"));
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
			}
		});
		testEmailButton.addClickListener(new ClickListener() {

			public void onClick(Widget sender) {
				final EmailInfo email = new EmailInfo(
						getString(smtpHostTextBox), getString(smtpPortTextBox),
						getString(smtpUserTextBox), getString(smtpPassTextBox),
						getString(serverEmailTextBox),
						getString(adminEmailTextBox));
				msService.setEmail(email, new AsyncCallback() {
					public void onSuccess(Object result) {

						msService.testAdminEmail(new AsyncCallback() {
							public void onSuccess(Object result) {
								updateInfo((ServerInfo) result);
							}

							public void onFailure(Throwable caught) {
								status.setStatus(new Status(false,
										"Error communicating with server"));
								// TODO do some UI stuff to show failure
							}
						});
						status
								.setStatus(new Status(
										true,
										"If notification is set up correctly, you should receive an email at the given address."));
					}

					public void onFailure(Throwable caught) {
						status.setStatus(new Status(false,
								"Error communicating with server"));
						// TODO do some UI stuff to show failure
					}
				});
			}
		});
		msService.getServerInfo(new AsyncCallback() {

			public void onSuccess(Object result) {
				updateInfo((ServerInfo) result);
			}

			public void onFailure(Throwable caught) {
				status.setStatus(new Status(false,
						"Error communicating with server"));
				// TODO do some UI stuff to show failure
			}
		});
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

	private static String getString(TextBoxBase box) {
		final String text = box.getText();
		return ((text == null) || text.length() == 0) ? null : text;
	}
}
