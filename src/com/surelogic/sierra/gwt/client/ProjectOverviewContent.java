package com.surelogic.sierra.gwt.client;

import java.util.Iterator;
import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.surelogic.sierra.gwt.client.data.ProjectOverview;
import com.surelogic.sierra.gwt.client.service.ServiceHelper;

public class ProjectOverviewContent extends ContentComposite {

	private static final ProjectOverviewContent instance = new ProjectOverviewContent();

	private final VerticalPanel projects = new VerticalPanel();

	private ProjectOverviewContent() {
		super();
	}

	public String getContextName() {
		return "Projects";
	}

	protected void onActivate() {
		ServiceHelper.getProjectOverviewService().getProjectOverviews(
				new AsyncCallback() {

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
														+ ((po
																.getLastSynchUser() == null) ? ""
																: ("<p>Last updated by "
																		+ po
																				.getLastSynchUser()
																		+ " on "
																		+ po
																				.getLastSynchDate() + "</p>"))));
							}
						}
					}

				});
	}

	protected boolean onDeactivate() {
		return true;
	}

	protected void onInitialize(DockPanel rootPanel) {
		rootPanel.add(projects, DockPanel.CENTER);
		projects.add(new HTML("Fetching latest information."));
	}

	public static ProjectOverviewContent getInstance() {
		return instance;
	}

}
