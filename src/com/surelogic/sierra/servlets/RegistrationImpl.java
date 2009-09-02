package com.surelogic.sierra.servlets;

import com.surelogic.sierra.message.srpc.MultiPartSRPCServlet;
import com.surelogic.sierra.tool.registration.ProductInfo;
import com.surelogic.sierra.tool.registration.ProductRegistrationInfo;
import com.surelogic.sierra.tool.registration.Registration;
import com.surelogic.sierra.tool.registration.RegistrationResponse;

/**
 * Implementation of {@link Registration}.
 * 
 * @author Edwin Chan
 */
public class RegistrationImpl extends MultiPartSRPCServlet implements
		Registration {
	private static final long serialVersionUID = -1444232644355354435L;

	public RegistrationResponse register(final ProductRegistrationInfo info) {
		final RegistrationResponse r = new RegistrationResponse();
		r.setMessage("Registered " + info.getName() + " v." + info.getVersion()
				+ " for " + info.getFirstName() + " " + info.getLastName()
				+ " successfully");
		return r;
	}

	public ProductInfo checkForUpdates(final ProductInfo info) {
		info.setVersion("Unknown");
		return info;
	}
}
