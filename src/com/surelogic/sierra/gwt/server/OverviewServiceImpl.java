package com.surelogic.sierra.gwt.server;

import java.sql.Connection;
import java.util.List;

import com.surelogic.sierra.gwt.SierraServiceServlet;
import com.surelogic.sierra.gwt.client.data.ProjectOverview;
import com.surelogic.sierra.gwt.client.data.UserOverview;
import com.surelogic.sierra.gwt.client.service.OverviewService;
import com.surelogic.sierra.jdbc.server.ConnectionFactory;
import com.surelogic.sierra.jdbc.server.Server;
import com.surelogic.sierra.jdbc.server.UserTransaction;
import com.surelogic.sierra.jdbc.user.User;
import com.surelogic.sierra.portal.PortalOverview;

public class OverviewServiceImpl extends SierraServiceServlet implements
		OverviewService {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1399491183980140077L;

	public List<UserOverview> getUserOverviews() {
		return ConnectionFactory
				.withUserReadOnly(new UserTransaction<List<UserOverview>>() {

					public List<UserOverview> perform(Connection conn,
							Server server, User user) throws Exception {
						conn
								.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
						return PortalOverview.getInstance(conn)
								.getEnabledUserOverviews();
					}
				});
	}

	public List<ProjectOverview> getProjectOverviews() {
		return ConnectionFactory
				.withUserReadOnly(new UserTransaction<List<ProjectOverview>>() {

					public List<ProjectOverview> perform(Connection conn,
							Server server, User user) throws Exception {
						conn
								.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
						return PortalOverview.getInstance(conn)
								.getProjectOverviews();
					}
				});
	}

}
