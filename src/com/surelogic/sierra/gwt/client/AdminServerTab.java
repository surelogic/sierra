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

public class AdminServerTab extends TabComposite {

	private final HTML currentVersion = new HTML();
	private final HTML availableVersion = new HTML();
	private final TextBox emailTextBox = new TextBox();
	private final Button updateEmailButton = new Button("Update Email Address");

	public AdminServerTab() {
		super();
		VerticalPanel panel = new VerticalPanel();
		panel.add(new HTML("<h3>Server Version</h3>"));
		panel.add(currentVersion);
		panel.add(availableVersion);
		emailTextBox.setWidth("40ex");
		panel.add(new HTML("<h3>Admin Email</h3>"));
		panel.add(emailTextBox);
		panel.add(updateEmailButton);
		updateInfo(ServerInfo.getDefault());

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
				msService.setEmail(emailTextBox.getText(), updateServerInfo);
			}
		});

		msService.getServerInfo(updateServerInfo);
		getRootPanel().add(panel, DockPanel.CENTER);
	}

	private void updateInfo(ServerInfo info) {
		currentVersion.setHTML("<div>Current Version: "
				+ info.getCurrentVersion() + "</div>");
		availableVersion.setHTML("<div>Available Version: "
				+ info.getAvailableVersion() + "</div>");
		emailTextBox.setText(info.getEmail());
	}

}
