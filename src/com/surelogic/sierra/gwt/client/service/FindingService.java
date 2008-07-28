package com.surelogic.sierra.gwt.client.service;

import java.util.List;
import java.util.UUID;

import com.google.gwt.user.client.rpc.RemoteService;
import com.surelogic.sierra.gwt.client.data.FindingOverview;
import com.surelogic.sierra.gwt.client.data.ImportanceView;
import com.surelogic.sierra.gwt.client.data.Result;
import com.surelogic.sierra.gwt.client.data.Scan;
import com.surelogic.sierra.gwt.client.data.ScanDetail;

public interface FindingService extends RemoteService {

	/**
	 * Return an overview of the given finding, which may be referred to by it's
	 * {@link UUID} identifier, or by it's local id.
	 * 
	 * @param id
	 *            a UUID in the form {@code
	 *            xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx} or a {@code long} value
	 * @return
	 */
	FindingOverview getFinding(final String id);

	/**
	 * Comment on a particular finding.
	 * 
	 * @param id
	 *            the local id
	 * @param comment
	 * @return
	 */
	Result<FindingOverview> comment(long id, String comment);

	/**
	 * Change the importance of a particular finding
	 * 
	 * @param id
	 *            the local id
	 * @param view
	 * @return
	 */
	Result<FindingOverview> changeImportance(final long id,
			final ImportanceView view);

	/**
	 * Get a list of the scans for a project in order from newest to oldest
	 * 
	 * @param project
	 * @return
	 */
	List<Scan> getScans(String project);

	/**
	 * Return details about a given scan
	 * 
	 * @param uuid
	 * @return
	 */
	ScanDetail getScanDetail(String uuid);

	/**
	 * Return details about the latest scan of a project
	 * 
	 * @param project
	 * @return
	 */
	ScanDetail getLatestScanDetail(String project);
}
