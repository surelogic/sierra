package com.surelogic.sierra.gwt.client.content.settings;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.surelogic.sierra.gwt.client.Context;
import com.surelogic.sierra.gwt.client.SessionManager;
import com.surelogic.sierra.gwt.client.content.ContentComposite;
import com.surelogic.sierra.gwt.client.data.ServerInfo;
import com.surelogic.sierra.gwt.client.service.ServiceHelper;
import com.surelogic.sierra.gwt.client.service.callback.StandardCallback;
import com.surelogic.sierra.gwt.client.ui.StyleHelper;
import com.surelogic.sierra.gwt.client.ui.StyleHelper.Style;

public class OneTimeSettingsForm extends ContentComposite {
	private final HTML check = new HTML(
			"Checking current server configuration...");
	private final HTML info = new HTML(
			"The server does not appear to be configured yet.  You will need to specify a name for this particular server.  Please do so now.");
	private final TextBox serverName = new TextBox();
	private final Button updateSiteSettings = new Button("Set Server Name");
	private final HTML errorText = new HTML();
	private final VerticalPanel panel = new VerticalPanel();

	@Override
	protected void onInitialize(final DockPanel rootPanel) {
		panel.addStyleName("one-time-form");
		panel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		StyleHelper.add(errorText, Style.ERROR);
		updateSiteSettings.addClickListener(new ClickListener() {

			public void onClick(final Widget sender) {
				final String text = serverName.getText();
				if (text == null || text.isEmpty()) {
					errorText.setHTML("You must set a server name.");
				} else {
					ServiceHelper.getManageServerService().setSiteName(
							serverName.getText(),
							new StandardCallback<ServerInfo>() {
								@Override
								protected void doSuccess(final ServerInfo result) {
									SessionManager.refreshUser();
								}
							});
				}
			}
		});
		rootPanel.add(panel, DockPanel.CENTER);
	}

	@Override
	protected void onUpdate(final Context context) {
		panel.add(check);
		ServiceHelper.getManageServerService().getServerInfo(
				new StandardCallback<ServerInfo>() {
					@Override
					protected void doSuccess(final ServerInfo result) {
						panel.clear();
						errorText.setText("");
						panel.add(info);
						panel.add(errorText);
						final String hostName = result.getHostName();
						if (hostName != null) {
							serverName.setText(hostName);
						}
						panel.add(serverName);
						panel.add(updateSiteSettings);
					}
				});
	}

	@Override
	protected void onDeactivate() {
		// Nothing to do
	}

}
