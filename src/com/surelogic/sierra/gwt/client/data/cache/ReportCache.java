package com.surelogic.sierra.gwt.client.data.cache;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.surelogic.sierra.gwt.client.data.Report;
import com.surelogic.sierra.gwt.client.data.ReportSettings;
import com.surelogic.sierra.gwt.client.data.Status;
import com.surelogic.sierra.gwt.client.data.Report.DataSource;
import com.surelogic.sierra.gwt.client.data.Report.OutputType;
import com.surelogic.sierra.gwt.client.data.Report.Parameter;
import com.surelogic.sierra.gwt.client.data.Report.Parameter.Type;
import com.surelogic.sierra.gwt.client.service.ServiceHelper;
import com.surelogic.sierra.gwt.client.service.callback.StandardCallback;

public class ReportCache extends Cache<Report> {
	private static final ReportCache instance = new ReportCache();

	public static ReportCache getInstance() {
		return instance;
	}

	private ReportCache() {
		// singleton
	}

	public static List<Report> allReports() {
		final List<Report> reports = new ArrayList<Report>();
		reports.add(auditContributions());
		reports.add(latestScans());
		reports.add(userAudits());
		reports.add(publishedProjects());
		reports.add(categoryCounts());
		reports.add(findingsByProject());
		reports.add(findingsByPriority());
		reports.add(projectFindings());
		reports.add(projectCompilations());
		reports.add(scanImportances());
		reports.add(scanImportancesByCategory());
		reports.add(scanFindings());
		reports.add(scanFindingsByCategory());
		reports.add(findingTypeCounts());
		reports.add(compareProjectScans());
		return reports;
	}

	@Override
	protected void doRefreshCall(final AsyncCallback<List<Report>> callback) {
		ServiceHelper.getSettingsService().listReportSettings(
				new StandardCallback<List<ReportSettings>>() {
					@Override
					protected void doSuccess(final List<ReportSettings> result) {
						final List<Report> reports = allReports();
						for (final ReportSettings s : result) {
							for (final Report r : reports) {
								if (r.getUuid().equals(s.getReportUuid())) {
									r.getSavedReports().add(s);
								}
							}
						}
						callback.onSuccess(reports);
					}
				});
	}

	public static Report auditContributions() {
		final Report report = new Report();
		report.setUuid("AuditContributions");
		report.setTitle("Contributions");
		report.setDescription("In The Last 30 Days");
		report.setDataSource(DataSource.TEAMSERVER);
		report.setOutputTypes(OutputType.CHART);
		return report;
	}

	public static Report latestScans() {
		final Report report = new Report();
		report.setUuid("LatestScanResults");
		report.setTitle("Published Scans");
		report.setDescription("Latest Scan Results");
		report.setDataSource(DataSource.TEAMSERVER);
		report.setOutputTypes(OutputType.CHART, OutputType.TABLE);
		return report;
	}

	public static Report userAudits() {
		final Report report = new Report();
		report.setUuid("UserAudits");
		report.setTitle("Users");
		report.setDescription("Latest user audits");
		report.setDataSource(DataSource.TEAMSERVER);
		report.setOutputTypes(OutputType.TABLE);
		return report;
	}

	public static Report publishedProjects() {
		final Report report = new Report();
		report.setUuid("PublishedProjects");
		report.setTitle("All Published Projects");
		report.setDescription("All Published Projects");
		report.setDataSource(DataSource.TEAMSERVER);
		report.setOutputTypes(OutputType.TABLE);
		return report;
	}

	public static Report categoryCounts() {
		final Report report = new Report();
		report.setUuid("CategoryCounts");
		report.setTitle("Category Counts");
		report
				.setDescription("Number of findings in each project for this category.");
		report.setDataSource(DataSource.TEAMSERVER);
		report.setOutputTypes(OutputType.CHART);
		final List<Parameter> params = report.getParameters();
		params.add(new Parameter("uuid", Type.CATEGORY));
		return report;
	}

	public static Report findingsByProject() {
		final Report report = new Report();
		report.setUuid("FindingsByProject");
		report.setTitle("Findings By Project");
		report.setDescription("Displays the number of findings per project.");
		report.setDataSource(DataSource.TEAMSERVER);
		report.setOutputTypes(OutputType.TABLE, OutputType.CHART);
		final List<Parameter> params = report.getParameters();
		params.add(new Parameter("Projects", Type.PROJECTS));
		params.add(new Parameter("Importance", Type.IMPORTANCE));
		return report;
	}

	public static Report findingsByPriority() {
		final Report report = new Report();
		report.setUuid("FindingsByPriority");
		report.setTitle("Findings By Priority");
		report.setDescription("Just a placeholder report. Not implemented.");
		report.setDataSource(DataSource.BUGLINK);
		report.setOutputTypes(OutputType.TABLE, OutputType.CHART,
				OutputType.PDF);
		final List<Parameter> params = report.getParameters();
		params.add(new Parameter("Importance", Type.IMPORTANCE));
		return report;
	}

	public static Report projectFindings() {
		final Report report = new Report();
		report.setUuid("ScanFindings");
		report.setTitle("Scan Findings");
		report.setDescription("Finding counts for a scan.");
		report.setDataSource(DataSource.TEAMSERVER);
		report.setOutputTypes(OutputType.CHART);
		final List<Parameter> params = report.getParameters();
		params.add(new Parameter("projectName", Type.PROJECTS));
		params.add(new Parameter("kLoC", Type.BOOLEAN));
		return report;
	}

	public static Report projectCompilations() {
		final Report report = new Report();
		report.setUuid("ScanFindings");
		report.setTitle("Scan Findings");
		report.setDescription("Finding counts for a scan.");
		report.setDataSource(DataSource.TEAMSERVER);
		report.setOutputTypes(OutputType.CHART);
		final List<Parameter> params = report.getParameters();
		params.add(new Parameter("projectName", Type.PROJECTS));
		params.add(new Parameter("kLoC", Type.BOOLEAN));
		return report;
	}

	public static Report scanImportances() {
		final Report report = new Report();
		report.setUuid("ScanImportances");
		report.setTitle("Scan Importances");
		report
				.setDescription("Show a breakdown of findings by importance for packages in a given scan of a project.");
		report.setDataSource(DataSource.TEAMSERVER);
		report.setOutputTypes(OutputType.CHART);
		final List<Parameter> params = report.getParameters();
		params.add(new Parameter("scan", Type.SCANS));
		params.add(new Parameter("importance", Type.IMPORTANCE));
		params.add(new Parameter("package", Type.TEXT));
		return report;
	}

	public static Report scanImportancesByCategory() {
		final Report report = new Report();
		report.setUuid("ScanImportancesByCategory");
		report.setTitle("Scan Importances");
		report
				.setDescription("Show a breakdown of findings by importance for packages in a given scan of a project.");
		report.setDataSource(DataSource.TEAMSERVER);
		report.setOutputTypes(OutputType.CHART);
		final List<Parameter> params = report.getParameters();
		params.add(new Parameter("scan", Type.SCANS));
		params.add(new Parameter("importance", Type.IMPORTANCE));
		params.add(new Parameter("package", Type.TEXT));
		params.add(new Parameter("category", Type.CATEGORY));
		return report;
	}

	public static Report scanFindings() {
		final Report report = new Report();
		report.setUuid("ScanFindings");
		report.setTitle("Scan Findings");
		report.setDescription("Finding counts for a scan.");
		report.setDataSource(DataSource.TEAMSERVER);
		report.setOutputTypes(OutputType.TABLE);
		final List<Parameter> params = report.getParameters();
		params.add(new Parameter("scan", Type.SCANS));
		params.add(new Parameter("importance", Type.IMPORTANCE));
		params.add(new Parameter("package", Type.TEXT));
		return report;
	}

	public static Report scanFindingsByCategory() {
		final Report report = new Report();
		report.setUuid("ScanFindingsByCategory");
		report.setTitle("Scan Findings By Category");
		report
				.setDescription("Finding counts in for a scan for the selected categories.");
		report.setDataSource(DataSource.TEAMSERVER);
		report.setOutputTypes(OutputType.TABLE);
		final List<Parameter> params = report.getParameters();
		params.add(new Parameter("scan", Type.SCANS));
		params.add(new Parameter("category", Type.CATEGORY));
		params.add(new Parameter("package", Type.TEXT));
		return report;
	}

	public static Report findingTypeCounts() {
		final Report report = new Report();
		report.setUuid("FindingTypeCounts");
		report.setTitle("Finding Type Counts");
		report
				.setDescription("The number of findings of this type in each project.");
		report.setDataSource(DataSource.TEAMSERVER);
		report.setOutputTypes(OutputType.CHART);
		final List<Parameter> params = report.getParameters();
		params.add(new Parameter("uuid", Type.FINDING_TYPE));
		return report;
	}

	public static Report compareProjectScans() {
		final Report report = new Report();
		report.setUuid("CompareProjectScans");
		report.setTitle("Compare Project Scans");
		report
				.setDescription("Compute and show the differences between two scans of the same project.");
		report.setDataSource(DataSource.TEAMSERVER);
		report.setOutputTypes(OutputType.TABLE, OutputType.CHART);
		final List<Parameter> params = report.getParameters();
		params.add(new Parameter("scans", Type.SCANS));
		return report;
	}

	@Override
	protected void doSaveCall(final Report item,
			final AsyncCallback<Status> callback) {
		// There is no save action
		callback
				.onFailure(new UnsupportedOperationException("Not implemented"));
	}

}
