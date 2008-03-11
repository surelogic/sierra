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
								"<span class=\"error\">Unable to retrieve project overviews. (Server may be down)</span>"));
			}

			public void onSuccess(Object result) {
				projects.clear();
				List list = (List) result;
				if (list.isEmpty()) {
					projects
							.add(new HTML(
									"<span class=\"success\">No project scans have been published to this server.</span>"));
				} else {
					final Grid grid = new Grid(list.size() + 1, 10);
					grid.setStyleName("overview-table");
					final RowFormatter f = grid.getRowFormatter();
					final CellFormatter cf = grid.getCellFormatter();
					final String[] projectHeader = new String[] { "Project",
							"Last Scan", "Audits", "Critical", "High",
							"Medium", "Low", "Irrelevant", "Last Audit",
							"By" };
					final String[] dataStyle = new String[] { "cell-text",
							"cell-date", "cell-text", "cell-number",
							"cell-number", "cell-number", "cell-number",
							"cell-number", "cell-date", "cell-text" };
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
						grid.setText(i, j++, po.getLastScanDate());
						if (po.getComments() > 0) {
							grid.setText(i, j++, Integer.toString(po
									.getComments())
									+ " on "
									+ Integer.toString(po
											.getCommentedFindings())
									+ " findings");
						} else {
							grid.setText(i, j++, "");
						}

						grid.setText(i, j++, iToS(po.getCritical()));
						grid.setText(i, j++, iToS(po.getHigh()));
						grid.setText(i, j++, iToS(po.getMedium()));
						grid.setText(i, j++, iToS(po.getLow()));
						grid.setText(i, j++, iToS(po.getIrrelevant()));
						grid.setText(i, j++,
								po.getLastSynchDate().length() == 0 ? "" : po
										.getLastSynchDate());
						grid.setText(i, j++, po.getLastSynchUser());
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
								"<span class=\"error\">Unable to retrieve user overviews. (Server may be down)</span>"));
			}

			public void onSuccess(Object result) {
				users.clear();
				final List list = (List) result;
				if (list.isEmpty()) {
					users
							.add(new HTML(
									"<span class=\"success\">No users defined for this server.</span>"));
				} else {
					final Grid grid = new Grid(list.size() + 1, 3);
					grid.setStyleName("overview-table");
					final RowFormatter f = grid.getRowFormatter();
					final CellFormatter cf = grid.getCellFormatter();
					final String[] userHeader = new String[] { "User",
							"Audits", "Last Contribution" };
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
						if (uo.getAudits() > 0) {
							grid.setText(i, j++, Integer.toString(uo
									.getAudits())
									+ " on "
									+ Integer.toString(uo.getFindings())
									+ " findings");
						} else {
							grid.setText(i, j++, "");
						}
						grid.setText(i, j++,
								uo.getLastSynch().length() == 0 ? "" : uo
										.getLastSynch());
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
		panel.add(new HTML("<p>Welcome to Sierra Team Server!</p><br>"));
		HorizontalPanel charts = new HorizontalPanel();
		charts.add(ChartBuilder.name("LatestScanResults").width(450).build());
		charts.add(ChartBuilder.name("AuditContributions").width(320).build());
		panel.add(charts);
		panel.add(new HTML("<h3>Projects</h3>"));
		panel.add(projects);
		panel.add(new HTML("<h3>Users</h3>"));
		panel.add(users);
		rootPanel.add(panel, DockPanel.CENTER);
		projects.add(new HTML("Fetching latest information."));
		users.add(new HTML("Fetching latest information."));
	}

	public static OverviewContent getInstance() {
		return instance;
	}

	private static String iToS(int i) {
		return i == 0 ? "" : Integer.toString(i);
	}

}
