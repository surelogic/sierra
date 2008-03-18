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
	private static List userListeners = new ArrayList();
	private static List contextListeners = new ArrayList();

	private ClientContext() {
		// Not instantiable
	}

	public static void initialize() {
		History.addHistoryListener(new ContextHistoryListener());
	}

	public static UserAccount getUser() {
		return userAccount;
	}

	public static boolean isLoggedIn() {
		return userAccount != null;
	}

	public static void login(String username, String password,
			final UserListener callback) {
		SessionServiceAsync sessionService = ServiceHelper.getSessionService();
		sessionService.login(username, password, new Callback() {

			public void onFailure(String message, Object result) {
				userAccount = null;
				for (Iterator i = userListeners.iterator(); i.hasNext();) {
					((UserListener) i.next()).onLoginFailure(message);
				}
				if (callback != null) {
					callback.onLoginFailure(message);
				}
				refreshContext();
			}

			public void onSuccess(String message, Object result) {
				userAccount = (UserAccount) result;
				for (Iterator i = userListeners.iterator(); i.hasNext();) {
					((UserListener) i.next()).onLogin(userAccount);
				}
				if (callback != null) {
					callback.onLogin(userAccount);
				}
				refreshContext();
			}
		});
	}

	public static void updateUser(UserAccount user) {
		userAccount = user;
		for (Iterator i = userListeners.iterator(); i.hasNext();) {
			((UserListener) i.next()).onUpdate(userAccount);
		}
		refreshContext();
	}

	public static void logout(final String errorMessage) {
		final SessionServiceAsync svc = ServiceHelper.getSessionService();
		svc.logout(new Callback() {

			protected void onFailure(String message, Object result) {
				if (errorMessage != null && !errorMessage.equals("")) {
					message += " (" + errorMessage + ")";
				}
				for (Iterator i = userListeners.iterator(); i.hasNext();) {
					((UserListener) i.next()).onLogout(userAccount, message);
				}
				refreshContext();
			}

			protected void onSuccess(String message, Object result) {
				final UserAccount oldUser = userAccount;
				userAccount = null;
				for (Iterator i = userListeners.iterator(); i.hasNext();) {
					((UserListener) i.next()).onLogout(oldUser, errorMessage);
				}
				refreshContext();
			}

		});
	}

	public static void addUserListener(UserListener listener) {
		userListeners.add(listener);
	}

	public static void removeUserListener(UserListener listener) {
		userListeners.remove(listener);
	}

	public static boolean isContent(String token) {
		return LangUtil.equals(token, getContext().getContent());
	}

	public static boolean isContent(ContentComposite content) {
		// TODO need to add support for subcontext
		return LangUtil.equals(content.getContextName(), getContext()
				.getContent());
	}

	public static Context getContext() {
		return Context.create(History.getToken());
	}

	public static void setContext(String token) {
		// Note: newItem calls onHistoryChanged, which calls
		// notifyContextListeners only if the token changes
		if (!isContent(token)) {
			History.newItem(token);
		} else {
			notifyContextListeners();
		}
	}

	public static void refreshContext() {
		notifyContextListeners();
	}

	public static void addContextListener(ContextListener listener) {
		contextListeners.add(listener);
	}

	public static void removeContextListener(ContextListener listener) {
		contextListeners.remove(listener);
	}

	private static void notifyContextListeners() {
		final Context context = getContext();
		for (Iterator i = contextListeners.iterator(); i.hasNext();) {
			final ContextListener listener = (ContextListener) i.next();
			listener.onChange(context);
		}
	}

	private static class ContextHistoryListener implements HistoryListener {

		public void onHistoryChanged(String historyToken) {
			notifyContextListeners();
		}

	}

}
