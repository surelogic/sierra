package com.surelogic.sierra.gwt.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

public class ServiceHelper {

	public static SessionServiceAsync getSessionService() {
		// get the session service
		SessionServiceAsync sessionService = (SessionServiceAsync) GWT
				.create(SessionService.class);
		ServiceDefTarget sessionEndPoint = (ServiceDefTarget) sessionService;
		sessionEndPoint.setServiceEntryPoint(GWT.getModuleBaseURL()
				+ "SessionService");
		return sessionService;
	}
	
	private ServiceHelper() {
		
	}
}
