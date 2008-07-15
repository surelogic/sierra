package com.surelogic.sierra.gwt.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.RootPanel;
import com.surelogic.sierra.gwt.client.content.ContentPanel;
import com.surelogic.sierra.gwt.client.content.ContentRegistry;
import com.surelogic.sierra.gwt.client.data.UserAccount;
import com.surelogic.sierra.gwt.client.header.HeaderPanel;
import com.surelogic.sierra.gwt.client.service.Callback;
import com.surelogic.sierra.gwt.client.service.ServiceHelper;
import com.surelogic.sierra.gwt.client.service.SessionServiceAsync;

/**
 * The Sierra Portal GWT Entry point
 */
public class SierraPortal implements EntryPoint {

	/**
	 * This method is called the first time a user enters the site. The header
	 * and content panels are created, and a check is done to see if the user
	 * has a valid session already established.
	 */
	public void onModuleLoad() {
		ContextManager.initialize();
		ContentRegistry.initialize();

		// create and display the main panels
		final HeaderPanel header = new HeaderPanel();
		final ContentPanel content = new ContentPanel();
		final LogPanel log = new LogPanel();
		RootPanel.get("header-pane").add(header);
		RootPanel.get("content-pane").add(content);
		RootPanel.get("log-pane").add(log);

		// see if the user has an established session, or needs to log in
		SessionServiceAsync sessionService = ServiceHelper.getSessionService();
		sessionService.getUserAccount(new Callback<UserAccount>() {

			protected void onFailure(String message, UserAccount result) {
				ContextManager.refreshContext();
			}

			protected void onSuccess(String message, UserAccount result) {
				ContextManager.updateUser(result);
			}
		});

	}

}
