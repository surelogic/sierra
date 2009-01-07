package com.surelogic.sierra.gwt.client;

import java.util.ArrayList;
import java.util.List;

import com.surelogic.sierra.gwt.client.content.login.LoginContent;
import com.surelogic.sierra.gwt.client.data.UserAccount;
import com.surelogic.sierra.gwt.client.service.ServiceHelper;
import com.surelogic.sierra.gwt.client.service.SessionServiceAsync;
import com.surelogic.sierra.gwt.client.service.callback.ResultCallback;

/**
 * This singleton provides login and logout functionality, along with access to
 * the current user. It also manages a list of session listeners, so
 * notifications can be received when the session state is changed.
 * 
 * @see UserAccount
 * @see SessionListener
 */
public final class SessionManager {
	/**
	 * The current user's account information.
	 */
	private static UserAccount userAccount;

	/**
	 * A list of listeners that will be notified when the session state changes.
	 */
	private static final List<SessionListener> sessionListeners = new ArrayList<SessionListener>();

	/**
	 * Returns the user information for the current session. This method can
	 * will return null if a login has not been attempted, or has failed.
	 * 
	 * @return the current user information, or null
	 */
	public static UserAccount getUser() {
		return userAccount;
	}

	/**
	 * Returns true if a successful login operation has occurred.
	 * 
	 * @return true if the user is logged in.
	 * @see #login(String, String)
	 */
	public static boolean isLoggedIn() {
		return userAccount != null;
	}

	/**
	 * Authenticates the given user and password with the server. When a result
	 * is received, the session listeners are notified and the web site context
	 * is refreshed.
	 * 
	 * @param username
	 *            the user to authenticate
	 * @param password
	 *            the user's password to verify
	 * @see SessionListener
	 * @see ContextManager#refreshContext()
	 */
	public static void login(final String username, final String password) {
		// make the login RPC call
		final SessionServiceAsync sessionService = ServiceHelper
				.getSessionService();
		sessionService.login(username, password,
				new ResultCallback<UserAccount>() {

					@Override
					public void doFailure(final String message,
							final UserAccount result) {
						// login failed
						userAccount = null;
						for (final SessionListener listener : sessionListeners) {
							listener.onLoginFailure(message);
						}
						ContextManager.refreshContext();
					}

					@Override
					public void doSuccess(final String message,
							final UserAccount result) {
						// login succeeded
						userAccount = result;
						for (final SessionListener listener : sessionListeners) {
							listener.onLogin(userAccount);
						}
						ContextManager.refreshContext();
					}
				});
	}

	/**
	 * Updates the user account manually. This is mainly used when an already
	 * authenticated user opens a new browser window.
	 * 
	 * @param user
	 *            the user's account information
	 */
	public static void updateUser(final UserAccount user) {
		userAccount = user;
		for (final SessionListener listener : sessionListeners) {
			listener.onUpdate(userAccount);
		}
		ContextManager.refreshContext();
	}

	/**
	 * Ends the current user session. This method attempts to notify the server
	 * of the logout, and will refresh the web site context when a result is
	 * received or the server can not be contacted.
	 * 
	 * @param errorMessage
	 *            the optional error message to show on the login web page
	 */
	public static void logout(final String errorMessage) {
		// make the logout RPC call
		final SessionServiceAsync svc = ServiceHelper.getSessionService();
		svc.logout(new ResultCallback<String>() {

			@Override
			protected void doException(final Throwable caught) {
				// an exception occurred, just clear out the user info and
				// notify listeners
				final UserAccount oldUser = userAccount;
				userAccount = null;
				for (final SessionListener listener : sessionListeners) {
					listener.onLogout(oldUser, errorMessage);
				}
				// TODO should be heading to login page from parent code, test
				// Context.create(LoginContent.getInstance(), null).submit();
			}

			@Override
			protected void doFailure(String message, final String result) {
				// the logout operation failed? This really shouldn't happen.
				if (errorMessage != null && !errorMessage.equals("")) {
					message += " (" + errorMessage + ")";
				}
				for (final SessionListener listener : sessionListeners) {
					listener.onLogout(userAccount, message);
				}
				new Context(LoginContent.getInstance()).submit();
			}

			@Override
			protected void doSuccess(final String message, final String result) {
				// logout was successful, notify listeners
				final UserAccount oldUser = userAccount;
				userAccount = null;
				for (final SessionListener listener : sessionListeners) {
					listener.onLogout(oldUser, errorMessage);
				}
				new Context(LoginContent.getInstance()).submit();
			}

		});
	}

	/**
	 * Registers a session listener to receive session change notifications.
	 * 
	 * @param listener
	 *            the listener to notify of session changes.
	 * @see #removeSessionListener(SessionListener)
	 */
	public static void addSessionListener(final SessionListener listener) {
		sessionListeners.add(listener);
	}

	/**
	 * Removes a session listener so it will no longer be notified of session
	 * changes.
	 * 
	 * @param listener
	 *            the listener to remove
	 * @see #addSessionListener(SessionListener)
	 */
	public static void removeSessionListener(final SessionListener listener) {
		sessionListeners.remove(listener);
	}

	private SessionManager() {
		// not instantiable
	}
}
