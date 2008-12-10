package com.surelogic.sierra.gwt.client.ui.block;

import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.surelogic.sierra.gwt.client.Context;
import com.surelogic.sierra.gwt.client.content.reports.TeamServerReportsContent;
import com.surelogic.sierra.gwt.client.data.ReportSettings;

public abstract class ReportBlock<T extends Widget> extends ContentBlock<T> {
	private ReportSettings report;
	private Label viewReportAction;

	public ReportBlock(final T root) {
		super(root);
		init();
	}

	public ReportBlock(final T root, final ReportSettings report) {
		super(root);
		init();
		setReportSettings(report);
	}

	private void init() {
		viewReportAction = addAction("View Report", new ClickListener() {

			public void onClick(final Widget sender) {
				final Context ctx = new Context();
				ctx.setContent(TeamServerReportsContent.getInstance());
				ctx.setUuid(report.getReportUuid());
				ctx.setParameter("reportSettingsUuid", report.getUuid());
				ctx.submit();
			}
		});
	}

	@Override
	public String getName() {
		return report == null ? null : report.getTitle();
	}

	@Override
	public String getSummary() {
		return report == null ? null : report.getDescription();
	}

	public ReportSettings getReportSettings() {
		return report;
	}

	public void setReportSettings(final ReportSettings report) {
		this.report = report;
		if (report != null) {
			addAction(viewReportAction);
		} else {
			removeAction(viewReportAction);
		}
		reportChanged(report);
		fireRefresh();
	}

	protected abstract void reportChanged(ReportSettings report);

}
