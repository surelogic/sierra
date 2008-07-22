package com.surelogic.sierra.gwt.client.service;

import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.surelogic.sierra.gwt.client.data.FindingOverview;
import com.surelogic.sierra.gwt.client.data.ImportanceView;
import com.surelogic.sierra.gwt.client.data.Result;
import com.surelogic.sierra.gwt.client.data.Scan;
import com.surelogic.sierra.gwt.client.data.ScanDetail;

public interface FindingService extends RemoteService {

	Result<FindingOverview> getFinding(String id);

	Result<FindingOverview> comment(long id, String comment);

	Result<FindingOverview> changeImportance(long id, ImportanceView view);

	List<Scan> getScans(String project);

	ScanDetail getScanDetail(String uuid);
}
