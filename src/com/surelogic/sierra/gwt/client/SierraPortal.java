package com.surelogic.sierra.gwt.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class SierraPortal implements EntryPoint {
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
		sessionService.isValidSession(new AsyncCallback() {
			public void onSuccess(Object result) {
				if (Boolean.TRUE.equals(result)) {
					// TODO update header panel to show Preferences, Logout, etc
					contentPanel.showDefault();
				} else {
					contentPanel.showLogin(null);
				}
			}

			public void onFailure(Throwable caught) {
				ExceptionTracker.logException(caught);
				
				contentPanel.showLogin("Unable to verify session: "
						+ caught.getMessage());
			}
		});
	}
}
