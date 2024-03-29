package com.surelogic.sierra.gwt.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.RootPanel;
import com.surelogic.sierra.gwt.client.content.ContentPane;
import com.surelogic.sierra.gwt.client.content.ContentRegistry;
import com.surelogic.sierra.gwt.client.data.UserAccount;
import com.surelogic.sierra.gwt.client.header.HeaderPane;
import com.surelogic.sierra.gwt.client.service.ServiceHelper;
import com.surelogic.sierra.gwt.client.service.SessionServiceAsync;
import com.surelogic.sierra.gwt.client.service.callback.ResultCallback;

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
		final HeaderPane header = new HeaderPane();
		final ContentPane content = new ContentPane();
		final LogPane log = new LogPane();
		RootPanel.get("header-pane").add(header);
		RootPanel.get("content-pane").add(content);
		RootPanel.get("log-pane").add(log);

		// see if the user has an established session, or needs to log in
		final SessionServiceAsync sessionService = ServiceHelper
				.getSessionService();
		sessionService.getUserAccount(new ResultCallback<UserAccount>() {

			@Override
			protected void doFailure(final String message,
					final UserAccount result) {
				ContextManager.refreshContext();
			}

			@Override
			protected void doSuccess(final String message,
					final UserAccount result) {
				SessionManager.updateUser(result);
			}
		});

	}

}
