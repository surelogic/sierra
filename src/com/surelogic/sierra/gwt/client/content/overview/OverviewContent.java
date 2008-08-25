package com.surelogic.sierra.gwt.client.content.overview;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.surelogic.sierra.gwt.client.Context;
import com.surelogic.sierra.gwt.client.chart.AuditContributionsChart;
import com.surelogic.sierra.gwt.client.chart.LatestScansChart;
import com.surelogic.sierra.gwt.client.content.ContentComposite;
import com.surelogic.sierra.gwt.client.data.ReportSettings;
import com.surelogic.sierra.gwt.client.table.ReportTableSection;
import com.surelogic.sierra.gwt.client.ui.ActionPanel;
import com.surelogic.sierra.gwt.client.ui.SectionPanel;
import com.surelogic.sierra.gwt.client.ui.SplitPanel;

public final class OverviewContent extends ContentComposite {
	private static final OverviewContent instance = new OverviewContent();
	private final VerticalPanel dashboard = new VerticalPanel();
	private final List<SectionPanel> sections = new ArrayList<SectionPanel>();
	private final ActionPanel actionPanel = new ActionPanel();

	public static OverviewContent getInstance() {
		return instance;
	}

	private OverviewContent() {
		super();
	}

	@Override
	protected void onInitialize(final DockPanel rootPanel) {
		final VerticalPanel panel = new VerticalPanel();
		panel.setWidth("100%");

		final HorizontalPanel titlePanel = new HorizontalPanel();
		titlePanel.setWidth("100%");
		titlePanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		titlePanel.add(new HTML("<p>Welcome to Sierra Team Server!</p>"));
		actionPanel.addAction("Edit", new ClickListener() {

			public void onClick(final Widget sender) {
				toggleDashboardEdit(true);
			}
		});
		actionPanel.addAction("Save", new ClickListener() {

			public void onClick(final Widget sender) {
				saveDashboard();
			}
		});
		actionPanel.setActionVisible("Save", false);
		actionPanel.addAction("Cancel", new ClickListener() {

			public void onClick(final Widget sender) {
				toggleDashboardEdit(false);
			}
		});
		actionPanel.setActionVisible("Cancel", false);

		titlePanel.add(actionPanel);
		titlePanel.setCellHorizontalAlignment(actionPanel,
				HasHorizontalAlignment.ALIGN_RIGHT);
		panel.add(titlePanel);

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
	protected void onUpdate(final Context context) {
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

	private void toggleDashboardEdit(final boolean editMode) {
		actionPanel.setActionVisible("Edit", !editMode);
		actionPanel.setActionVisible("Save", editMode);
		actionPanel.setActionVisible("Cancel", editMode);

		// TODO switch to edit mode
	}

	private void saveDashboard() {
		// TODO save the dashboard and leave edit mode
	}

}
