package com.surelogic.sierra.gwt.client.ui.block;

import com.google.gwt.user.client.ui.Widget;
import com.surelogic.sierra.gwt.client.data.ReportSettings;

public abstract class ReportBlock<T extends Widget> extends ContentBlock<T> {
	private ReportSettings report;

	public ReportBlock(final T root) {
		super(root);
	}

	public ReportBlock(final T root, final ReportSettings report) {
		super(root);
		setReportSettings(report);
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
		reportChanged(report);
		fireRefresh();
	}

	protected abstract void reportChanged(ReportSettings report);
}
