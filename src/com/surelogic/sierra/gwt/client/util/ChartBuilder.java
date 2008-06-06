package com.surelogic.sierra.gwt.client.util;

import java.util.HashMap;
import java.util.Map;

import com.surelogic.sierra.gwt.client.data.Ticket;
import com.surelogic.sierra.gwt.client.service.Callback;
import com.surelogic.sierra.gwt.client.service.ServiceHelper;
import com.surelogic.sierra.gwt.client.ui.Chart;

public final class ChartBuilder {

	private final Map<String, String> parameters = new HashMap<String, String>();

	private ChartBuilder(String name) {
		parameters.put("type", name);
	}

	public ChartBuilder width(int width) {
		parameters.put("width", Integer.toString(width));
		return this;
	}

	public ChartBuilder height(int height) {
		parameters.put("height", Integer.toString(height));
		return this;
	}

	public ChartBuilder prop(String prop, String value) {
		parameters.put(prop, value);
		return this;
	}

	public Chart build() {
		final Chart chart = new Chart();
		ServiceHelper.getTicketService().getTicket(parameters,
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
