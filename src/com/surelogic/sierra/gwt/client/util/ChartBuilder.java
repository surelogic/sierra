package com.surelogic.sierra.gwt.client.util;

import com.surelogic.sierra.gwt.client.data.Report;
import com.surelogic.sierra.gwt.client.data.Ticket;
import com.surelogic.sierra.gwt.client.data.Report.Parameter;
import com.surelogic.sierra.gwt.client.service.Callback;
import com.surelogic.sierra.gwt.client.service.ServiceHelper;
import com.surelogic.sierra.gwt.client.ui.Chart;

/**
 * Helper class for constructing charts to be displayed in the portal.
 * 
 * @author nathan
 * 
 */
public final class ChartBuilder {

	private final Report report = new Report();

	private ChartBuilder(String name) {
		report.setName(name);
	}

	public ChartBuilder width(int width) {
		report.getParameters().add(
				new Parameter("width", Integer.toString(width)));
		return this;
	}

	public ChartBuilder height(int height) {
		report.getParameters().add(
				new Parameter("height", Integer.toString(height)));
		return this;
	}

	public ChartBuilder prop(String prop, String value) {
		report.getParameters().add(new Parameter(prop, value));
		return this;
	}

	public Chart build() {
		final Chart chart = new Chart();
		ServiceHelper.getTicketService().getTicket(report,
				new Callback<Ticket>() {

					@Override
					protected void onFailure(String message, Ticket result) {
						// TODO
					}

					@Override
					protected void onSuccess(String message, Ticket result) {
						chart.setChartTicket(result);
					}
				});
		return chart;
	}

	public static ChartBuilder name(String name) {
		return new ChartBuilder(name);
	}

}
