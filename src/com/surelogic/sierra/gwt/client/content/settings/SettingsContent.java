package com.surelogic.sierra.gwt.client.content.settings;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.TextBoxBase;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;
import com.google.gwt.user.client.ui.HTMLTable.RowFormatter;
import com.surelogic.sierra.gwt.client.Context;
import com.surelogic.sierra.gwt.client.content.ContentComposite;
import com.surelogic.sierra.gwt.client.data.EmailInfo;
import com.surelogic.sierra.gwt.client.data.ServerInfo;
import com.surelogic.sierra.gwt.client.data.Status;
import com.surelogic.sierra.gwt.client.service.ManageServerServiceAsync;
import com.surelogic.sierra.gwt.client.service.ServiceHelper;
import com.surelogic.sierra.gwt.client.service.callback.StandardCallback;
import com.surelogic.sierra.gwt.client.ui.StatusBox;

public final class SettingsContent extends ContentComposite {
	private static final SettingsContent instance = new SettingsContent();

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

	public static SettingsContent getInstance() {
		return instance;
	}

	private SettingsContent() {
		// singleton
	}

	@Override
	protected void onInitialize(DockPanel rootPanel) {
		final VerticalPanel panel = new VerticalPanel();
		panel
				.add(new HTML(
						"<h3>Version Information</h3><span class=\"settings-info-text\">The below table reports version information about this team server.</span>"));
		final FlexTable t = new FlexTable();
		final FlexCellFormatter tf = t.getFlexCellFormatter();
		t.addStyleName("overview-table");
		t.setWidget(2, 2, availableVersion);
		t.setText(0, 0, "Software");
		tf.setRowSpan(0, 0, 2);
		t.setWidget(2, 1, currentVersion);
		t.setWidget(2, 0, productVersion);
		t.setText(1, 0, "Current");
		t.setText(1, 1, "Available");
		t.setText(0, 1, "Database Schema");
		tf.setColSpan(0, 1, 2);
		final RowFormatter rf = t.getRowFormatter();
		rf.addStyleName(0, "overview-header");
		rf.addStyleName(1, "overview-header");
		rf.addStyleName(2, "overview-data");
		for (int i = 0; i < 3; i++) {
			tf.addStyleName(2, i, "cell-number");
		}
		panel.add(t);
		adminEmailTextBox.setWidth("40ex");
		serverEmailTextBox.setWidth("40ex");
		smtpHostTextBox.setWidth("40ex");
		smtpPortTextBox.setWidth("40ex");
		smtpUserTextBox.setWidth("40ex");
		smtpPassTextBox.setWidth("40ex");
		final FlexTable at = new FlexTable();
		at.setWidget(5, 1, smtpPassTextBox);
		at.setWidget(4, 1, smtpUserTextBox);
		at.setWidget(3, 1, smtpPortTextBox);
		at.setWidget(2, 1, smtpHostTextBox);
		at.setWidget(1, 1, serverEmailTextBox);
		at.setWidget(0, 1, adminEmailTextBox);
		at.setWidget(5, 0, createLabel("SMTP Password (Optional):"));
		at.setWidget(4, 0, createLabel("STMP User (Optional):"));
		at.setWidget(3, 0, createLabel("Port:"));
		at.setWidget(2, 0, createLabel("SMTP Host:"));
		at.setWidget(1, 0, createLabel("From:"));
		at.setWidget(0, 0, createLabel("To:"));
		panel
				.add(new HTML(
						"<h3>Administration Email</h3><span class=\"settings-info-text\">The below settings configure this team server to send email about any problems it encounters to a designated administrator.</span>"));
		panel.add(at);
		final HorizontalPanel hp = new HorizontalPanel();
		hp.add(updateEmailButton);
		hp.add(testEmailButton);
		panel.add(hp);
		panel.add(status);
		updateInfo(ServerInfo.getDefault());
		getRootPanel().add(panel, DockPanel.CENTER);
	}

	@Override
	protected void onUpdate(Context context) {
		final AsyncCallback<ServerInfo> updateServerInfo = new StandardCallback<ServerInfo>() {
			@Override
			protected void doSuccess(ServerInfo result) {
				updateInfo(result);
				status.setStatus(new Status(true, "Information updated."));
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
				msService.setEmail(email, new StandardCallback<ServerInfo>() {
					@Override
					protected void doSuccess(ServerInfo result) {

						msService.testAdminEmail(new StandardCallback<Void>() {
							@Override
							protected void doSuccess(Void result) {
								status.setStatus(Status
										.success("Test email sent."));
							}

						});
						status
								.setStatus(new Status(
										true,
										"If notification is set up correctly, you should receive an email at the given address."));
					}

				});
			}
		});
		msService.getServerInfo(new StandardCallback<ServerInfo>() {

			@Override
			protected void doSuccess(ServerInfo result) {
				updateInfo(result);
			}

		});
	}

	@Override
	protected void onDeactivate() {
		// nothing to do
	}

	private Label createLabel(String text) {
		final Label label = new Label(text);
		label.addStyleName(".settings-label-text");
		return label;
	}

	private void updateInfo(ServerInfo info) {
		currentVersion.setHTML(info.getCurrentVersion());
		availableVersion.setHTML(info.getAvailableVersion());
		productVersion.setHTML(info.getProductVersion());
		final EmailInfo email = info.getEmail();
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
