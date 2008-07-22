package com.surelogic.sierra.gwt.client.content.reports;

import java.util.List;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.surelogic.sierra.gwt.client.chart.Chart;
import com.surelogic.sierra.gwt.client.data.Report;
import com.surelogic.sierra.gwt.client.data.Result;
import com.surelogic.sierra.gwt.client.data.Ticket;
import com.surelogic.sierra.gwt.client.data.Report.Parameter;
import com.surelogic.sierra.gwt.client.service.ServiceHelper;
import com.surelogic.sierra.gwt.client.ui.BlockPanel;

public class ReportView extends BlockPanel {

	VerticalPanel report = new VerticalPanel();

	@Override
	protected void onInitialize(VerticalPanel contentPanel) {
		contentPanel.add(report);
	}

	public void retrieveReport(Report selection) {
		report.clear();
		// retrieve and display the report
		ServiceHelper.getTicketService().getTicket(selection,
				new AsyncCallback<Result<Ticket>>() {

					public void onFailure(Throwable caught) {
						report.add(new Label(caught.toString()));
					}

					public void onSuccess(Result<Ticket> result) {
						if (result.isSuccess()) {
							final Chart chart = new Chart();
							chart.setChartTicket(result.getResult());
							report.add(chart);
						} else {
							report.add(new Label(result.getMessage()));
						}
					}
				});
		final StringBuffer str = new StringBuffer(selection.getName())
				.append(" ");
		str.append(" \"").append(selection.getTitle()).append("\" (");
		for (final Parameter param : selection.getParameters()) {
			str.append(param.getName()).append("=");
			final List<String> values = param.getValues();
			if (values.size() == 1) {
				str.append(values.get(0));
			} else if (values.size() > 1) {
				str.append("[");
				for (final String value : values) {
					str.append(value).append(",");
				}
				str.append("]");
			} else {
				str.append("none");
			}
			str.append(",");
		}
		str.append(")");
		Window.alert("Report: " + str.toString());
	}

}
