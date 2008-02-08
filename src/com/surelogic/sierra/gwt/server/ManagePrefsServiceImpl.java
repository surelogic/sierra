package com.surelogic.sierra.gwt.server;

import java.sql.Connection;

import com.surelogic.sierra.gwt.SierraServiceServlet;
import com.surelogic.sierra.gwt.client.service.ManagePrefsService;
import com.surelogic.sierra.jdbc.server.ConnectionFactory;
import com.surelogic.sierra.jdbc.server.Server;
import com.surelogic.sierra.jdbc.server.UserTransaction;
import com.surelogic.sierra.jdbc.user.ServerUserManager;
import com.surelogic.sierra.jdbc.user.User;

public class ManagePrefsServiceImpl extends SierraServiceServlet implements
		ManagePrefsService {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3252495260597590769L;

	public boolean changePassword(final String oldPassword, final String newPassword) {
		return ConnectionFactory
				.withUserTransaction(new UserTransaction<Boolean>() {

					public Boolean perform(Connection conn, Server server,
							User user) throws Exception {
						return ServerUserManager.getInstance(conn)
								.changeUserPassword(user.getName(),
										oldPassword, newPassword);
					}
				});
	}

}
