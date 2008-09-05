package com.surelogic.sierra.gwt.client;

import java.util.ArrayList;
import java.util.List;

import com.surelogic.sierra.gwt.client.content.login.LoginContent;
import com.surelogic.sierra.gwt.client.data.UserAccount;
import com.surelogic.sierra.gwt.client.service.ServiceHelper;
import com.surelogic.sierra.gwt.client.service.SessionServiceAsync;
import com.surelogic.sierra.gwt.client.service.callback.ResultCallback;

public class SessionManager {
	private static UserAccount userAccount;
	private static final List<UserListener> userListeners = new ArrayList<UserListener>();

	public static UserAccount getUser() {
		return userAccount;
	}

	public static boolean isLoggedIn() {
		return userAccount != null;
	}

	public static void login(final String username, final String password) {
		final SessionServiceAsync sessionService = ServiceHelper
				.getSessionService();
		sessionService.login(username, password,
				new ResultCallback<UserAccount>() {

					@Override
					public void doFailure(final String message,
							final UserAccount result) {
						userAccount = null;
						for (final UserListener listener : userListeners) {
							listener.onLoginFailure(message);
						}
						ContextManager.refreshContext();
					}

					@Override
					public void doSuccess(final String message,
							final UserAccount result) {
						userAccount = result;
						for (final UserListener listener : userListeners) {
							listener.onLogin(userAccount);
						}
						ContextManager.refreshContext();
					}
				});
	}

	public static void updateUser(final UserAccount user) {
		userAccount = user;
		for (final UserListener listener : userListeners) {
			listener.onUpdate(userAccount);
		}
		ContextManager.refreshContext();
	}

	public static void logout(final String errorMessage) {
		final SessionServiceAsync svc = ServiceHelper.getSessionService();
		svc.logout(new ResultCallback<String>() {

			@Override
			protected void doException(final Throwable caught) {
				final UserAccount oldUser = userAccount;
				userAccount = null;
				for (final UserListener listener : userListeners) {
					listener.onLogout(oldUser, errorMessage);
				}
				// TODO should be heading to login page from parent code, test
				// Context.create(LoginContent.getInstance(), null).submit();
			}

			@Override
			protected void doFailure(String message, final String result) {
				if (errorMessage != null && !errorMessage.equals("")) {
					message += " (" + errorMessage + ")";
				}
				for (final UserListener listener : userListeners) {
					listener.onLogout(userAccount, message);
				}
				Context.create(LoginContent.getInstance(), null).submit();
			}

			@Override
			protected void doSuccess(final String message, final String result) {
				final UserAccount oldUser = userAccount;
				userAccount = null;
				for (final UserListener listener : userListeners) {
					listener.onLogout(oldUser, errorMessage);
				}
				Context.create(LoginContent.getInstance(), null).submit();
			}

		});
	}

	public static void addUserListener(final UserListener listener) {
		userListeners.add(listener);
	}

	public static void removeUserListener(final UserListener listener) {
		userListeners.remove(listener);
	}

	private SessionManager() {
		// not instantiable
	}
}
