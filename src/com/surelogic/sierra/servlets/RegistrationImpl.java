package com.surelogic.sierra.servlets;

import com.surelogic.sierra.message.srpc.*;
import com.surelogic.sierra.tool.registration.*;

/**
 * Implementation of {@link Registration}.
 * 
 * @author Edwin Chan
 */
public class RegistrationImpl extends SRPCServlet implements Registration {
	private static final long serialVersionUID = -1444232644355354435L;

	public RegistrationResponse register(ProductRegistrationInfo info) {
		RegistrationResponse r = new RegistrationResponse();
		r.setMessage("Registered "+info.getName()+" v."+info.getVersion()+" for "+
				     info.getFirstName()+" "+info.getLastName()+" successfully");
		return r;
	}
	

	public ProductInfo checkForUpdates(ProductInfo info) {
		info.setVersion("Unknown");
		return info;
	}
}
