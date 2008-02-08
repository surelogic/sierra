package com.surelogic.sierra.gwt.client.service;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface ManagePrefsServiceAsync {

	void changePassword(String oldPassword, String newPassword,
			AsyncCallback callback);

}
