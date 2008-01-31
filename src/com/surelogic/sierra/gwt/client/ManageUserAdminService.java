package com.surelogic.sierra.gwt.client;

import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;

public interface ManageUserAdminService extends RemoteService {
	/**
	 * Returns whether or not the current user has rights to this service.
	 * 
	 * @return <code>true</code> if the service is available to this user,
	 *         false otherwise
	 */
	boolean isAvailable();

	/**
	 * @gwt.typeArgs <java.lang.String>
	 * @return a list of user names
	 */
	List getUsers();

	/**
	 * @gwt.typeArgs <java.lang.String>
	 * @return a list of user names
	 */
	List findUser(String userQueryString);

	/**
	 * Create a new user.
	 * 
	 * @param user
	 * @param password
	 * @return A user-readable status message indicating whether or not creation
	 *         succeeded.
	 */
	String createUser(String user, String password);

	/**
	 * Get details about a user's information
	 * 
	 * @param user
	 * @return
	 */
	UserInfo getUserInfo(String user);

	/**
	 * 
	 * @param user
	 *            the user's name
	 * @param password
	 *            a new user password, or null if the password should not be
	 *            updated
	 * @param isAdmin
	 *            the user's admin status
	 * @return
	 */
	UserInfo updateUser(String user, String password, boolean isAdmin);

	/**
	 * Delete and existing user.
	 * 
	 * @param user
	 */
	void deleteUser(String user);
}
