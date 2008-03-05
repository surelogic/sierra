package com.surelogic.sierra.gwt.client;

import java.util.Iterator;
import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.HTMLTable.CellFormatter;
import com.google.gwt.user.client.ui.HTMLTable.RowFormatter;
import com.surelogic.sierra.gwt.client.data.ProjectOverview;
import com.surelogic.sierra.gwt.client.data.UserOverview;
import com.surelogic.sierra.gwt.client.service.OverviewServiceAsync;
import com.surelogic.sierra.gwt.client.service.ServiceHelper;
import com.surelogic.sierra.gwt.client.util.ChartBuilder;

public class OverviewContent extends ContentComposite {

	private static final OverviewContent instance = new OverviewContent();

	private final VerticalPanel users = new VerticalPanel();
	private final VerticalPanel projects = new VerticalPanel();

	private OverviewContent() {
		super();
	}

	public String getContextName() {
		return "Projects";
	}

	protected void onActivate() {
		OverviewServiceAsync service = ServiceHelper.getOverviewService();
		service.getProjectOverviews(new AsyncCallback() {

			public void onFailure(Throwable caught) {
				// TODO what do we actually do here now?
				projects.clear();
				projects
						.add(new HTML(
								"<span class=\"error\">Error communicating with server.</span>"));
			}

			public void onSuccess(Object result) {
				projects.clear();
				List list = (List) result;
				if (list.isEmpty()) {
					projects
							.add(new HTML(
									"<span class=\"success\">No recent findings.</span>"));
				} else {
					final Grid grid = new Grid(list.size() + 1, 11);
					grid.setStyleName("overview-table");
					final RowFormatter f = grid.getRowFormatter();
					final CellFormatter cf = grid.getCellFormatter();
					final String[] projectHeader = new String[] { "Project",
							"Comments", "Critical", "High", "Medium", "Low",
							"Total", "Last Synch", "Last Synched By",
							"Last Scan Time" };
					final String[] dataStyle = new String[] { "cell-text",
							"cell-text", "cell-number", "cell-number",
							"cell-number", "cell-number", "cell-number",
							"cell-date", "cell-text", "cell-date" };
					for (int j = 0; j < projectHeader.length; j++) {
						grid.setText(0, j, projectHeader[j]);
					}
					f.setStyleName(0, "overview-header");
					int i = 1;
					for (Iterator rows = list.iterator(); rows.hasNext(); i++) {
						int j = 0;
						f.setStyleName(i, "overview-data");
						ProjectOverview po = (ProjectOverview) rows.next();
						grid.setText(i, j++, po.getName());
						grid.setText(i, j++, Integer.toString(po.getComments())
								+ " on "
								+ Integer.toString(po.getCommentedFindings())
								+ " findings");
						final String scanTime = po.getLastScanDate();
						if (!"-".equals(scanTime)) {
							grid.setText(i, j++, Integer.toString(po
									.getCritical()));
							grid
									.setText(i, j++, Integer.toString(po
											.getHigh()));
							grid.setText(i, j++, Integer.toString(po
									.getMedium()));
							grid.setText(i, j++, Integer.toString(po.getLow()));
							grid.setText(i, j++, Integer.toString(po
									.getTotalFindings()));
						} else {
							for (int k = 0; k < 6; k++) {
								grid.setText(i, j++, "-");
							}
						}
						grid.setText(i, j++, po.getLastSynchDate());
						grid.setText(i, j++, po.getLastSynchUser());
						grid.setText(i, j++, scanTime);
						for (j = 0; j < dataStyle.length; j++) {
							cf.addStyleName(i, j, dataStyle[j]);
						}
					}
					projects.add(grid);
				}
			}

		});
		service.getUserOverviews(new AsyncCallback() {

			public void onFailure(Throwable caught) {
				// TODO what do we actually do here now?
				users.clear();
				users
						.add(new HTML(
								"<span class=\"error\">Error communicating with server.</span>"));
			}

			public void onSuccess(Object result) {
				users.clear();
				final List list = (List) result;
				if (list.isEmpty()) {
					users
							.add(new HTML(
									"<span class=\"success\">No recent findings.</span>"));
				} else {
					final Grid grid = new Grid(list.size() + 1, 3);
					grid.setStyleName("overview-table");
					final RowFormatter f = grid.getRowFormatter();
					final CellFormatter cf = grid.getCellFormatter();
					final String[] userHeader = new String[] { "User",
							"Comments", "Last Synched" };
					final String[] dataStyle = new String[] { "cell-text",
							"cell-text", "cell-date" };
					for (int j = 0; j < userHeader.length; j++) {
						grid.setText(0, j, userHeader[j]);
					}
					f.setStyleName(0, "overview-header");
					int i = 1;
					for (Iterator rows = list.iterator(); rows.hasNext(); i++) {
						int j = 0;
						f.setStyleName(i, "overview-data");
						final UserOverview uo = (UserOverview) rows.next();
						grid.setText(i, j++, uo.getUserName());
						grid.setText(i, j++, Integer.toString(uo.getAudits())
								+ " on " + Integer.toString(uo.getFindings())
								+ " findings");
						grid.setText(i, j++, uo.getLastSynch());
						for (j = 0; j < dataStyle.length; j++) {
							cf.addStyleName(i, j, dataStyle[j]);
						}
					}
					users.add(grid);
				}
			}
		});
	}

	protected boolean onDeactivate() {
		return true;
	}

	protected void onInitialize(DockPanel rootPanel) {
		final VerticalPanel panel = new VerticalPanel();
		HorizontalPanel charts = new HorizontalPanel();
		charts.add(ChartBuilder.name("LatestScanResults").width(500).build());
		charts.add(ChartBuilder.name("AuditContributions").width(400).build());
		panel.add(charts);
		panel.add(new HTML("<h2>Projects</h2>"));
		panel.add(projects);
		panel.add(new HTML("<h2>Users</h2>"));
		panel.add(users);
		rootPanel.add(panel, DockPanel.CENTER);
		projects.add(new HTML("Fetching latest information."));
		users.add(new HTML("Fetching latest information."));
	}

	public static OverviewContent getInstance() {
		return instance;
	}

}
