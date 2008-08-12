package com.surelogic.sierra.gwt.client.content.overview;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.surelogic.sierra.gwt.client.Context;
import com.surelogic.sierra.gwt.client.chart.AuditContributionsChart;
import com.surelogic.sierra.gwt.client.chart.LatestScansChart;
import com.surelogic.sierra.gwt.client.content.ContentComposite;
import com.surelogic.sierra.gwt.client.data.ReportSettings;
import com.surelogic.sierra.gwt.client.table.ReportTableSection;
import com.surelogic.sierra.gwt.client.ui.SectionPanel;
import com.surelogic.sierra.gwt.client.ui.SplitPanel;

public final class OverviewContent extends ContentComposite {
	private static final OverviewContent instance = new OverviewContent();
	private final VerticalPanel dashboard = new VerticalPanel();
	private final List<SectionPanel> sections = new ArrayList<SectionPanel>();

	public static OverviewContent getInstance() {
		return instance;
	}

	private OverviewContent() {
		super();
	}

	@Override
	protected void onInitialize(DockPanel rootPanel) {
		final VerticalPanel panel = new VerticalPanel();
		panel.setWidth("100%");
		panel.add(new HTML("<p>Welcome to Sierra Team Server!</p>"));

		panel.add(dashboard);
		dashboard.addStyleName("dashboard");
		dashboard.setWidth("100%");

		final SplitPanel scansAudits = new SplitPanel();

		final SectionPanel latestScans = new LatestScansChart();
		sections.add(latestScans);
		scansAudits.addLeft(latestScans);

		final SectionPanel auditContribs = new AuditContributionsChart();
		sections.add(auditContribs);
		scansAudits.addRight(auditContribs);

		final ReportSettings userAudits = new ReportSettings();
		userAudits.setReportUuid("UserAudits");
		userAudits.setTitle("Users");
		userAudits.setDescription("Latest user audits");
		final ReportTableSection auditsTable = new ReportTableSection(
				userAudits);
		sections.add(auditsTable);
		scansAudits.addRight(auditsTable);

		dashboard.add(scansAudits);

		addPublishedProjectsTable();
		rootPanel.add(panel, DockPanel.CENTER);
	}

	private void addPublishedProjectsTable() {
		final ReportSettings r = new ReportSettings();
		r.setReportUuid("PublishedProjects");
		r.setTitle("All Published Projects");
		r.setDescription("All Published Projects");
		final ReportTableSection reportTable = new ReportTableSection(r);
		sections.add(reportTable);
		dashboard.add(reportTable);
	}

	@Override
	protected void onUpdate(Context context) {
		for (final SectionPanel section : sections) {
			section.update(context);
		}
	}

	@Override
	protected void onDeactivate() {
		for (final SectionPanel section : sections) {
			section.deactivate();
		}
	}

}
