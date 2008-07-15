package com.surelogic.sierra.gwt.client.content.finding;

import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.surelogic.sierra.gwt.client.Context;
import com.surelogic.sierra.gwt.client.content.ContentComposite;
import com.surelogic.sierra.gwt.client.data.AuditOverview;
import com.surelogic.sierra.gwt.client.data.FindingOverview;
import com.surelogic.sierra.gwt.client.service.Callback;
import com.surelogic.sierra.gwt.client.service.ServiceHelper;
import com.surelogic.sierra.gwt.client.util.UI;

public final class FindingContent extends ContentComposite {
	public static final String PARAM_FINDING = "finding";

	private static final FindingContent instance = new FindingContent();

	private HTML synopsis = new HTML();

	private HTML location = new HTML();

	private HTML description = new HTML();

	private VerticalPanel audits = new VerticalPanel();

	private VerticalPanel artifacts = new VerticalPanel();

	public static FindingContent getInstance() {
		return instance;
	}

	private FindingContent() {
		// singleton
	}

	@Override
	protected void onInitialize(DockPanel rootPanel) {
		final VerticalPanel panel = new VerticalPanel();
		panel.add(UI.h3("Synopsis"));
		panel.add(synopsis);
		panel.add(UI.h3("Location"));
		panel.add(location);
		panel.add(UI.h3("Description"));
		panel.add(description);
		panel.add(UI.h3("Audits"));
		panel.add(audits);
		getRootPanel().add(panel, DockPanel.CENTER);
	}

	@Override
	protected void onUpdate(Context context) {
		final String findingType = context.getParameter(PARAM_FINDING);
		if (findingType == null || findingType.length() == 0) {
			setEmpty();
		} else {
			ServiceHelper.getFindingService().getFinding(findingType,
					new Callback<FindingOverview>() {

						@Override
						protected void onFailure(String message,
								FindingOverview result) {
							setEmpty();
						}

						@Override
						protected void onSuccess(String message,
								FindingOverview result) {
							setFinding(result);
						}

					});
		}
	}

	@Override
	protected void onDeactivate() {
		// nothing to do
	}

	private void clear() {
		synopsis.setText("");
		location.setText("");
		description.setText("");
		audits.clear();
		artifacts.clear();
	}

	private void setEmpty() {
		clear();
		synopsis.setText("No Finding");
	}

	private void setFinding(FindingOverview f) {
		clear();
		String firstReported = null;
		String firstReportedBy = null;
		for (AuditOverview audit : f.getAudits()) {
			if (firstReported == null) {
				firstReported = audit.getTime();
				firstReportedBy = audit.getUser();
			}
			audits.add(new HTML(audit.getTime() + " by <span class=\"user\">"
					+ audit.getUser() + "</span>:<p class=\"audit\">"
					+ audit.getText() + "</p>"));
		}
		final String s = "This <em>"
				+ f.getFindingType()
				+ "</em> in <em>"
				+ f.getCategory()
				+ "</em> is of <strong>"
				+ f.getImportance()
				+ "</strong> importance."
				+ (f.getAudits().isEmpty() ? ""
						: "It has been audited "
								+ f.getAudits().size()
								+ " times, and was first audited by <span class=\"user\">"
								+ firstReportedBy + "</span> on "
								+ firstReported + ".");
		synopsis.setHTML(s);
		location.setHTML(f.getPackageName() + "." + f.getClassName());
		description.setHTML(f.getSummary());
	}
}
