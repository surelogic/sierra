package com.surelogic.sierra.gwt.client.table;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.surelogic.sierra.gwt.client.Context;
import com.surelogic.sierra.gwt.client.data.ProjectOverview;
import com.surelogic.sierra.gwt.client.service.ServiceHelper;
import com.surelogic.sierra.gwt.client.util.ExceptionUtil;

public class PublishedProjectsTable extends TableSection {

	public PublishedProjectsTable() {
		super();
		setTitle("Projects");
		setSummary("All Published Projects");
	}

	@Override
	protected String[] getHeaderTitles() {
		return new String[] { "Project", "Last Scan", "Audits", "Critical",
				"High", "Medium", "Low", "Irrelevant", "Last Audit", "By" };
	}

	@Override
	protected String[] getHeaderDataTypes() {
		return new String[] { "cell-text", "cell-date", "cell-text",
				"cell-number", "cell-number", "cell-number", "cell-number",
				"cell-number", "cell-date", "cell-text" };
	}

	@Override
	protected void updateTable(Context context) {
		clearRows();
		setWaitStatus();

		ServiceHelper.getOverviewService().getProjectOverviews(
				new AsyncCallback<List<ProjectOverview>>() {

					public void onFailure(Throwable caught) {
						ExceptionUtil.log(caught);
						setErrorStatus("Unable to retrieve project overviews. (Server may be down)");
					}

					public void onSuccess(List<ProjectOverview> result) {
						clearStatus();

						if (result.isEmpty()) {
							setSuccessStatus("No project scans have been published to this server.");
						} else {
							for (ProjectOverview po : result) {
								addRow();
								addColumn(po.getName());
								addColumn(po.getLastScanDate());
								final StringBuffer comments = new StringBuffer();
								if (po.getComments() > 0) {
									comments.append(po.getComments());
									comments.append(" on ");
									comments.append(po.getCommentedFindings());
									comments.append(" findings");
								}
								addColumn(comments.toString());
								addColumn(po.getCritical());
								addColumn(po.getHigh());
								addColumn(po.getMedium());
								addColumn(po.getLow());
								addColumn(po.getIrrelevant());
								addColumn(po.getLastSynchDate().length() == 0 ? ""
										: po.getLastSynchDate());
								addColumn(po.getLastSynchUser());
							}
						}
					}

				});
	}
}
