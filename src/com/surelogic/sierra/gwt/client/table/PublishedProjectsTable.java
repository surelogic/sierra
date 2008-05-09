package com.surelogic.sierra.gwt.client.table;

import java.util.Iterator;
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

	protected String[] getHeaderTitles() {
		return new String[] { "Project", "Last Scan", "Audits", "Critical",
				"High", "Medium", "Low", "Irrelevant", "Last Audit", "By" };
	}

	protected String[] getHeaderDataTypes() {
		return new String[] { "cell-text", "cell-date", "cell-text",
				"cell-number", "cell-number", "cell-number", "cell-number",
				"cell-number", "cell-date", "cell-text" };
	}

	protected void updateTable(Context context) {
		clearRows();
		setWaitStatus();

		ServiceHelper.getOverviewService().getProjectOverviews(
				new AsyncCallback() {

					public void onFailure(Throwable caught) {
						ExceptionUtil.log(caught);
						setErrorStatus("Unable to retrieve project overviews. (Server may be down)");
					}

					public void onSuccess(Object result) {
						clearStatus();

						List list = (List) result;
						if (list.isEmpty()) {
							setSuccessStatus("No project scans have been published to this server.");
						} else {
							for (Iterator rows = list.iterator(); rows
									.hasNext();) {
								ProjectOverview po = (ProjectOverview) rows
										.next();
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
