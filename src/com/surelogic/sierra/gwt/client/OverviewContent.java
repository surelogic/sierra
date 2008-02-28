package com.surelogic.sierra.gwt.client;

import java.util.Iterator;
import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.HTMLTable.RowFormatter;
import com.surelogic.sierra.gwt.client.data.ProjectOverview;
import com.surelogic.sierra.gwt.client.data.UserOverview;
import com.surelogic.sierra.gwt.client.service.OverviewServiceAsync;
import com.surelogic.sierra.gwt.client.service.ServiceHelper;

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
					final Grid grid = new Grid(list.size() + 1, 10);
					grid.setStyleName("overview-table");
					final RowFormatter f = grid.getRowFormatter();
					final String[] projectHeader = new String[] { "Project",
							"# Comments", "# Findings", "Critical", "High",
							"Medium", "Low", "Irrelevant", "Last Synch",
							"Last Synched By" };
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
						grid
								.setText(i, j++, Integer.toString(po
										.getComments()));
						grid
								.setText(i, j++, Integer.toString(po
										.getFindings()));
						grid
								.setText(i, j++, Integer.toString(po
										.getCritical()));
						grid.setText(i, j++, Integer.toString(po.getHigh()));
						grid.setText(i, j++, Integer.toString(po.getMedium()));
						grid.setText(i, j++, Integer.toString(po.getLow()));
						grid.setText(i, j++, Integer.toString(po
								.getIrrelevant()));
						grid.setText(i, j++, po.getLastSynchDate());
						grid.setText(i, j++, po.getLastSynchUser());
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
					projects
							.add(new HTML(
									"<span class=\"success\">No recent findings.</span>"));
				} else {
					final Grid grid = new Grid(list.size() + 1, 10);
					grid.setStyleName("overview-table");
					final RowFormatter f = grid.getRowFormatter();
					final String[] userHeader = new String[] { "User",
							"# Audits", "# Findings" };
					for (int j = 0; j < userHeader.length; j++) {
						grid.setText(0, j, userHeader[j]);
					}
					f.setStyleName(0, "overview-header");
					int i = 1;
					for (Iterator rows = list.iterator(); rows.hasNext(); i++) {
						int j = 0;
						f.setStyleName(i, "overview-data");
						UserOverview uo = (UserOverview) rows.next();
						grid.setText(i, j++, uo.getUserName());
						grid.setText(i, j++, Integer.toString(uo.getAudits()));
						grid
								.setText(i, j++, Integer.toString(uo
										.getFindings()));
					}
				}
			}
		});
	}

	protected boolean onDeactivate() {
		return true;
	}

	protected void onInitialize(DockPanel rootPanel) {
		final VerticalPanel panel = new VerticalPanel();
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
