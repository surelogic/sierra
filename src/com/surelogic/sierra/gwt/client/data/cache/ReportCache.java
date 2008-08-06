package com.surelogic.sierra.gwt.client.data.cache;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.surelogic.sierra.gwt.client.data.Report;
import com.surelogic.sierra.gwt.client.data.Status;
import com.surelogic.sierra.gwt.client.data.Report.DataSource;
import com.surelogic.sierra.gwt.client.data.Report.OutputType;
import com.surelogic.sierra.gwt.client.data.Report.Parameter;
import com.surelogic.sierra.gwt.client.data.Report.Parameter.Type;

public class ReportCache extends Cache<Report> {
	private static final ReportCache instance = new ReportCache();

	public static ReportCache getInstance() {
		return instance;
	}

	private ReportCache() {
		// singleton
	}

	@Override
	protected void doRefreshCall(final AsyncCallback<List<Report>> callback) {
		// TODO load real reports
		final List<Report> tempReports = new ArrayList<Report>();
		tempReports.add(findingsByProject());
		tempReports.add(findingsByPriority());
		callback.onSuccess(tempReports);
	}

	private Report findingsByProject() {
		final Report report = new Report();
		report.setUuid("TS-findingsByProject");
		report.setName("FindingsByProject");
		report.setTitle("Findings By Project");
		report.setDescription("Displays the number of findings per project.");
		report.setDataSource(DataSource.TEAMSERVER);
		report.setOutputTypes(OutputType.TABLE, OutputType.CHART);
		final List<Parameter> params = report.getParameters();
		params.add(new Parameter("Projects", Type.PROJECTS));
		params.add(new Parameter("Importance", Type.IMPORTANCE));
		return report;
	}

	private Report findingsByPriority() {
		final Report report = new Report();
		report.setUuid("BS-findingsByPriority");
		report.setName("FindingsByPriority");
		report.setTitle("Findings By Priority");
		report.setDescription("Just a placeholder report. Not implemented.");
		report.setDataSource(DataSource.BUGLINK);
		report.setOutputTypes(OutputType.TABLE, OutputType.CHART,
				OutputType.PDF);
		final List<Parameter> params = report.getParameters();
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
