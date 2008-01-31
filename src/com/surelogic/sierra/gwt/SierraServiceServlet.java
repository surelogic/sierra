package com.surelogic.sierra.gwt;

import java.security.Principal;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.surelogic.sierra.jdbc.server.UserTransaction;

/**
 * 
 * @author nathan
 * 
 */
public abstract class SierraServiceServlet extends RemoteServiceServlet {

	protected abstract class WebTransaction<T> implements UserTransaction<T> {

		private final String userName;

		protected WebTransaction() {
			final Principal p = (Principal) getThreadLocalRequest()
					.getSession().getAttribute("SierraUser");
			userName = p == null ? null : p.getName();
		}

		public String getUserName() {
			return userName;
		}

	}
}
