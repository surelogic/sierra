package com.surelogic.sierra.tool.message;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.surelogic.sierra.jdbc.server.UserContext;
import com.surelogic.sierra.jdbc.user.User;
import com.surelogic.sierra.message.srpc.SRPCServlet;

public abstract class SecureServiceServlet extends SRPCServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -664892243722651966L;

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		try {
			UserContext.set((User) req.getAttribute("SierraUser"));
			super.service(req, resp);
		} finally {
			UserContext.remove();
		}
	}
}
