package com.surelogic.sierra.gwt.client.service;

import com.google.gwt.user.client.rpc.RemoteService;
import com.surelogic.sierra.gwt.client.data.FindingOverview;
import com.surelogic.sierra.gwt.client.data.Result;

public interface FindingService extends RemoteService {

	Result<FindingOverview> getFinding(String id);

}
