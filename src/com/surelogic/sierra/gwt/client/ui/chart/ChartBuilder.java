package com.surelogic.sierra.gwt.client.ui.chart;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.surelogic.sierra.gwt.client.data.Report;
import com.surelogic.sierra.gwt.client.data.ReportSettings;
import com.surelogic.sierra.gwt.client.data.Ticket;
import com.surelogic.sierra.gwt.client.service.ServiceHelper;
import com.surelogic.sierra.gwt.client.service.callback.ResultCallback;

/**
 * Helper class for constructing charts to be displayed in the portal.
 * 
 * @author nathan
 * 
 */
public final class ChartBuilder {

	private final ReportSettings settings = new ReportSettings();

	public static ChartBuilder report(final Report report, final String title,
			final String description) {
		return new ChartBuilder(report, title, description);
	}

	public static ChartBuilder report(final Report report) {
		return new ChartBuilder(report, report.getTitle(), report
				.getDescription());
	}

	private ChartBuilder(final Report report, final String title,
			final String description) {
		settings.setReport(report);
		settings.setTitle(title);
		settings.setDescription(description);
	}

	public ChartBuilder width(final int width) {
		settings.setSettingValue("width", Integer.toString(width));
		return this;
	}

	public ChartBuilder height(final int height) {
		settings.setSettingValue("height", Integer.toString(height));
		return this;
	}

	public ChartBuilder prop(final String prop, final String value) {
		settings.setSettingValue(prop, value);
		return this;
	}

	public ChartBuilder prop(final String prop, final Collection<String> values) {
		final List<String> list = new ArrayList<String>();
		list.addAll(values);
		settings.setSettingValue(prop, list);
		return this;
	}

	public Chart build() {
		final Chart chart = new Chart();
		ServiceHelper.getTicketService().getTicket(settings,
				new ResultCallback<Ticket>() {

					@Override
					protected void doFailure(final String message,
							final Ticket result) {
						// TODO
					}

					@Override
					protected void doSuccess(final String message,
							final Ticket result) {
						chart.setChartTicket(result);
					}
				});
		return chart;
	}

}
