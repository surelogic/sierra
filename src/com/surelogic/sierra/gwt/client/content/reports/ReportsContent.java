package com.surelogic.sierra.gwt.client.content.reports;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.surelogic.sierra.gwt.client.ListContentComposite;
import com.surelogic.sierra.gwt.client.data.Report;
import com.surelogic.sierra.gwt.client.util.LangUtil;

public class ReportsContent extends ListContentComposite<Report, ReportCache> {
	private static final ReportsContent instance = new ReportsContent();
	private final ReportParametersView reportParamsView = new ReportParametersView();
	private final ReportView reportView = new ReportView();

	public static ReportsContent getInstance() {
		return instance;
	}

	private ReportsContent() {
		super(new ReportCache());
		// singleton
	}

	@Override
	protected void onInitialize(DockPanel rootPanel,
			VerticalPanel selectionPanel) {
		setCaption("Reports");
		reportView.initialize();
		reportParamsView.initialize();
		reportParamsView.addReportAction("Show Table", new ClickListener() {

			public void onClick(Widget sender) {
				final VerticalPanel selectionPanel = getSelectionPanel();
				if (selectionPanel.getWidgetIndex(reportView) == -1) {
					selectionPanel.add(reportView);
				}
				reportView.retrieveReport(reportParamsView.getUpdatedReport());
			}
		});
		reportParamsView.addReportAction("Show Chart", new ClickListener() {

			public void onClick(Widget sender) {
				final VerticalPanel selectionPanel = getSelectionPanel();
				if (selectionPanel.getWidgetIndex(reportView) == -1) {
					selectionPanel.add(reportView);
				}
				reportView.retrieveReport(reportParamsView.getUpdatedReport());
			}
		});
		reportParamsView.addReportAction("Show on Dashboard",
				new ClickListener() {

					public void onClick(Widget sender) {
						Window.alert("TODO: Show on Dashboard");
					}

				});
		reportParamsView.addReportAction("Export to PDF", new ClickListener() {

			public void onClick(Widget sender) {
				// TODO Auto-generated method stub
				Window.alert("TODO: Export to PDF");
			}
		});

		selectionPanel.add(reportParamsView);
		selectionPanel.add(reportView);
	}

	@Override
	protected void onSelectionChanged(Report item) {
		reportParamsView.setSelection(item);
	}

	@Override
	protected String getItemText(Report item) {
		return item.getTitle();
	}

	@Override
	protected boolean isMatch(Report item, String query) {
		return LangUtil.containsIgnoreCase(item.getTitle(), query);
	}

}
