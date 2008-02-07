package com.surelogic.sierra.gwt.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;
import com.surelogic.sierra.gwt.client.data.UserAccount;

public class HeaderPanel extends Composite {
	private final DockPanel rootPanel = new DockPanel();
	private final HorizontalPanel userPanel = new HorizontalPanel();

	public static HeaderPanel getInstance() {
		return (HeaderPanel) RootPanel.get("header-pane").getWidget(0);
	}

	public HeaderPanel() {
		super();
		initWidget(rootPanel);

		rootPanel.setVerticalAlignment(DockPanel.ALIGN_MIDDLE);
		rootPanel.setWidth("100%");

		Image sierraLogo = new Image(GWT.getModuleBaseURL()
				+ "images/header-sierra-logo.gif");
		rootPanel.add(sierraLogo, DockPanel.WEST);

		
		rootPanel.add(userPanel, DockPanel.EAST);
		rootPanel.setCellHorizontalAlignment(userPanel, DockPanel.ALIGN_RIGHT);
	}

	public void updateAccountPanel(UserAccount user) {
		if (user != null) {
			userPanel.add(createUserLabel("Logged in as " + user.getUserName(), null));
			userPanel.add(createUserLabel("|", null));
			userPanel.add(createUserLabel("Preferences", new PreferencesListener()));
			userPanel.add(createUserLabel("|", null));
			userPanel.add(createUserLabel("Log out", new LogoutListener()));
		} else {
			userPanel.clear();
		}
	}
	
	private Label createUserLabel(String text, ClickListener clickListener) {
		final Label lbl = new Label(text);
		lbl.addStyleName("header-user-label");
		if (clickListener != null) {
			lbl.addStyleName("header-user-clickable");
			lbl.addClickListener(clickListener);
		}
		return lbl;
	}
	
	private static class PreferencesListener implements ClickListener {

		public void onClick(Widget sender) {
			// TODO Auto-generated method stub
			
		}
		
	}
	private static class LogoutListener implements ClickListener {

		public void onClick(Widget sender) {
			// TODO Auto-generated method stub
			
		}
		
	}
}
