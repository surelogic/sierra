package com.surelogic.sierra.gwt.client;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.gwt.user.client.History;
import com.google.gwt.user.client.HistoryListener;
import com.surelogic.sierra.gwt.client.data.UserAccount;
import com.surelogic.sierra.gwt.client.service.Callback;
import com.surelogic.sierra.gwt.client.service.ServiceHelper;
import com.surelogic.sierra.gwt.client.service.SessionServiceAsync;
import com.surelogic.sierra.gwt.client.util.LangUtil;

public final class ClientContext {

	private static UserAccount userAccount;
	private static List listeners = new ArrayList();
	private static String context = History.getToken();

	private ClientContext() {
		// Not instantiable
	}

	public static void initialize() {
		History.addHistoryListener(new ContextHistoryListener());
	}

	public static UserAccount getUser() {
		return userAccount;
	}

	public static void setUser(UserAccount user) {
		userAccount = user;
		notifyListeners();
	}

	public static boolean isLoggedIn() {
		return userAccount != null;
	}

	public static void logout() {
		final SessionServiceAsync svc = ServiceHelper.getSessionService();
		svc.logout(new Callback() {

			protected void onFailure(String message, Object result) {
				invalidate(message);
			}

			protected void onSuccess(String message, Object result) {
				setUser(null);
			}

		});
	}

	/**
	 * Called when the session is no longer valid and the login page should be
	 * displayed with an error message.
	 * 
	 * @param errorMessage
	 *            the error message to display
	 */
	public static void invalidate(String errorMessage) {
		ClientContext.setUser(null);
		LoginContent.getInstance(errorMessage).show();
	}

	public static boolean isContext(String token) {
		return LangUtil.equals(token, context);
	}

	public static String getContext() {
		return context;
	}

	public static void setContext(String token) {
		context = token;
		History.newItem(context);
		notifyListeners();
	}

	public static void addChangeListener(ClientContextListener listener) {
		listeners.add(listener);
	}

	public static void removeChangeListener(ClientContextListener listener) {
		listeners.remove(listener);
	}

	private static void notifyListeners() {
		for (Iterator i = listeners.iterator(); i.hasNext();) {
			final ClientContextListener listener = (ClientContextListener) i
					.next();
			listener.onChange(userAccount, context);
		}
	}

	private static class ContextHistoryListener implements HistoryListener {

		public void onHistoryChanged(String historyToken) {
			context = historyToken;
			notifyListeners();
		}

	}

}
