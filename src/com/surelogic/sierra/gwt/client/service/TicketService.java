package com.surelogic.sierra.gwt.client.service;

import java.util.Map;

import com.google.gwt.user.client.rpc.RemoteService;
import com.surelogic.sierra.gwt.client.data.Result;
import com.surelogic.sierra.gwt.client.data.Ticket;

public interface TicketService extends RemoteService {

	/**
	 * @gwt.typeArgs args <java.lang.String,java.lang.String>
	 */
	Result getTicket(Map args);
	Result getImageMap(Ticket ticket);

}
