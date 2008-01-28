package com.surelogic.sierra.gwt.server.test;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.surelogic.sierra.gwt.client.test.TestService;

public class TestServiceImpl extends RemoteServiceServlet implements
		TestService {
	private static final long serialVersionUID = 1L;

	public String toUpperCase(String text) {
		return text != null ? text.toUpperCase() : null;
	}

}
