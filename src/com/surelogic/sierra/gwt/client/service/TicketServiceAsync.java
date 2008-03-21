package com.surelogic.sierra.gwt.client.service;

import java.util.Map;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.surelogic.sierra.gwt.client.data.Ticket;

public interface TicketServiceAsync {
	void getTicket(Map args, AsyncCallback callback);

	void getImageMap(Ticket ticket, AsyncCallback callback);
}
