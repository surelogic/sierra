package com.surelogic.sierra.gwt.client.service;

import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.surelogic.sierra.gwt.client.data.UserAccount;

public interface ManageUserAdminService extends RemoteService {
	/**
	 * Returns whether or not the current user has rights to this service.
	 * 
	 * @return <code>true</code> if the service is available to this user,
	 *         false otherwise
	 */
	boolean isAvailable();

	/**
	 * @gwt.typeArgs <com.surelogic.sierra.gwt.client.data.UserAccount>
	 * @return a list of user names
	 */
	List getUsers();

	/**
	 * @gwt.typeArgs <com.surelogic.sierra.gwt.client.data.UserAccount>
	 * @return a list of user names
	 */
	List findUser(String userQueryString);

	/**
	 * Create a new user with the given password.
	 * 
	 * @param account
	 * @return A user-readable status message indicating whether or not creation
	 *         succeeded.
	 */
	String createUser(UserAccount account, String password);

	/**
	 * Get details about a user's information
	 * 
	 * @param userName
	 * @return
	 */
	UserAccount getUserInfo(String userName);

	/**
	 * 
	 * @param account
	 *            the user's updated account info
	 * @param password
	 *            a new user password, or null if the password should not be
	 *            updated
	 * @return
	 */
	UserAccount updateUser(UserAccount account, String password);

	/**
	 * Delete and existing user.
	 * 
	 * @param user
	 * @return true if the user was successfully deleted
	 */
	boolean deleteUser(String user);
}
