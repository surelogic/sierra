package com.surelogic.sierra.gwt.client;

import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;

public interface ManageUserService extends RemoteService {

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

}
