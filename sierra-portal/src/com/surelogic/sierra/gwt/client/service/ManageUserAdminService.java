package com.surelogic.sierra.gwt.client.service;

import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.surelogic.sierra.gwt.client.data.Result;
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
	 * @return a list of user names
	 */
	List<UserAccount> getUsers();

	/**
	 * @return a list of user names
	 */
	List<UserAccount> findUser(String userQueryString);

	/**
	 * Create a new user with the given password.
	 * 
	 * @param account
	 * @return A user-readable {@link Result} message indicating whether or not
	 *         creation succeeded.
	 */
	Result<String> createUser(UserAccount account, String password);

	/**
	 * Get details about a user's information
	 * 
	 * @param userName
	 * @return
	 */
	UserAccount getUserInfo(String userName);

	/**
	 * Change the target user's password. If the target user is someone other
	 * than the user in context, then the user in context must be an
	 * administrator.
	 * 
	 * @param targetUser
	 * @param currentUserPassword
	 *            the password of the user in context
	 * @param newPassword
	 * @return a {@link Result} indicating whether the password change was
	 *         successful or not
	 */
	Result<String> changeUserPassword(String targetUser,
			String currentUserPassword, String newPassword);

	/**
	 * 
	 * @param account
	 *            the user's updated account info
	 * @return
	 */
	Result<UserAccount> updateUser(UserAccount account);

	/**
	 * Update the status of a list of users. User passwords cannot currently be
	 * updated in a batch.
	 * 
	 * @param account
	 *            the user's updated account info
	 * @return
	 */
	void updateUsersStatus(List<String> users, boolean status);

}
