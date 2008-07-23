package com.surelogic.sierra.jdbc.server;

import java.sql.Connection;

public abstract class NullServerTransaction implements ServerTransaction<Void> {

	public final Void perform(final Connection conn, final Server server)
			throws Exception {
		doPerform(conn, server);
		return null;
	}

	public abstract void doPerform(Connection conn, Server server)
			throws Exception;
}
