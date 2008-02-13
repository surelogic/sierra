package com.surelogic.sierra.gwt.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.HistoryListener;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.RootPanel;
import com.surelogic.sierra.gwt.client.data.UserAccount;
import com.surelogic.sierra.gwt.client.service.ServiceHelper;
import com.surelogic.sierra.gwt.client.service.SessionServiceAsync;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class SierraPortal implements EntryPoint, HistoryListener {
	private final HeaderPanel headerPanel = new HeaderPanel();
	private final ContentPanel contentPanel = new ContentPanel();

	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		RootPanel.get("header-pane").add(headerPanel);
		RootPanel.get("content-pane").add(contentPanel);

		// see if the user has an established session, or needs to log in
		SessionServiceAsync sessionService = ServiceHelper.getSessionService();
		sessionService.getUserAccount(new AsyncCallback() {
			public void onSuccess(Object result) {
				ClientContext.setUser((UserAccount) result);
				if (result == null) {
					contentPanel.showLogin(null);
				} else {
					contentPanel.showDefault();
				}
			}

			public void onFailure(Throwable caught) {
				ExceptionTracker.logException(caught);
				headerPanel.updateAccountPanel(null);
				contentPanel.showLogin("Unable to verify session: "
						+ caught.getMessage());
			}
		});
	}

	public void onHistoryChanged(String historyToken) {
		// TODO Auto-generated method stub

	}
}
