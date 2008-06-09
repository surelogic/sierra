package com.surelogic.sierra.gwt.client.service;

import java.util.Map;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.surelogic.sierra.gwt.client.data.ImageMapData;
import com.surelogic.sierra.gwt.client.data.Result;
import com.surelogic.sierra.gwt.client.data.Ticket;

public interface TicketServiceAsync {
	void getTicket(Map<String, String> args,
			AsyncCallback<Result<Ticket>> callback);

	void getImageMap(Ticket ticket, AsyncCallback<Result<ImageMapData>> callback);
}
