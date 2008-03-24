package com.surelogic.sierra.gwt.client.service;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

public final class ServiceHelper {

	private ServiceHelper() {
		// no instances
	}

	public static SessionServiceAsync getSessionService() {
		return (SessionServiceAsync) bindService(GWT
				.create(SessionService.class), "SessionService");
	}

	public static ManageServerServiceAsync getManageServerService() {
		return (ManageServerServiceAsync) bindService(GWT
				.create(ManageServerService.class), "ManageServerService");
	}

	public static ManageUserAdminServiceAsync getManageUserService() {
		return (ManageUserAdminServiceAsync) bindService(GWT
				.create(ManageUserAdminService.class), "ManageUserService");
	}

	public static OverviewServiceAsync getOverviewService() {
		return (OverviewServiceAsync) bindService(GWT
				.create(OverviewService.class), "OverviewService");
	}

	public static FindingServiceAsync getFindingService() {
		return (FindingServiceAsync) bindService(GWT
				.create(FindingService.class), "FindingService");
	}

	public static TicketServiceAsync getTicketService() {
		return (TicketServiceAsync) bindService(GWT
				.create(TicketService.class), "TicketService");
	}

	public static SettingsServiceAsync getSettingsService() {
		return (SettingsServiceAsync) bindService(GWT
				.create(SettingsService.class), "SettingsService");
	}
	
	private static Object bindService(Object service, String servletName) {
		ServiceDefTarget endpoint = (ServiceDefTarget) service;
		endpoint.setServiceEntryPoint(GWT.getModuleBaseURL() + "rpc/"
				+ servletName);
		return service;
	}

}
