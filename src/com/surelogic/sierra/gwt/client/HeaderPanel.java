package com.surelogic.sierra.gwt.client;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SourcesTabEvents;
import com.google.gwt.user.client.ui.TabBar;
import com.google.gwt.user.client.ui.TabListener;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.surelogic.sierra.gwt.client.data.UserAccount;
import com.surelogic.sierra.gwt.client.service.ServiceHelper;
import com.surelogic.sierra.gwt.client.service.SessionServiceAsync;
import com.surelogic.sierra.gwt.client.util.ExceptionTracker;

public final class HeaderPanel extends Composite {
	private static final String PRIMARY_STYLE = "header";
	private static final String SESSION_STYLE = PRIMARY_STYLE + "-session";

	private final VerticalPanel rootPanel = new VerticalPanel();
	private final HorizontalPanel headerRow = new HorizontalPanel();
	private final HorizontalPanel sessionPanel = new HorizontalPanel();
	private final Label loggedInAs;
	private final TabBar mainBar = new TabBar();
	private final List tabContextNames = new ArrayList();

	public static HeaderPanel getInstance() {
		return (HeaderPanel) RootPanel.get("header-pane").getWidget(0);
	}

	public HeaderPanel() {
		super();
		initWidget(rootPanel);
		rootPanel.addStyleName(PRIMARY_STYLE);

		headerRow.addStyleName(PRIMARY_STYLE);
		final Image sierraLogo = new Image(GWT.getModuleBaseURL()
				+ "images/header-sierra-logo.gif");
		headerRow.add(sierraLogo);

		sessionPanel.addStyleName(SESSION_STYLE);
		loggedInAs = createUserLabel("Logged In", null);
		sessionPanel.add(loggedInAs);
		sessionPanel.add(createUserLabel("|", null));
		sessionPanel.add(createUserLabel("Preferences", new ClickListener() {

			public void onClick(Widget sender) {
				PreferencesContent.getInstance().show();
			}
		}));

		sessionPanel.add(createUserLabel("|", null));
		sessionPanel.add(createUserLabel("Log out", new ClickListener() {

			public void onClick(Widget sender) {
				final SessionServiceAsync svc = ServiceHelper
						.getSessionService();
				svc.logout(new AsyncCallback() {

					public void onFailure(Throwable caught) {
						ExceptionTracker.logException(caught);
						ClientContext.setUser(null);
						LoginContent.getInstance("Unable to contact server")
								.show();
					}

					public void onSuccess(Object result) {
						ClientContext.setUser(null);
						LoginContent.getInstance().show();
					}
				});
			}
		}));

		rootPanel.add(headerRow);

		mainBar.setWidth("100%");

		mainBar.addTabListener(new TabListener() {

			public boolean onBeforeTabSelected(SourcesTabEvents sender,
					int tabIndex) {
				return true;
			}

			public void onTabSelected(SourcesTabEvents sender, int tabIndex) {
				if (tabIndex < 0 || tabIndex >= tabContextNames.size()) {
					tabIndex = 0;
				}
				String contextName = (String) tabContextNames.get(tabIndex);
				ClientContext.setContext(contextName);
			}
		});

		// TODO set up mainBar tabs here

		// Listen for user session changes
		ClientContext.addChangeListener(new ClientContextListener() {

			public void onChange(UserAccount account, String context) {
				updateSession(account);
			}

		});
		updateSession(ClientContext.getUser());
	}

	public void updateSession(UserAccount user) {
		if (user == null) {
			if (headerRow.getWidgetIndex(sessionPanel) != -1) {
				headerRow.remove(sessionPanel);
			}

			if (rootPanel.getWidgetIndex(mainBar) != -1) {
				rootPanel.remove(mainBar);
			}
		} else {
			if (headerRow.getWidgetIndex(sessionPanel) == -1) {
				headerRow.add(sessionPanel);
				headerRow.setCellHorizontalAlignment(sessionPanel,
						HorizontalPanel.ALIGN_RIGHT);
			}
			loggedInAs.setText("Logged in as " + user.getUserName());

			if (rootPanel.getWidgetIndex(mainBar) == -1) {
				rootPanel.add(mainBar);
			}

			// TODO add welcome tab and normal user stuff

			if (user.isAdministrator()) {
				addTab("Server Settings", ServerSettingsContent.getInstance()
						.getContextName());
				addTab("User Management", UserManagementContent.getInstance()
						.getContextName());
			} else {
				removeTab(ServerSettingsContent.getInstance().getContextName());
				removeTab(UserManagementContent.getInstance().getContextName());
			}
		}

	}

	private Label createUserLabel(String text, ClickListener clickListener) {
		final Label lbl = new Label(text);
		lbl.addStyleName(SESSION_STYLE);
		if (clickListener != null) {
			lbl.addStyleName(SESSION_STYLE + "-clickable");
			lbl.addClickListener(clickListener);
		}
		return lbl;
	}

	private void addTab(String title, String contextName) {
		if (tabContextNames.indexOf(contextName) == -1) {
			tabContextNames.add(contextName);
			mainBar.addTab(title);
		}
	}

	private void removeTab(String contextName) {
		int tabIndex = tabContextNames.indexOf(contextName);
		if (tabIndex != -1) {
			tabContextNames.remove(tabIndex);
			mainBar.removeTab(tabIndex);
		}
	}
}
