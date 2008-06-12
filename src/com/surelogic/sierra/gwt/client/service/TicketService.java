package com.surelogic.sierra.gwt.client.service;

import java.util.Map;

import com.google.gwt.user.client.rpc.RemoteService;
import com.surelogic.sierra.gwt.client.data.ImageMapData;
import com.surelogic.sierra.gwt.client.data.ReportTable;
import com.surelogic.sierra.gwt.client.data.Result;
import com.surelogic.sierra.gwt.client.data.Ticket;

public interface TicketService extends RemoteService {

	Result<Ticket> getTicket(Map<String, String> args);

	Result<ImageMapData> getImageMap(Ticket ticket);

	Result<ReportTable> getTableData(Ticket ticket);

}
