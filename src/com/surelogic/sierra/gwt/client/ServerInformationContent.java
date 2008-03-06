package com.surelogic.sierra.gwt.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.TextBoxBase;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;
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
	private final HTML productVersion = new HTML();
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
						"<h3>Version Information</h3><span class=\"settings-info-text\">The below table reports version information about this team server.</span>"));
		final FlexTable t = new FlexTable();
		t.addStyleName("settings-version-table");
		t.setText(0, 0, "");
		t.setWidget(2, 2, availableVersion);
		t.setWidget(2, 1, currentVersion);
		t.setWidget(2, 0, productVersion);
		t.setText(1, 2, "Available");
		t.setText(1, 1, "Current");
		t.setText(1, 0, "Software");
		t.setText(0, 1, "Database Schema");
		final FlexCellFormatter tf = t.getFlexCellFormatter();
		tf.setColSpan(0, 1, 2);
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				tf.addStyleName(i, j, "settings-version-td");
			}
		}
		panel.add(t);
		adminEmailTextBox.setWidth("40ex");
		serverEmailTextBox.setWidth("40ex");
		smtpHostTextBox.setWidth("40ex");
		smtpPortTextBox.setWidth("10ex");
		smtpUserTextBox.setWidth("40ex");
		smtpPassTextBox.setWidth("40ex");
		panel
				.add(new HTML(
						"<h3>Administration Email</h3><span class=\"settings-info-text\">The below settings configure this team server to send email about any problems it encounters to a designated administrator.</span>"));
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
		currentVersion.setHTML(info.getCurrentVersion());
		availableVersion.setHTML(info.getAvailableVersion());
		productVersion.setHTML(info.getProductVersion());
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
