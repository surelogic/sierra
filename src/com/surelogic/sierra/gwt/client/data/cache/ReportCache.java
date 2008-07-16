package com.surelogic.sierra.gwt.client.data.cache;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.surelogic.sierra.gwt.client.data.Report;
import com.surelogic.sierra.gwt.client.data.Status;
import com.surelogic.sierra.gwt.client.data.Report.Parameter;
import com.surelogic.sierra.gwt.client.data.Report.Parameter.Type;

public class ReportCache extends Cache<Report> {

	@Override
	protected void doRefreshCall(final AsyncCallback<List<Report>> callback) {
		// TODO load real reports
		final List<Report> tempReports = new ArrayList<Report>();
		tempReports.add(findingsByProject());

		callback.onSuccess(tempReports);
	}

	private Report findingsByProject() {
		final Report report = new Report();
		report.setUuid("Temp1");
		report.setName("FindingsByProject");
		report.setTitle("Findings By Project");
		report.setDescription("Displays the number of findings per project.");
		final List<Parameter> params = report.getParameters();
		params.add(new Parameter("Projects", Type.PROJECTS));
		params.add(new Parameter("Importance", Type.IMPORTANCE));
		return report;
	}

	@Override
	protected void doSaveCall(final Report item,
			final AsyncCallback<Status> callback) {
		callback.onFailure(new Exception("Not implemented"));

		// TODO no save implemented
	}

}
