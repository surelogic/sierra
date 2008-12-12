package com.surelogic.sierra.gwt.client.data.cache;

import java.util.ArrayList;
import java.util.Collections;
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

public final class ReportCache extends Cache<Report> {
	private static final ReportCache instance = new ReportCache();

	public static ReportCache getInstance() {
		return instance;
	}

	private ReportCache() {
		// singleton
	}

	public static List<Report> allReports() {
		final List<Report> reports = new ArrayList<Report>();
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
		Collections.sort(reports);
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

	public static Report latestScans() {
		final Report report = new Report();
		report.setUuid("LatestScanResults");
		report.setTitle("Published Scans");
		report.setShortDescription("Latest Scan Results");
		report
				.setLongDescription("Shows the number of findings in the most recent published scan for each project.");
		report.setDataSource(DataSource.TEAMSERVER);
		report.setOutputTypes(OutputType.CHART, OutputType.TABLE);
		return report;
	}

	public static Report userAudits() {
		final Report report = new Report();
		report.setUuid("UserAudits");
		report.setTitle("Users");
		report.setShortDescription("Number of Comments In Last 30 Days");
		report
				.setLongDescription("Shows the active users and the number of comments made in the past 30 days.");
		report.setDataSource(DataSource.TEAMSERVER);
		report.setOutputTypes(OutputType.TABLE, OutputType.CHART);
		return report;
	}

	public static Report publishedProjects() {
		final Report report = new Report();
		report.setUuid("PublishedProjects");
		report.setTitle("Published Projects");
		report.setShortDescription("Projects Published To This Portal");
		report
				.setLongDescription("General information on all projects commented on or published to the server.");
		report.setDataSource(DataSource.TEAMSERVER);
		report.setOutputTypes(OutputType.TABLE);
		return report;
	}

	public static Report categoryCounts() {
		final Report report = new Report();
		report.setUuid("CategoryCounts");
		report.setTitle("Category Findings");
		report.setShortDescription("Findings For This Category By Project");
		report
				.setLongDescription("Number of findings in each project for this category.");
		report.setDataSource(DataSource.TEAMSERVER);
		report.setOutputTypes(OutputType.CHART);
		final List<Parameter> params = report.getParameters();
		params.add(new Parameter("uuid", "Category", Type.CATEGORY));
		return report;
	}

	public static Report findingsByProject() {
		final Report report = new Report();
		report.setUuid("FindingsByProject");
		report.setTitle("Findings By Project");
		report.setShortDescription("Number of Findings By Project");
		report
				.setLongDescription("Displays the number of findings per project.");
		report.setDataSource(DataSource.TEAMSERVER);
		report.setOutputTypes(OutputType.CHART);
		final List<Parameter> params = report.getParameters();
		params.add(new Parameter("Projects", "Projects", Type.PROJECTS));
		params.add(new Parameter("Importance", "Importance", Type.IMPORTANCES));
		return report;
	}

	public static Report findingsByPriority() {
		final Report report = new Report();
		report.setUuid("FindingsByPriority");
		report.setTitle("Findings By Priority");
		report.setShortDescription("Number of Findings By Priority");
		report
				.setLongDescription("Displays the number of findings by priority.");
		report.setDataSource(DataSource.BUGLINK);
		report.setOutputTypes(OutputType.TABLE, OutputType.CHART,
				OutputType.PDF);
		final List<Parameter> params = report.getParameters();
		params.add(new Parameter("Importance", "Importance", Type.IMPORTANCES));
		return report;
	}

	public static Report projectFindings() {
		final Report report = new Report();
		report.setUuid("ProjectFindingsChart");
		report.setTitle("Project Findings");
		report.setShortDescription("Findings Over Time For This Project");
		report
				.setLongDescription("Shows finding trends over time for a project.");
		report.setDataSource(DataSource.TEAMSERVER);
		report.setOutputTypes(OutputType.CHART);
		final List<Parameter> params = report.getParameters();
		params.add(new Parameter("projectName", "Projects", Type.PROJECT));
		params.add(new Parameter("kLoC", "Show KLoC", Type.BOOLEAN));
		return report;
	}

	public static Report projectCompilations() {
		final Report report = new Report();
		report.setUuid("ProjectCompilationsChart");
		report.setTitle("Project Packages");
		report.setShortDescription("Findings By Package For This Project");
		report
				.setLongDescription("Shows the findings breakdown for each package in the project");
		report.setDataSource(DataSource.TEAMSERVER);
		report.setOutputTypes(OutputType.CHART);
		final List<Parameter> params = report.getParameters();
		params.add(new Parameter("projectName", "Project", Type.PROJECT));
		params.add(new Parameter("kLoC", "Show KLoC", Type.BOOLEAN));
		return report;
	}

	public static Report scanImportances() {
		final Report report = new Report();
		report.setUuid("ScanImportances");
		report.setTitle("Scan Importances");
		report
				.setShortDescription("Findings By Importance and Package For This Scan");
		report
				.setLongDescription("Show a breakdown of findings by importance for packages in a given scan of a project.");
		report.setDataSource(DataSource.TEAMSERVER);
		report.setOutputTypes(OutputType.CHART);
		final List<Parameter> params = report.getParameters();
		final Parameter project = new Parameter("project", "Projects",
				Type.PROJECT);
		final Parameter scan = new Parameter("scan", "Scans", Type.SCAN);
		project.getChildren().add(scan);
		scan.getChildren().add(
				new Parameter("package", "Package", Type.PACKAGES));
		params.add(project);
		params.add(new Parameter("importance", "Importance", Type.IMPORTANCES));
		return report;
	}

	public static Report scanImportancesByCategory() {
		final Report report = new Report();
		report.setUuid("ScanImportancesByCategory");
		report.setTitle("Scan Importances By Category");
		report
				.setShortDescription("Findings By Importance, Category, and Package For This Scan");
		report
				.setLongDescription("Show a breakdown of findings by importance and category for packages in a given scan of a project.");
		report.setDataSource(DataSource.TEAMSERVER);
		report.setOutputTypes(OutputType.CHART);
		final List<Parameter> params = report.getParameters();
		final Parameter project = new Parameter("project", "Projects",
				Type.PROJECT);
		final Parameter scan = new Parameter("scan", "Scans", Type.SCAN);
		project.getChildren().add(scan);
		scan.getChildren().add(
				new Parameter("package", "Package", Type.PACKAGES));
		params.add(project);
		params.add(new Parameter("importance", "Importance", Type.IMPORTANCES));
		params.add(new Parameter("category", "Category", Type.CATEGORY));
		return report;
	}

	public static Report scanFindings() {
		final Report report = new Report();
		report.setUuid("ScanFindings");
		report.setTitle("Scan Findings");
		report.setShortDescription("Finding counts By Scan");
		report.setLongDescription("Finding counts for a scan.");
		report.setDataSource(DataSource.TEAMSERVER);
		report.setOutputTypes(OutputType.TABLE);
		final List<Parameter> params = report.getParameters();
		final Parameter project = new Parameter("project", "Projects",
				Type.PROJECT);
		final Parameter scan = new Parameter("scan", "Scans", Type.SCAN);
		project.getChildren().add(scan);
		scan.getChildren().add(
				new Parameter("package", "Package", Type.PACKAGES));
		params.add(project);
		params.add(new Parameter("importance", "Importance", Type.IMPORTANCES));
		return report;
	}

	public static Report scanFindingsByCategory() {
		final Report report = new Report();
		report.setUuid("ScanFindingsByCategory");
		report.setTitle("Scan Findings By Category");
		report.setShortDescription("Finding Counts By Category For This Scan");
		report
				.setLongDescription("Finding counts in for a scan for the selected categories.");
		report.setDataSource(DataSource.TEAMSERVER);
		report.setOutputTypes(OutputType.TABLE);
		final List<Parameter> params = report.getParameters();
		final Parameter project = new Parameter("project", "Projects",
				Type.PROJECT);
		final Parameter scan = new Parameter("scan", "Scans", Type.SCAN);
		project.getChildren().add(scan);
		scan.getChildren().add(
				new Parameter("package", "Package", Type.PACKAGES));
		params.add(project);
		params.add(new Parameter("category", "Category", Type.CATEGORY));
		return report;
	}

	public static Report findingTypeCounts() {
		final Report report = new Report();
		report.setUuid("FindingTypeCounts");
		report.setTitle("Finding Type Counts");
		report.setShortDescription("Finding Count By Project For This Type");
		report
				.setLongDescription("The number of findings of this type in each project.");
		report.setDataSource(DataSource.TEAMSERVER);
		report.setOutputTypes(OutputType.CHART);
		final List<Parameter> params = report.getParameters();
		params.add(new Parameter("uuid", "Finding Types", Type.FINDING_TYPE));
		return report;
	}

	public static Report compareProjectScans() {
		final Report report = new Report();
		report.setUuid("CompareProjectScans");
		report.setTitle("Compare Project Scans");
		report.setShortDescription("Change Between Scans Of This Project");
		report
				.setLongDescription("Compute and show the differences between two scans of the same project.");
		report.setDataSource(DataSource.TEAMSERVER);
		report.setOutputTypes(OutputType.TABLE, OutputType.CHART);
		final List<Parameter> params = report.getParameters();
		final Parameter project = new Parameter("project", "Projects",
				Type.PROJECT);
		final Parameter scan = new Parameter("scans", "Scans", Type.SCANS);
		params.add(project);
		project.getChildren().add(scan);
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
