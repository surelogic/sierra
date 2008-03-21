package com.surelogic.sierra.gwt.client.ui;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.surelogic.sierra.gwt.client.data.ImageMapData;
import com.surelogic.sierra.gwt.client.data.Ticket;
import com.surelogic.sierra.gwt.client.service.Callback;
import com.surelogic.sierra.gwt.client.service.ServiceHelper;

public class Chart extends Composite {

	private final HTML html = new HTML();

	public Chart() {
		initWidget(html);
	}

	public void setChartTicket(final Ticket ticket) {
		ServiceHelper.getTicketService().getImageMap(ticket, new Callback() {

			protected void onFailure(String message, Object result) {
				// TODO failure
			}

			protected void onSuccess(String message, Object result) {
				html.setHTML(((ImageMapData) result).getData()
						+ "<img src=\"chart/png?ticket=" + ticket.getUUID()
						+ "\" usemap=\"#map" + ticket.getUUID() + "\" />");
			}
		});
	}
}
