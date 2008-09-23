package com.surelogic.sierra.gwt.client.content.reports;

import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.surelogic.sierra.gwt.client.data.ReportSettings;
import com.surelogic.sierra.gwt.client.data.Ticket;
import com.surelogic.sierra.gwt.client.service.ServiceHelper;
import com.surelogic.sierra.gwt.client.service.callback.ResultCallback;
import com.surelogic.sierra.gwt.client.ui.block.ContentBlockPanel;
import com.surelogic.sierra.gwt.client.ui.block.ReportTableBlock;
import com.surelogic.sierra.gwt.client.ui.chart.Chart;
import com.surelogic.sierra.gwt.client.ui.panel.BlockPanel;

public class ReportView extends BlockPanel {

	VerticalPanel report = new VerticalPanel();

	@Override
	protected void onInitialize(final VerticalPanel contentPanel) {
		contentPanel.add(report);
	}

	public void clear() {
		report.clear();
	}

	public void retrieveChart(final ReportSettings selection) {
		report.clear();
		// retrieve and display the report
		ServiceHelper.getTicketService().getTicket(selection,
				new ResultCallback<Ticket>() {

					@Override
					protected void doFailure(final String message,
							final Ticket result) {
						report.add(new Label(message));
					}

					@Override
					protected void doSuccess(final String message,
							final Ticket result) {
						final Chart chart = new Chart();
						chart.setChartTicket(result);
						report.add(chart);
					}
				});
	}

	public void retrieveTable(final ReportSettings selection) {
		report.clear();
		// retrieve and display the report
		report.add(new ContentBlockPanel(new ReportTableBlock(selection)));
	}
}
