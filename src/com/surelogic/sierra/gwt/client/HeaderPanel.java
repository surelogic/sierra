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

// TODO using a TabBar mucks up the context change handling code
// Change the TabBar to our own setup so we can change the tab style without 
// triggering a TabSelected event or similar
public final class HeaderPanel extends Composite {
	private static final String PRIMARY_STYLE = "header";
	private static final String SESSION_STYLE = PRIMARY_STYLE + "-session";

	private final VerticalPanel rootPanel = new VerticalPanel();
	private final HorizontalPanel headerRow = new HorizontalPanel();
	private final HorizontalPanel sessionPanel = new HorizontalPanel();
	private final Label loggedInAs;
	private final Label userName;
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
				+ "images/surelogic.png");
		headerRow.add(sierraLogo);
		headerRow.setCellVerticalAlignment(sierraLogo,
				HorizontalPanel.ALIGN_MIDDLE);

		sessionPanel.addStyleName(SESSION_STYLE);
		loggedInAs = createUserLabel("Logged in to "
				+ GWT.getHostPageBaseURL().replaceFirst(".*//", "")
						.replaceFirst("/portal.*", "") + " as", null);
		userName = createUserLabel(null, null);
		userName.addStyleName("user");
		sessionPanel.add(loggedInAs);
		sessionPanel.add(userName);
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
					}
				});
			}
		}));

		rootPanel.add(headerRow);

		mainBar.setWidth("100%");
		addTab("Overview", OverviewContent.getInstance());
		addTab("User Management", UserManagementContent.getInstance());
		mainBar.addTabListener(new TabListener() {

			public boolean onBeforeTabSelected(SourcesTabEvents sender,
					int tabIndex) {
				return true;
			}

			public void onTabSelected(SourcesTabEvents sender, int tabIndex) {
				if (tabIndex >= 0 && tabIndex < tabContextNames.size()) {
					String contextName = (String) tabContextNames.get(tabIndex);
					if (!ClientContext.isContext(contextName)) {
						ClientContext.setContext(contextName);
					}
				}
			}
		});

		// TODO set up mainBar tabs here

		// Listen for user session changes
		ClientContext.addChangeListener(new ClientContextListener() {

			public void onChange(UserAccount account, String context) {
				updateContext(account, context);
			}

		});
		updateContext(ClientContext.getUser(), null);
	}

	public void updateContext(UserAccount user, String context) {
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
			loggedInAs.setText("Logged in to "
					+ GWT.getHostPageBaseURL().replaceFirst(".*//", "")
							.replaceFirst("/portal.*", "") + " as");
			userName.setText(user.getUserName());
			if (rootPanel.getWidgetIndex(mainBar) == -1) {
				rootPanel.add(mainBar);
			}

			if (user.isAdministrator()) {
				addTab("Settings", ServerInformationContent
						.getInstance());
			} else {
				removeTab(ServerInformationContent.getInstance()
						.getContextName());
			}

			final int tabIndex = mainBar.getSelectedTab();
			if (context != null) {
				int contextIndex = tabContextNames.indexOf(context);
				if (contextIndex != tabIndex) {
					mainBar.selectTab(contextIndex);
				}
			} else if (tabIndex != -1) {
				mainBar.selectTab(-1);
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

	private void addTab(String title, ContentComposite content) {
		final String contextName = content.getContextName();
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
