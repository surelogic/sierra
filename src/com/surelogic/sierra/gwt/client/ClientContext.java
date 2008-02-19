package com.surelogic.sierra.gwt.client;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.gwt.user.client.History;
import com.google.gwt.user.client.HistoryListener;
import com.surelogic.sierra.gwt.client.data.UserAccount;

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

	public static void setContext(String token) {
		context = token;
		History.newItem(context);
		notifyListeners();
	}

}
