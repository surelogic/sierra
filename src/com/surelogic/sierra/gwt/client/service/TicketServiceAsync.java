package com.surelogic.sierra.gwt.client.service;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.surelogic.sierra.gwt.client.data.ImageMapData;
import com.surelogic.sierra.gwt.client.data.Report;
import com.surelogic.sierra.gwt.client.data.ReportTable;
import com.surelogic.sierra.gwt.client.data.Result;
import com.surelogic.sierra.gwt.client.data.Ticket;

public interface TicketServiceAsync {

	void getTicket(Report report, AsyncCallback<Result<Ticket>> callback);

	void getImageMap(Ticket ticket, AsyncCallback<Result<ImageMapData>> callback);

	void getReportTable(Ticket ticket,
			AsyncCallback<Result<ReportTable>> callback);

	void getReportTable(Report r,
			AsyncCallback<Result<ReportTable>> asyncCallback);

}
