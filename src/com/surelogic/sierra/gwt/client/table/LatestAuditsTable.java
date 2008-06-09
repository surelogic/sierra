package com.surelogic.sierra.gwt.client.table;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.surelogic.sierra.gwt.client.Context;
import com.surelogic.sierra.gwt.client.data.UserOverview;
import com.surelogic.sierra.gwt.client.service.ServiceHelper;
import com.surelogic.sierra.gwt.client.util.ExceptionUtil;

public class LatestAuditsTable extends TableSection {

	public LatestAuditsTable() {
		super();
		setTitle("Users");
		setSummary("Latest User Audits");
	}

	@Override
	protected String[] getHeaderTitles() {
		return new String[] { "User", "Audits", "Last Contribution" };
	}

	@Override
	protected String[] getHeaderDataTypes() {
		return new String[] { "cell-text", "cell-text", "cell-date" };
	}

	@Override
	protected void updateTable(Context context) {
		clearRows();
		setWaitStatus();

		ServiceHelper.getOverviewService().getUserOverviews(
				new AsyncCallback<List<UserOverview>>() {

					public void onFailure(Throwable caught) {
						ExceptionUtil.log(caught);
						setErrorStatus("Unable to retrieve user overviews. (Server may be down)");
					}

					public void onSuccess(List<UserOverview> result) {
						clearStatus();

						if (result.isEmpty()) {
							setSuccessStatus("No users defined for this server.");
						} else {
							for (UserOverview uo : result) {
								addRow();
								addColumn(uo.getUserName());
								StringBuffer audits = new StringBuffer();
								if (uo.getAudits() > 0) {
									audits.append(uo.getAudits());
									audits.append(" on ");
									audits.append(uo.getFindings());
									audits.append(" findings");
								}
								addColumn(audits.toString());
								addColumn(uo.getLastSynch().length() == 0 ? ""
										: uo.getLastSynch());
							}
						}
					}
				});
	}
}
