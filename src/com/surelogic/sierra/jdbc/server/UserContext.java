package com.surelogic.sierra.jdbc.server;

import com.surelogic.sierra.jdbc.user.User;

/**
 * UserContext holds the user currently in context. UserContext is valid only in
 * the server, and only in the thread that was invoked to handle the user's
 * request.
 * 
 * @author nathan
 * 
 */
public class UserContext {

	private static final ThreadLocal<User> local = new ThreadLocal<User>();

	public static User peek() {
		return local.get();
	}

	public static void set(User user) {
		local.set(user);
	}

	/**
	 * Remove the user from the current context.
	 * 
	 * @return
	 */
	public static User remove() {
		User user = local.get();
		local.set(null);
		return user;
	}

}
