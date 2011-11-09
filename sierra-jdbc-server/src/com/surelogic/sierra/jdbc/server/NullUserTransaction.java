package com.surelogic.sierra.jdbc.server;

import java.sql.Connection;

import com.surelogic.sierra.jdbc.user.User;

public abstract class NullUserTransaction implements UserTransaction<Void> {

	public final Void perform(final Connection conn, final Server server,
			final User user) throws Exception {
		doPerform(conn, server, user);
		return null;
	}

	public abstract void doPerform(Connection conn, Server server, User user)
			throws Exception;

}
