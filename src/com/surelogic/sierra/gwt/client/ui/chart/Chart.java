package com.surelogic.sierra.gwt.client.ui.chart;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.LoadListener;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.surelogic.sierra.gwt.client.data.ImageMapData;
import com.surelogic.sierra.gwt.client.data.ReportSettings;
import com.surelogic.sierra.gwt.client.data.Ticket;
import com.surelogic.sierra.gwt.client.service.ServiceHelper;
import com.surelogic.sierra.gwt.client.service.callback.ResultCallback;
import com.surelogic.sierra.gwt.client.ui.ImageHelper;

public final class Chart extends Composite {
	private final VerticalPanel rootPanel = new VerticalPanel();
	private final Image waitImage = ImageHelper.getWaitImage(16);

	public Chart() {
		initWidget(rootPanel);
		rootPanel.add(waitImage);
	}

	public Chart(final ReportSettings settings) {
		initWidget(rootPanel);
		rootPanel.add(waitImage);
		setReportSettings(settings);
	}

	public Chart(final Ticket ticket) {
		initWidget(rootPanel);
		rootPanel.add(waitImage);
		setChartTicket(ticket);
	}

	public void setReportSettings(final ReportSettings settings) {
		ServiceHelper.getTicketService().getTicket(settings,
				new ResultCallback<Ticket>() {

					@Override
					protected void doFailure(final String message,
							final Ticket result) {
						rootPanel.clear();
						rootPanel.add(new Label(message));
					}

					@Override
					protected void doSuccess(final String message,
							final Ticket result) {
						setChartTicket(result);
					}
				});
	}

	public void setChartTicket(final Ticket ticket) {
		if (ticket == null) {
			rootPanel.clear();
			rootPanel.add(new Label("No Chart Available"));
		} else {
			ServiceHelper.getTicketService().getImageMap(ticket,
					new ResultCallback<ImageMapData>() {

						@Override
						protected void doFailure(final String message,
								final ImageMapData result) {
							setErrorMessage(message);
						}

						@Override
						protected void doSuccess(final String message,
								final ImageMapData result) {
							loadChart(ticket, result);
						}

					});
		}
	}

	public void setErrorMessage(final String message) {
		rootPanel.clear();
		rootPanel.add(new Label("Unable To Load Chart"));
		if (message != null && !"".equals(message)) {
			rootPanel.add(new Label(message));
		}
	}

	private void loadChart(final Ticket ticket, final ImageMapData mapData) {
		final String chartId = ticket.getUUID();
		final String url = "chart/png?ticket=" + chartId;
		final String map = "#map" + chartId;

		final MappedImage chartImg = new MappedImage(url, map);
		chartImg.addLoadListener(new LoadListener() {

			public void onError(final Widget sender) {
				setErrorMessage(null);
			}

			public void onLoad(final Widget sender) {
				rootPanel.remove(waitImage);
			}
		});

		rootPanel.insert(chartImg, 0);
	}

}
