package com.surelogic.sierra.gwt.client.service;

import com.google.gwt.user.client.rpc.RemoteService;

public interface ManagePrefsService extends RemoteService {

	boolean changePassword(String oldPassword, String newPassword);

}
