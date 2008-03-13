package com.surelogic.sierra.gwt.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.RootPanel;
import com.surelogic.sierra.gwt.client.data.UserAccount;
import com.surelogic.sierra.gwt.client.service.ServiceHelper;
import com.surelogic.sierra.gwt.client.service.SessionServiceAsync;
import com.surelogic.sierra.gwt.client.util.ExceptionTracker;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class SierraPortal implements EntryPoint {
	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		ClientContext.initialize();

		final HeaderPanel header = new HeaderPanel();
		final ContentPanel content = new ContentPanel();
		RootPanel.get("header-pane").add(header);
		RootPanel.get("content-pane").add(content);

		// see if the user has an established session, or needs to log in
		SessionServiceAsync sessionService = ServiceHelper.getSessionService();
		sessionService.getUserAccount(new AsyncCallback() {
			public void onSuccess(Object result) {
				ClientContext.setUser((UserAccount) result);
			}

			public void onFailure(Throwable caught) {
				ExceptionTracker.logException(caught);
				ClientContext.setUser(null);
				LoginContent.getInstance(
						"Unable to verify session. (Server may be down)")
						.show();
			}
		});
	}

}
