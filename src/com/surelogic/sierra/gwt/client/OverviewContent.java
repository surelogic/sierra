package com.surelogic.sierra.gwt.client;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.surelogic.sierra.gwt.client.chart.AuditContributionsChart;
import com.surelogic.sierra.gwt.client.chart.LatestScansChart;
import com.surelogic.sierra.gwt.client.table.LatestAuditsTable;
import com.surelogic.sierra.gwt.client.table.PublishedProjectsTable;
import com.surelogic.sierra.gwt.client.ui.SectionPanel;

public final class OverviewContent extends ContentComposite {
	private static final OverviewContent instance = new OverviewContent();
	private final FlexTable dashboard = new FlexTable();
	private final List sections = new ArrayList();

	public static OverviewContent getInstance() {
		return instance;
	}

	private OverviewContent() {
		super();
	}

	protected void onInitialize(DockPanel rootPanel) {
		final VerticalPanel panel = new VerticalPanel();
		panel.setWidth("100%");
		panel.add(new HTML("<p>Welcome to Sierra Team Server!</p>"));

		panel.add(dashboard);
		dashboard.addStyleName("dashboard");
		dashboard.setWidth("100%");
		dashboard.getColumnFormatter().setWidth(0, "50%");
		dashboard.getColumnFormatter().setWidth(1, "50%");

		addDashboardSection(0, 0, new LatestScansChart());
		addDashboardSection(0, 1, new AuditContributionsChart());
		addDashboardSection(1, 0, new PublishedProjectsTable());
		addDashboardSection(1, 1, new LatestAuditsTable());

		rootPanel.add(panel, DockPanel.CENTER);
	}

	protected void onActivate(Context context) {
		for (Iterator it = sections.iterator(); it.hasNext();) {
			((SectionPanel) it.next()).activate(context);
		}
	}

	protected void onUpdate(Context context) {
		for (Iterator it = sections.iterator(); it.hasNext();) {
			((SectionPanel) it.next()).update(context);
		}
	}

	protected void onDeactivate() {
		for (Iterator it = sections.iterator(); it.hasNext();) {
			((SectionPanel) it.next()).deactivate();
		}
	}

	private void addDashboardSection(int row, int col, SectionPanel section) {
		dashboard.setWidget(row, col, section);
		dashboard.getCellFormatter().addStyleName(row, col, "dashboard-cell");
		sections.add(section);
	}

}
