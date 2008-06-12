package com.surelogic.sierra.gwt.client.content.reports;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.surelogic.sierra.gwt.client.data.Cache;
import com.surelogic.sierra.gwt.client.data.Report;
import com.surelogic.sierra.gwt.client.data.Status;
import com.surelogic.sierra.gwt.client.data.Report.Parameter;
import com.surelogic.sierra.gwt.client.data.Report.Parameter.Type;

public class ReportCache extends Cache<Report> {

	@Override
	protected void doRefreshCall(AsyncCallback<List<Report>> callback) {
		// TODO load real reports
		List<Report> tempReports = new ArrayList<Report>();
		tempReports.add(findingsByProject());

		callback.onSuccess(tempReports);
	}

	private Report findingsByProject() {
		Report report = new Report();
		report.setUuid("Temp1");
		report.setName("FindingsByProject");
		report.setTitle("Findings By Project");
		report.setDescription("Displays the number of findings per project.");
		Set<Parameter> params = report.getParameters();
		params.add(new Parameter("Projects", Type.PROJECTS));
		params.add(new Parameter("Priority", Type.PRIORITY));
		return report;
	}

	@Override
	protected void doSaveCall(Report item, AsyncCallback<Status> callback) {
		callback.onFailure(new Exception("Not implemented"));

		// TODO no save implemented
	}

}
