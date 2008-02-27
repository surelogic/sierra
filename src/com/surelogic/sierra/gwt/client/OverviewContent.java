package com.surelogic.sierra.gwt.client;

import java.util.Iterator;
import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;
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
					for (Iterator i = list.iterator(); i.hasNext();) {
						ProjectOverview po = (ProjectOverview) i.next();
						projects
								.add(new HTML(
										"<h3>"
												+ po.getName()
												+ "</h3><p>"
												+ po.getComments()
												+ (po.getComments() == 1 ? " comment"
														: " comments")
												+ " on "
												+ po.getFindings()
												+ (po.getFindings() == 1 ? " finding"
														: " findings")
												+ " in the last 30 days.<br />"
												+ po.getCritical()
												+ " Critical</br />"
												+ po.getHigh()
												+ " High<br />"
												+ po.getMedium()
												+ " Medium<br />"
												+ po.getLow()
												+ " Low<br />"
												+ po.getIrrelevant()
												+ " Irrelevant</p>"
												+ ((po.getLastSynchUser() == null) ? ""
														: ("<p>Last updated by <span class=\"user\">"
																+ po
																		.getLastSynchUser()
																+ "</span> on "
																+ po
																		.getLastSynchDate() + "</p>"))));
					}
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
				List list = (List) result;
				if (list.isEmpty()) {
					projects
							.add(new HTML(
									"<span class=\"success\">No recent findings.</span>"));
				} else {
					for (Iterator i = list.iterator(); i.hasNext();) {
						UserOverview uo = (UserOverview) i.next();
						users.add(new HTML("<span class=\"user\">"
								+ uo.getUserName() + "</span> has "
								+ uo.getAudits() + " comments on "
								+ uo.getFindings() + " findings."));
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
