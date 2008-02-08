package com.surelogic.sierra.gwt.client.service;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

// TODO probably no need to create the service every time each method is called
// just cache the service instances
public final class ServiceHelper {

	public static SessionServiceAsync getSessionService() {
		// get the session service
		SessionServiceAsync sessionService = (SessionServiceAsync) GWT
				.create(SessionService.class);
		ServiceDefTarget endPoint = (ServiceDefTarget) sessionService;
		endPoint
				.setServiceEntryPoint(GWT.getModuleBaseURL() + "SessionService");
		return sessionService;
	}

	public static ManageServerServiceAsync getManageServerService() {
		ManageServerServiceAsync serverService = (ManageServerServiceAsync) GWT
				.create(ManageServerService.class);
		ServiceDefTarget endpoint = (ServiceDefTarget) serverService;
		endpoint.setServiceEntryPoint(GWT.getModuleBaseURL()
				+ "ManageServerService");
		return serverService;
	}

	public static ManageUserAdminServiceAsync getManageUserService() {
		ManageUserAdminServiceAsync serverService = (ManageUserAdminServiceAsync) GWT
				.create(ManageUserAdminService.class);
		ServiceDefTarget endpoint = (ServiceDefTarget) serverService;
		endpoint.setServiceEntryPoint(GWT.getModuleBaseURL()
				+ "ManageUserService");
		return serverService;
	}

	public static ManagePrefsServiceAsync getManagePrefsService() {
		ManagePrefsServiceAsync serverService = (ManagePrefsServiceAsync) GWT
				.create(ManagePrefsService.class);
		ServiceDefTarget endpoint = (ServiceDefTarget) serverService;
		endpoint.setServiceEntryPoint(GWT.getModuleBaseURL()
				+ "ManagePrefsService");
		return serverService;
	}

	private ServiceHelper() {

	}
}
