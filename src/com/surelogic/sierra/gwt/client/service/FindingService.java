package com.surelogic.sierra.gwt.client.service;

import com.google.gwt.user.client.rpc.RemoteService;
import com.surelogic.sierra.gwt.client.data.FindingOverview;

public interface FindingService extends RemoteService {

	FindingOverview getFinding(String id);
	
}
