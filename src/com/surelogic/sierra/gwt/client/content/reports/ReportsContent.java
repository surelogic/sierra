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
import com.surelogic.sierra.gwt.client.data.ReportSettings;
import com.surelogic.sierra.gwt.client.data.Status;
import com.surelogic.sierra.gwt.client.data.Report.OutputType;
import com.surelogic.sierra.gwt.client.data.cache.ReportCache;
import com.surelogic.sierra.gwt.client.service.ServiceHelper;
import com.surelogic.sierra.gwt.client.service.callback.StatusCallback;
import com.surelogic.sierra.gwt.client.util.LangUtil;

public abstract class ReportsContent extends
		ListContentComposite<Report, ReportCache> {
	private final ReportParametersView reportParamsView = new ReportParametersView();
	private final ReportView reportView = new ReportView();

	public ReportsContent() {
		super(ReportCache.getInstance());
	}

	@Override
	protected void onInitialize(final DockPanel rootPanel,
			final VerticalPanel selectionPanel) {
		setCaption("Reports");
		reportView.initialize();
		reportParamsView.initialize();
		reportParamsView.addReportAction("Show Table", OutputType.TABLE,
				new ClickListener() {

					public void onClick(final Widget sender) {
						final VerticalPanel selectionPanel = getSelectionPanel();
						if (selectionPanel.getWidgetIndex(reportView) == -1) {
							selectionPanel.add(reportView);
						}
						reportView.retrieveTable(reportParamsView
								.getReportSettings());
					}
				});

		reportParamsView.addReportAction("Show Chart", OutputType.CHART,
				new ClickListener() {
					public void onClick(final Widget sender) {
						final VerticalPanel selectionPanel = getSelectionPanel();
						if (selectionPanel.getWidgetIndex(reportView) == -1) {
							selectionPanel.add(reportView);
						}
						reportView.retrieveChart(reportParamsView
								.getReportSettings());
					}
				});

		reportParamsView.addReportAction("Export to PDF", OutputType.PDF,
				new ClickListener() {

					public void onClick(final Widget sender) {
						// TODO Auto-generated method stub
						Window.alert("TODO: Export to PDF");
					}
				});

		reportParamsView.addReportAction("Save Report", null,
				new ClickListener() {

					public void onClick(final Widget sender) {
						saveReportConfig(reportParamsView.getReportSettings());
					}

				});

		selectionPanel.add(reportParamsView);
		selectionPanel.add(reportView);
	}

	private void saveReportConfig(final ReportSettings settings) {
		final SaveReportDialog dialog = new SaveReportDialog();
		dialog.addPopupListener(new PopupListener() {

			public void onPopupClosed(final PopupPanel sender,
					final boolean autoClosed) {
				final Status dialogStatus = dialog.getStatus();
				if ((dialogStatus != null) && dialogStatus.isSuccess()) {
					final String reportName = dialog.getName();
					if (reportName.length() > 0) {
						settings.setTitle(reportName);
						ServiceHelper.getSettingsService().saveReportSettings(
								settings, new StatusCallback() {
									@Override
									protected void doStatus(final Status status) {
										getCache().refresh();
									}
								});
					}
				}
			}
		});
		dialog.center();
	}

	@Override
	protected void onSelectionChanged(final Report item) {
		reportParamsView.setSelection(item);
	}

	@Override
	protected String getItemText(final Report item) {
		return item.getTitle();
	}

	@Override
	protected boolean isItemVisible(final Report item, final String query) {
		if (getDataSource() == item.getDataSource()) {
			return LangUtil.containsIgnoreCase(item.getTitle(), query);
		}
		return false;
	}

	protected abstract Report.DataSource getDataSource();

}
