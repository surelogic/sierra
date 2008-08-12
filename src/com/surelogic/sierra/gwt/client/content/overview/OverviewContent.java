package com.surelogic.sierra.gwt.client.content.overview;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.surelogic.sierra.gwt.client.Context;
import com.surelogic.sierra.gwt.client.chart.AuditContributionsChart;
import com.surelogic.sierra.gwt.client.chart.LatestScansChart;
import com.surelogic.sierra.gwt.client.content.ContentComposite;
import com.surelogic.sierra.gwt.client.data.ReportSettings;
import com.surelogic.sierra.gwt.client.table.ReportTableSection;
import com.surelogic.sierra.gwt.client.ui.SectionPanel;

public final class OverviewContent extends ContentComposite {
	private static final OverviewContent instance = new OverviewContent();
	private final FlexTable dashboard = new FlexTable();
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
		dashboard.getColumnFormatter().setWidth(0, "50%");
		dashboard.getColumnFormatter().setWidth(1, "50%");

		addDashboardSection(0, 0, 2, 1, new LatestScansChart());
		addDashboardSection(0, 1, 1, 1, new AuditContributionsChart());
		addAuditsTable();
		addPublishedProjectsTable();
		rootPanel.add(panel, DockPanel.CENTER);
	}

	private void addPublishedProjectsTable() {
		final ReportSettings r = new ReportSettings();
		r.setReportUuid("PublishedProjects");
		r.setTitle("All Published Projects");
		r.setDescription("All Published Projects");
		addDashboardSection(2, 0, 1, 2, new ReportTableSection(r));
	}

	private void addAuditsTable() {
		final ReportSettings r = new ReportSettings();
		r.setReportUuid("UserAudits");
		r.setTitle("Users");
		r.setDescription("Latest user audits");
		addDashboardSection(1, 0, 1, 1, new ReportTableSection(r));
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

	private void addDashboardSection(int row, int col, int rowSpan,
			int colSpan, SectionPanel section) {
		dashboard.setWidget(row, col, section);
		dashboard.getCellFormatter().addStyleName(row, col, "dashboard-cell");
		dashboard.getFlexCellFormatter().setRowSpan(row, col, rowSpan);
		dashboard.getFlexCellFormatter().setColSpan(row, col, colSpan);
		sections.add(section);
	}

}
