package com.surelogic.sierra.gwt.client.service;

import com.google.gwt.user.client.rpc.RemoteService;
import com.surelogic.sierra.gwt.client.data.ImageMapData;
import com.surelogic.sierra.gwt.client.data.Report;
import com.surelogic.sierra.gwt.client.data.ReportTable;
import com.surelogic.sierra.gwt.client.data.Result;
import com.surelogic.sierra.gwt.client.data.Ticket;

public interface TicketService extends RemoteService {

	Result<Ticket> getTicket(Report report);

	/*
	 * Data requests
	 */

	Result<ImageMapData> getImageMap(Ticket ticket);

	Result<ReportTable> getReportTable(Ticket ticket);

}
