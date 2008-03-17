package com.surelogic.sierra.gwt.client;

import com.surelogic.sierra.gwt.client.data.UserAccount;

public interface UserListener {

	void onLogin(UserAccount user);

	void onLoginFailure(String message);

	void onUpdate(UserAccount user);

	void onLogout(UserAccount user, String errorMessage);

}
