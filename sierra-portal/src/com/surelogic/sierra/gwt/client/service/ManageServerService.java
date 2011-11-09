package com.surelogic.sierra.gwt.client.service;

import com.google.gwt.user.client.rpc.RemoteService;
import com.surelogic.sierra.gwt.client.data.EmailInfo;
import com.surelogic.sierra.gwt.client.data.ServerInfo;
import com.surelogic.sierra.gwt.client.data.Status;

public interface ManageServerService extends RemoteService {

	ServerInfo getServerInfo();

	ServerInfo deploySchema();

	ServerInfo setSiteName(String name);

	ServerInfo setEmail(EmailInfo address);

	Status testAdminEmail();

}
