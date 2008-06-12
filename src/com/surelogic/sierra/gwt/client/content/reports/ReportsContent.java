package com.surelogic.sierra.gwt.client.content.reports;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.surelogic.sierra.gwt.client.ContentComposite;
import com.surelogic.sierra.gwt.client.Context;
import com.surelogic.sierra.gwt.client.ContextManager;
import com.surelogic.sierra.gwt.client.data.Cache;
import com.surelogic.sierra.gwt.client.data.CacheListenerAdapter;
import com.surelogic.sierra.gwt.client.data.Report;
import com.surelogic.sierra.gwt.client.util.LangUtil;

public class ReportsContent extends ContentComposite {
	private static final ReportsContent instance = new ReportsContent();
	private final ReportsListView reportsListView = new ReportsListView();
	private final VerticalPanel selectionPanel = new VerticalPanel();
	private final ReportParametersView reportParamsView = new ReportParametersView();
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

		reportsListView.initialize();
		rootPanel.add(reportsListView, DockPanel.WEST);
		rootPanel.setCellWidth(reportsListView, "25%");

		reportView.initialize();

		reportParamsView.initialize();
		reportParamsView.addReportAction("Show Report", new ClickListener() {

			public void onClick(Widget sender) {
				if (selectionPanel.getWidgetIndex(reportView) == -1) {
					selectionPanel.add(reportView);
				}
				reportView.retrieveReport(reportParamsView.getSelection(),
						reportParamsView.getParameters());
			}
		});
		reportParamsView.addReportAction("Export to PDF", new ClickListener() {

			public void onClick(Widget sender) {
				// TODO Auto-generated method stub
				Window.alert("Export to PDF");
			}
		});

		selectionPanel.setWidth("100%");
		selectionPanel.add(reportParamsView);
		rootPanel.add(selectionPanel, DockPanel.CENTER);
		rootPanel.setCellWidth(selectionPanel, "75%");

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
		final String reportUuid = context.getUuid();
		reportsListView.updateReports(reports);
		if (LangUtil.notEmpty(reportUuid)) {
			final Report report = reports.getItem(reportUuid);
			if (report != null) {
				reportsListView.setSelection(report);
				reportParamsView.setSelection(report);
			} else {
				reportParamsView.setSelection(null);
			}
		} else {
			if (reports.getItemCount() > 0) {
				Context.createWithUuid(reports.getItem(0)).submit();
			}
		}
	}
}
