package com.surelogic.sierra.gwt.client;

import com.surelogic.sierra.gwt.client.data.UserAccount;

/**
 * Listens for changes in the web client session (i.e. login, logout).
 * 
 */
public interface SessionListener {

	/**
	 * Called when the user has successfully authenticated.
	 * 
	 * @param user
	 *            the user's account information
	 */
	void onLogin(UserAccount user);

	/**
	 * Called when a user login attempt has failed.
	 * 
	 * @param message
	 *            the error message
	 */
	void onLoginFailure(String message);

	/**
	 * Called when the user account information is updated manually.
	 * 
	 * @param user
	 *            the new user account information
	 */
	void onUpdate(UserAccount user);

	/**
	 * Called when the user logs out, with an optional message to show when they
	 * are redirected to the login page.
	 * 
	 * @param user
	 *            the user's account information before logout
	 * @param errorMessage
	 *            an optional error message to display
	 */
	void onLogout(UserAccount user, String errorMessage);

}
