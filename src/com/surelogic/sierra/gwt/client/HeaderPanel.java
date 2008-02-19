package com.surelogic.sierra.gwt.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
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
	private final HorizontalPanel sessionPanel = new HorizontalPanel();

	public static HeaderPanel getInstance() {
		return (HeaderPanel) RootPanel.get("header-pane").getWidget(0);
	}

	public HeaderPanel() {
		super();
		initWidget(rootPanel);
		rootPanel.addStyleName(PRIMARY_STYLE);

		final HorizontalPanel headerRow = new HorizontalPanel();
		headerRow.addStyleName(PRIMARY_STYLE);
		final Image sierraLogo = new Image(GWT.getModuleBaseURL()
				+ "images/header-sierra-logo.gif");
		headerRow.add(sierraLogo);

		sessionPanel.addStyleName(SESSION_STYLE);
		headerRow.add(sessionPanel);
		headerRow.setCellHorizontalAlignment(sessionPanel,
				HorizontalPanel.ALIGN_RIGHT);

		rootPanel.add(headerRow);

		// Listen for user session changes
		ClientContext.addChangeListener(new ClientContextListener() {

			public void onChange(UserAccount account, String context) {
				updateSession(account);
			}

		});
		updateSession(ClientContext.getUser());
	}

	public void updateSession(UserAccount user) {
		sessionPanel.clear();
		if (user != null) {
			sessionPanel.add(createUserLabel("Logged in as "
					+ user.getUserName(), null));
			sessionPanel.add(createUserLabel("|", null));
			sessionPanel.add(createUserLabel("Preferences",
					new ClickListener() {

						public void onClick(Widget sender) {
							PrefsContent.getInstance().show();
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

}
