package com.surelogic.sierra.gwt.client.chart;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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

	private ChartBuilder(final String name) {
		report.setName(name);
	}

	public ChartBuilder width(final int width) {
		report.getParameters().add(
				new Parameter("width", Integer.toString(width)));
		return this;
	}

	public ChartBuilder height(final int height) {
		report.getParameters().add(
				new Parameter("height", Integer.toString(height)));
		return this;
	}

	public ChartBuilder prop(final String prop, final String value) {
		report.getParameters().add(new Parameter(prop, value));
		return this;
	}

	public ChartBuilder prop(final String prop, final Collection<String> values) {
		final List<String> list = new ArrayList<String>();
		list.addAll(values);
		report.getParameters().add(new Parameter(prop, list));
		return this;
	}

	public Chart build() {
		final Chart chart = new Chart();
		ServiceHelper.getTicketService().getTicket(report,
				new Callback<Ticket>() {

					@Override
					protected void onFailure(final String message,
							final Ticket result) {
						// TODO
					}

					@Override
					protected void onSuccess(final String message,
							final Ticket result) {
						chart.setChartTicket(result);
					}
				});
		return chart;
	}

	public static ChartBuilder name(final String name) {
		return new ChartBuilder(name);
	}

}
