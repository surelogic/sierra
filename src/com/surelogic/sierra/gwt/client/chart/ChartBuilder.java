package com.surelogic.sierra.gwt.client.chart;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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

	private final ReportSettings report = new ReportSettings();

	public static ChartBuilder report(final String reportUuid, final String title,
			final String description) {
		return new ChartBuilder(reportUuid, title, description);
	}

	private ChartBuilder(final String reportUuid, final String title,
			final String description) {
		report.setReportUuid(reportUuid);
		report.setTitle(title);
		report.setDescription(description);
	}

	public ChartBuilder width(final int width) {
		report.setSettingValue("width", Integer.toString(width));
		return this;
	}

	public ChartBuilder height(final int height) {
		report.setSettingValue("height", Integer.toString(height));
		return this;
	}

	public ChartBuilder prop(final String prop, final String value) {
		report.setSettingValue(prop, value);
		return this;
	}

	public ChartBuilder prop(final String prop, final Collection<String> values) {
		final List<String> list = new ArrayList<String>();
		list.addAll(values);
		report.setSettingValue(prop, list);
		return this;
	}

	public Chart build() {
		final Chart chart = new Chart();
		ServiceHelper.getTicketService().getTicket(report,
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
