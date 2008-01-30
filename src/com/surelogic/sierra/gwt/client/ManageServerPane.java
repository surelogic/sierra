package com.surelogic.sierra.gwt.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class ManageServerPane extends Composite {

	private HTML currentVersion = new HTML();

	private HTML availableVersion = new HTML();

	private TextBox emailTextBox = new TextBox();

	private Button updateEmailButton = new Button("Update Email Address");

	private AsyncCallback updateServerInfo = new AsyncCallback() {
		public void onSuccess(Object result) {
			updateInfo((ServerInfo) result);
		}

		public void onFailure(Throwable caught) {
			// do some UI stuff to show failure
		}
	};

	public ManageServerPane() {
		updateEmailButton.addClickListener(new ClickListener() {

			public void onClick(Widget sender) {
				getService().setEmail(emailTextBox.getText(), updateServerInfo);
			}
		});
		VerticalPanel panel = new VerticalPanel();
		panel.add(currentVersion);
		panel.add(availableVersion);
		panel.add(emailTextBox);
		panel.add(updateEmailButton);
		updateInfo(ServerInfo.getDefault());
		getService().getServerInfo(updateServerInfo);
		initWidget(panel);
	}

	private ManageServerServiceAsync getService() {
		ManageServerServiceAsync serverService = (ManageServerServiceAsync) GWT
				.create(ManageServerService.class);

		// (2) Specify the URL at which our service implementation is running.
		// Note that the target URL must reside on the same domain and port from
		// which the host page was served.
		//
		ServiceDefTarget endpoint = (ServiceDefTarget) serverService;
		String moduleRelativeURL = GWT.getModuleBaseURL()
				+ "ManageServerService";
		endpoint.setServiceEntryPoint(moduleRelativeURL);
		return serverService;
	}

	private void updateInfo(ServerInfo info) {
		currentVersion.setHTML("<div>Current Version: "
				+ info.getCurrentVersion() + "</div>");
		availableVersion.setHTML("<div>Available Version: "
				+ info.getAvailableVersion() + "</div>");
		emailTextBox.setText(info.getEmail());
	}

}
