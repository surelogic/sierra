package com.surelogic.sierra.gwt.client.chart;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.LoadListener;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.surelogic.sierra.gwt.client.data.ImageMapData;
import com.surelogic.sierra.gwt.client.data.Ticket;
import com.surelogic.sierra.gwt.client.service.ServiceHelper;
import com.surelogic.sierra.gwt.client.service.callback.ResultCallback;
import com.surelogic.sierra.gwt.client.ui.ImageHelper;

public class Chart extends Composite {
	private final VerticalPanel rootPanel = new VerticalPanel();
	private final Image waitImage = ImageHelper.getWaitImage(16);

	public Chart() {
		initWidget(rootPanel);
		rootPanel.add(waitImage);
	}

	public void setChartTicket(final Ticket ticket) {
		if (ticket == null) {
			rootPanel.clear();
			rootPanel.add(new Label("No Chart Available"));
		} else {
			ServiceHelper.getTicketService().getImageMap(ticket,
					new ResultCallback<ImageMapData>() {

						@Override
						protected void doFailure(String message,
								ImageMapData result) {
							loadFailed(message);
						}

						@Override
						protected void doSuccess(String message,
								ImageMapData result) {
							loadChart(ticket, result);
						}

					});
		}
	}

	private void loadChart(final Ticket ticket, ImageMapData mapData) {
		final String chartId = ticket.getUUID();
		final String url = "chart/png?ticket=" + chartId;
		final String map = "#map" + chartId;

		final MappedImage chartImg = new MappedImage(url, map);
		chartImg.addLoadListener(new LoadListener() {

			public void onError(Widget sender) {
				loadFailed(null);
			}

			public void onLoad(Widget sender) {
				rootPanel.remove(waitImage);
			}
		});

		rootPanel.insert(chartImg, 0);
	}

	private void loadFailed(String message) {
		rootPanel.clear();
		rootPanel.add(new Label("Unable To Load Chart"));
		if (message != null && !"".equals(message)) {
			rootPanel.add(new Label(message));
		}
	}
}
