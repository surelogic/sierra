package com.surelogic.sierra.gwt.client.service;

import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.surelogic.sierra.gwt.client.data.Result;
import com.surelogic.sierra.gwt.client.data.Status;
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
	Status createUser(UserAccount account, String password);

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
	Result changeUserPassword(String targetUser, String currentUserPassword,
			String newPassword);

	/**
	 * 
	 * @param account
	 *            the user's updated account info
	 * @return
	 */
	Result updateUser(UserAccount account);

	/**
	 * Update the status of a list of users. User passwords cannot currently be
	 * updated in a batch.
	 * 
	 * @gwt.typeArgs users <java.lang.String>
	 * 
	 * @param account
	 *            the user's updated account info
	 * @return
	 */
	void updateUsersStatus(List users, boolean status);

}
