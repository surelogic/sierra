package com.surelogic.sierra.gwt.client.service;

import com.google.gwt.user.client.rpc.RemoteService;
import com.surelogic.sierra.gwt.client.data.ImageMapData;
import com.surelogic.sierra.gwt.client.data.ReportSettings;
import com.surelogic.sierra.gwt.client.data.ReportTable;
import com.surelogic.sierra.gwt.client.data.Result;
import com.surelogic.sierra.gwt.client.data.Ticket;

public interface TicketService extends RemoteService {

	Result<Ticket> getTicket(ReportSettings report);

	/*
	 * Data requests
	 */

	Result<ImageMapData> getImageMap(Ticket ticket);

	Result<ReportTable> getReportTable(Ticket ticket);

	/**
	 * Similar to calling getTicket followed by getReportTable, except that the
	 * ticket is not returned to the portal.
	 * 
	 * @param r
	 * @return
	 */
	Result<ReportTable> getReportTable(ReportSettings r);

	/**
	 * This is a hack to make sure String[] is included in the serialization
	 * policy file
	 * 
	 * @param str
	 *            ignored
	 * @return an empty string array
	 */
	String[] policyHack(String[] str);

}
