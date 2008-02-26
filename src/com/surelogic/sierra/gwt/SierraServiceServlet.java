package com.surelogic.sierra.gwt;

import java.io.IOException;
import java.sql.Connection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.surelogic.sierra.jdbc.server.ConnectionFactory;
import com.surelogic.sierra.jdbc.server.SecurityHelper;
import com.surelogic.sierra.jdbc.server.Server;
import com.surelogic.sierra.jdbc.server.UserContext;
import com.surelogic.sierra.jdbc.server.UserTransaction;
import com.surelogic.sierra.jdbc.user.ServerUserManager;
import com.surelogic.sierra.jdbc.user.SierraGroup;
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
			UserContext.set((User) req.getSession().getAttribute(
					SecurityHelper.USER));
			super.service(req, resp);
		} finally {
			UserContext.remove();
		}
	}

	// TODO probably should get this into ServerConnection in some form
	protected <T> T performAdmin(boolean readOnly, final UserTransaction<T> t) {
		UserTransaction<T> adminTrans = new UserTransaction<T>() {

			public T perform(Connection conn, Server server, User user)
					throws Exception {
				final ServerUserManager man = ServerUserManager
						.getInstance(conn);
				if (man.isUserInGroup(user.getName(), SierraGroup.ADMIN
						.getName())) {
					return t.perform(conn, server, user);
				} else {
					return null;
				}
			}
		};
		if (readOnly) {
			return ConnectionFactory.withUserReadOnly(adminTrans);
		}
		return ConnectionFactory.withUserTransaction(adminTrans);
	}
}
