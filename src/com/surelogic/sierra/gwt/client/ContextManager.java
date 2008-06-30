package com.surelogic.sierra.gwt.client;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.History;
import com.google.gwt.user.client.HistoryListener;
import com.surelogic.sierra.gwt.client.content.login.LoginContent;
import com.surelogic.sierra.gwt.client.data.UserAccount;
import com.surelogic.sierra.gwt.client.service.Callback;
import com.surelogic.sierra.gwt.client.service.ServiceHelper;
import com.surelogic.sierra.gwt.client.service.SessionServiceAsync;

public final class ContextManager {

	private static UserAccount userAccount;
	private static List<UserListener> userListeners = new ArrayList<UserListener>();
	private static List<ContextListener> contextListeners = new ArrayList<ContextListener>();

	private ContextManager() {
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

	public static void login(String username, String password) {
		final SessionServiceAsync sessionService = ServiceHelper
				.getSessionService();
		sessionService.login(username, password, new Callback<UserAccount>() {

			@Override
			public void onFailure(String message, UserAccount result) {
				userAccount = null;
				for (final UserListener listener : userListeners) {
					listener.onLoginFailure(message);
				}
				refreshContext();
			}

			@Override
			public void onSuccess(String message, UserAccount result) {
				userAccount = result;
				for (final UserListener listener : userListeners) {
					listener.onLogin(userAccount);
				}
				refreshContext();
			}
		});
	}

	public static void updateUser(UserAccount user) {
		userAccount = user;
		for (final UserListener listener : userListeners) {
			listener.onUpdate(userAccount);
		}
		refreshContext();
	}

	public static void logout(final String errorMessage) {
		final SessionServiceAsync svc = ServiceHelper.getSessionService();
		svc.logout(new Callback<String>() {

			@Override
			protected void onException(Throwable caught) {
				final UserAccount oldUser = userAccount;
				userAccount = null;
				for (final UserListener listener : userListeners) {
					listener.onLogout(oldUser, errorMessage);
				}
				setContent(LoginContent.getInstance());
			}

			@Override
			protected void onFailure(String message, String result) {
				if (errorMessage != null && !errorMessage.equals("")) {
					message += " (" + errorMessage + ")";
				}
				for (final UserListener listener : userListeners) {
					listener.onLogout(userAccount, message);
				}
				setContent(LoginContent.getInstance());
			}

			@Override
			protected void onSuccess(String message, String result) {
				final UserAccount oldUser = userAccount;
				userAccount = null;
				for (final UserListener listener : userListeners) {
					listener.onLogout(oldUser, errorMessage);
				}
				setContent(LoginContent.getInstance());
			}

		});
	}

	public static void addUserListener(UserListener listener) {
		userListeners.add(listener);
	}

	public static void removeUserListener(UserListener listener) {
		userListeners.remove(listener);
	}

	public static boolean isContext(String context) {
		final Context ctx = Context.create(context);
		return ctx.equals(getContext());
	}

	public static boolean isContent(ContentComposite content) {
		return content == getContext().getContent();
	}

	public static Context getContext() {
		return Context.create(History.getToken());
	}

	public static void setContent(ContentComposite content) {
		setContext(ContentRegistry.getContentName(content));
	}

	public static void setContext(Context context) {
		setContext(context.toString());
	}

	public static void setContext(String token) {
		// Note: newItem calls onHistoryChanged, which calls
		// notifyContextListeners only if the token changes
		if (!isContext(token)) {
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
		for (final ContextListener listener : contextListeners) {
			listener.onChange(context);
		}
	}

	private static class ContextHistoryListener implements HistoryListener {

		public void onHistoryChanged(String historyToken) {
			notifyContextListeners();
		}

	}

}
