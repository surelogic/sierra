package com.surelogic.sierra.gwt.client.content.finding;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.surelogic.sierra.gwt.client.Context;
import com.surelogic.sierra.gwt.client.content.ContentComposite;
import com.surelogic.sierra.gwt.client.content.scans.ScanContent;
import com.surelogic.sierra.gwt.client.data.ArtifactOverview;
import com.surelogic.sierra.gwt.client.data.AuditOverview;
import com.surelogic.sierra.gwt.client.data.FindingOverview;
import com.surelogic.sierra.gwt.client.data.ImportanceView;
import com.surelogic.sierra.gwt.client.service.ServiceHelper;
import com.surelogic.sierra.gwt.client.service.callback.ResultCallback;
import com.surelogic.sierra.gwt.client.service.callback.StandardCallback;
import com.surelogic.sierra.gwt.client.ui.StyleHelper;
import com.surelogic.sierra.gwt.client.ui.StyleHelper.Style;
import com.surelogic.sierra.gwt.client.ui.choice.SingleImportanceChoice;
import com.surelogic.sierra.gwt.client.ui.link.ContentLink;

public final class FindingContent extends ContentComposite {
	public static final String PARAM_FINDING = "finding";

	private static final FindingContent instance = new FindingContent();

	private final VerticalPanel findingPanel = new VerticalPanel();

	private final HTML synopsis = new HTML();

	private final HTML location = new HTML();

	private final HTML description = new HTML();

	private final VerticalPanel audits = new VerticalPanel();

	private final VerticalPanel auditBox = new VerticalPanel();

	private final VerticalPanel artifacts = new VerticalPanel();

	private final VerticalPanel selectionPanel = new VerticalPanel();

	public static FindingContent getInstance() {
		return instance;
	}

	private FindingContent() {
		// singleton
	}

	@Override
	protected void onInitialize(final DockPanel rootPanel) {
		findingPanel.add(StyleHelper.add(new Label("Synopsis"), Style.STRONG));
		findingPanel.add(synopsis);
		findingPanel.add(StyleHelper.add(new Label("Location"), Style.STRONG));
		findingPanel.add(location);
		findingPanel.add(StyleHelper
				.add(new Label("Description"), Style.STRONG));
		findingPanel.add(description);
		findingPanel.add(StyleHelper.add(new Label("Audits"), Style.STRONG));
		findingPanel.add(audits);
		findingPanel.add(auditBox);
		findingPanel.add(StyleHelper.add(new Label("Artifacts"), Style.STRONG));
		findingPanel.add(artifacts);
		getRootPanel().add(findingPanel, DockPanel.CENTER);
		findingPanel.setVisible(false);
		final ContentLink link = new ContentLink(
				"Select a Finding From the Scans Tab", ScanContent
						.getInstance(), null);
		link.addStyleName("padded");
		selectionPanel.add(link);
		getRootPanel().add(selectionPanel, DockPanel.NORTH);
		selectionPanel.setVisible(false);
	}

	@Override
	protected void onUpdate(final Context context) {
		final String findingType = context.getParameter(PARAM_FINDING);
		if ((findingType == null) || (findingType.length() == 0)) {
			setEmpty();
		} else {
			ServiceHelper.getFindingService().getFinding(findingType,
					new StandardCallback<FindingOverview>() {

						@Override
						protected void doSuccess(final FindingOverview result) {
							if (result == null) {
								setEmpty();
							} else {
								setFinding(result);
							}
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
		auditBox.clear();
	}

	private void setEmpty() {
		clear();
		findingPanel.setVisible(false);
		selectionPanel.setVisible(true);
	}

	private void setFinding(final FindingOverview f) {
		clear();
		findingPanel.setVisible(true);
		selectionPanel.setVisible(false);
		String firstReported = null;
		String firstReportedBy = null;
		if (f.getAudits().isEmpty()) {
			audits.add(new Label("No comments"));
		} else {
			for (final AuditOverview audit : f.getAudits()) {
				if (firstReported == null) {
					firstReported = audit.getTime();
					firstReportedBy = audit.getUser();
				}
				audits.add(new HTML(audit.getTime()
						+ " by <span class=\"user\">" + audit.getUser()
						+ "</span>:<p class=\"audit\">" + audit.getText()
						+ "</p>"));
			}
		}
		for (final ArtifactOverview artifact : f.getArtifacts()) {
			artifacts.add(new HTML(artifact.getTime()
					+ " by <span class=\"artifact\">" + artifact.getTool()
					+ " - " + artifact.getType()
					+ "</span>:<p class=\"artifact\">" + artifact.getSummary()
					+ "</p>"));
		}
		final String s = "This <em>"
				+ f.getFindingType()
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
		final SingleImportanceChoice importanceChoice = new SingleImportanceChoice();
		final ResultCallback<FindingOverview> callback = new ResultCallback<FindingOverview>() {

			@Override
			protected void doFailure(final String message,
					final FindingOverview result) {
				// TODO Auto-generated method stub
			}

			@Override
			protected void doSuccess(final String message,
					final FindingOverview result) {
				setFinding(result);
			}
		};
		importanceChoice.addChangeListener(new ChangeListener() {
			public void onChange(final Widget sender) {
				final ImportanceView selectedImportance = importanceChoice
						.getSelectedImportance();
				if (selectedImportance != f.getImportance()) {
					ServiceHelper.getFindingService().changeImportance(
							f.getFindingId(), selectedImportance, callback);
				}
			}
		});
		final TextBox commentBox = new TextBox();
		final Button comment = new Button("Comment");
		comment.addClickListener(new ClickListener() {
			public void onClick(final Widget sender) {
				if (commentBox.getText().length() > 0) {
					ServiceHelper.getFindingService().comment(f.getFindingId(),
							commentBox.getText(), callback);
				}
			}
		});
		auditBox.add(importanceChoice);
		auditBox.add(commentBox);
		auditBox.add(comment);
	}
}
