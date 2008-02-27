package com.surelogic.sierra.gwt.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.surelogic.sierra.gwt.client.data.ServerInfo;
import com.surelogic.sierra.gwt.client.service.ManageServerServiceAsync;
import com.surelogic.sierra.gwt.client.service.ServiceHelper;

public class ServerInformationContent extends ContentComposite {
	private static final ServerInformationContent instance = new ServerInformationContent();

	private final HTML currentVersion = new HTML();
	private final HTML availableVersion = new HTML();
	private final TextBox emailTextBox = new TextBox();
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
		emailTextBox.setWidth("40ex");
//TODO
//		panel.add(new HTML("<h3>Admin Email</h3>"));
//		panel.add(emailTextBox);
//		final HorizontalPanel hp = new HorizontalPanel();
//		hp.add(updateEmailButton);
//		hp.add(testEmailButton);
//		panel.add(hp);
//		panel.add(message);
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
				message.setHTML("Email updated.");
				msService.setEmail(emailTextBox.getText(), updateServerInfo);
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
		emailTextBox.setText(info.getEmail());
	}
}
