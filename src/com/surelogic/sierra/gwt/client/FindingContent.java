package com.surelogic.sierra.gwt.client;

import java.util.Iterator;

import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.surelogic.sierra.gwt.client.data.AuditOverview;
import com.surelogic.sierra.gwt.client.data.FindingOverview;
import com.surelogic.sierra.gwt.client.service.Callback;
import com.surelogic.sierra.gwt.client.service.ServiceHelper;
import com.surelogic.sierra.gwt.client.util.UI;

public class FindingContent extends ContentComposite {

	private static final FindingContent instance = new FindingContent();

	private HTML synopsis = new HTML();

	private HTML location = new HTML();

	private HTML description = new HTML();

	private VerticalPanel audits = new VerticalPanel();

	private VerticalPanel artifacts = new VerticalPanel();

	private FindingContent() {
		// Do nothing
	}

	protected void onActivate(Context context) {
		final String arg = context.getArgs();
		if (arg == null || arg.length() == 0) {
			setEmpty();
		} else {
			ServiceHelper.getFindingService().getFinding(arg, new Callback() {

				protected void onFailure(String message, Object result) {
					setEmpty();
				}

				protected void onSuccess(String message, Object result) {
					setFinding((FindingOverview) result);
				}

			});
		}
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
		for (Iterator i = f.getAudits().iterator(); i.hasNext();) {
			AuditOverview audit = (AuditOverview) i.next();
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

	protected boolean onDeactivate() {
		return true;
	}

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

	public static FindingContent getInstance() {
		return instance;
	}
}
