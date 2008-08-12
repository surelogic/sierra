package com.surelogic.sierra.gwt.client.content.reports;

import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.surelogic.sierra.gwt.client.chart.Chart;
import com.surelogic.sierra.gwt.client.data.ReportSettings;
import com.surelogic.sierra.gwt.client.data.Ticket;
import com.surelogic.sierra.gwt.client.service.ServiceHelper;
import com.surelogic.sierra.gwt.client.service.callback.ResultCallback;
import com.surelogic.sierra.gwt.client.ui.BlockPanel;

public class ReportView extends BlockPanel {

	VerticalPanel report = new VerticalPanel();

	@Override
	protected void onInitialize(VerticalPanel contentPanel) {
		contentPanel.add(report);
	}

	public void retrieveReport(ReportSettings selection) {
		report.clear();
		// retrieve and display the report
		ServiceHelper.getTicketService().getTicket(selection,
				new ResultCallback<Ticket>() {

					@Override
					protected void doFailure(String message, Ticket result) {
						report.add(new Label(message));
					}

					@Override
					protected void doSuccess(String message, Ticket result) {
						final Chart chart = new Chart();
						chart.setChartTicket(result);
						report.add(chart);
					}
				});
	}

}
