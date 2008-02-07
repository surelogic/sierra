package com.surelogic.sierra.gwt.client;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.surelogic.sierra.gwt.client.data.UserAccount;

public final class ClientContext {

	private static UserAccount userAccount;
	private static List listeners = new ArrayList();

	public static UserAccount getUser() {
		return userAccount;
	}

	public static void setUser(UserAccount user) {
		userAccount = user;
		for (Iterator i = listeners.iterator(); i.hasNext();) {
			final UserAccountListener listener = (UserAccountListener) i.next();
			listener.onChange(userAccount);
		}
	}

	public static void addChangeListener(UserAccountListener listener) {
		listeners.add(listener);
	}

	public static void removeChangeListener(UserAccountListener listener) {
		listeners.remove(listener);
	}

}
