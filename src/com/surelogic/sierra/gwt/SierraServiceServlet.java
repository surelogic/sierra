package com.surelogic.sierra.gwt;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.surelogic.sierra.jdbc.server.SecurityHelper;
import com.surelogic.sierra.jdbc.server.UserContext;
import com.surelogic.sierra.jdbc.user.User;

/**
 * 
 * @author nathan
 * 
 */
public abstract class SierraServiceServlet extends RemoteServiceServlet {

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		try {
			UserContext.set((User) req.getSession().getAttribute(SecurityHelper.USER));
			super.service(req, resp);
		} finally {
			UserContext.remove();
		}
	}
}
