package com.surelogic.sierra.gwt.client.reports;

import com.google.gwt.user.client.ui.DockPanel;
import com.surelogic.sierra.gwt.client.ContentComposite;
import com.surelogic.sierra.gwt.client.Context;
import com.surelogic.sierra.gwt.client.ContextManager;
import com.surelogic.sierra.gwt.client.data.Cache;
import com.surelogic.sierra.gwt.client.data.CacheListenerAdapter;
import com.surelogic.sierra.gwt.client.data.Report;
import com.surelogic.sierra.gwt.client.util.LangUtil;

public class ReportsContent extends ContentComposite {
	private static final ReportsContent instance = new ReportsContent();
	private final ReportsListView reportsView = new ReportsListView();
	private final ReportView reportView = new ReportView();
	private final ReportCache reports = new ReportCache();

	public static ReportsContent getInstance() {
		return instance;
	}

	private ReportsContent() {
		super();
		// singleton
	}

	@Override
	protected void onInitialize(DockPanel rootPanel) {
		setCaption("Reports");

		reportsView.initialize();
		rootPanel.add(reportsView, DockPanel.WEST);
		rootPanel.setCellWidth(reportsView, "25%");

		reportView.initialize();
		rootPanel.add(reportView, DockPanel.CENTER);
		rootPanel.setCellWidth(reportView, "75%");

		reports.addListener(new CacheListenerAdapter<Report>() {

			@Override
			public void onRefresh(Cache<Report> cache, Throwable failure) {
				refreshContext(ContextManager.getContext());
			}

		});
	}

	@Override
	protected void onDeactivate() {
		// nothing to do
	}

	@Override
	protected void onUpdate(Context context) {
		if (!isActive()) {
			reports.refresh();
		} else {
			refreshContext(context);
		}
	}

	private void refreshContext(Context context) {
		final ReportsContext reportsCtx = new ReportsContext(context);
		final String reportUuid = reportsCtx.getReport();
		reportsView.updateReports(reports);
		if (LangUtil.notEmpty(reportUuid)) {
			final Report report = reports.getItem(reportUuid);
			if (report != null) {
				reportsView.setSelection(report);
				reportView.setSelection(report);
			} else {
				reportView.setSelection(null);
			}
		} else {
			if (reports.getItemCount() > 0) {
				new ReportsContext(reports.getItem(0)).updateContext();
			}
		}
	}
}
