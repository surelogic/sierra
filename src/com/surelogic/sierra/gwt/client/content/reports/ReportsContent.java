package com.surelogic.sierra.gwt.client.content.reports;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.PopupListener;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.surelogic.sierra.gwt.client.content.ListContentComposite;
import com.surelogic.sierra.gwt.client.data.Report;
import com.surelogic.sierra.gwt.client.data.Status;
import com.surelogic.sierra.gwt.client.data.Report.OutputType;
import com.surelogic.sierra.gwt.client.data.cache.ReportCache;
import com.surelogic.sierra.gwt.client.util.LangUtil;

public abstract class ReportsContent extends
		ListContentComposite<Report, ReportCache> {
	private final ReportParametersView reportParamsView = new ReportParametersView();
	private final ReportView reportView = new ReportView();

	public ReportsContent() {
		super(ReportCache.getInstance());
	}

	@Override
	protected void onInitialize(DockPanel rootPanel,
			VerticalPanel selectionPanel) {
		setCaption("Reports");
		reportView.initialize();
		reportParamsView.initialize();
		reportParamsView.addReportAction("Show Table", OutputType.TABLE,
				new ClickListener() {

					public void onClick(Widget sender) {
						final VerticalPanel selectionPanel = getSelectionPanel();
						if (selectionPanel.getWidgetIndex(reportView) == -1) {
							selectionPanel.add(reportView);
						}
						reportView.retrieveReport(reportParamsView
								.getUpdatedReport());
					}
				});

		reportParamsView.addReportAction("Show Chart", OutputType.CHART,
				new ClickListener() {

					public void onClick(Widget sender) {
						final VerticalPanel selectionPanel = getSelectionPanel();
						if (selectionPanel.getWidgetIndex(reportView) == -1) {
							selectionPanel.add(reportView);
						}
						reportView.retrieveReport(reportParamsView
								.getUpdatedReport());
					}
				});

		reportParamsView.addReportAction("Export to PDF", OutputType.PDF,
				new ClickListener() {

					public void onClick(Widget sender) {
						// TODO Auto-generated method stub
						Window.alert("TODO: Export to PDF");
					}
				});

		reportParamsView.addReportAction("Save Report", null,
				new ClickListener() {

					public void onClick(Widget sender) {
						saveReportConfig(reportParamsView.getUpdatedReport());
					}

				});

		selectionPanel.add(reportParamsView);
		selectionPanel.add(reportView);
	}

	private void saveReportConfig(Report updatedReport) {
		final SaveReportDialog dialog = new SaveReportDialog();
		dialog.addPopupListener(new PopupListener() {

			public void onPopupClosed(final PopupPanel sender,
					final boolean autoClosed) {
				final Status dialogStatus = dialog.getStatus();
				if (dialogStatus != null && dialogStatus.isSuccess()) {
					final String reportName = dialog.getName();
					if (reportName.length() > 0) {
						// TODO save the report

						Window.alert("TODO: Save the report");
					}
				}
			}
		});
		dialog.center();
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
	protected boolean isItemVisible(Report item, String query) {
		if (getDataSource() == item.getDataSource()) {
			return LangUtil.containsIgnoreCase(item.getTitle(), query);
		}
		return false;
	}

	protected abstract Report.DataSource getDataSource();

}
