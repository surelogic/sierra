package com.surelogic.sierra.jdbc.settings;

import com.surelogic.common.jdbc.Row;
import com.surelogic.common.jdbc.RowHandler;
import com.surelogic.sierra.tool.message.ServerRevision;

public class ServerRevisionRowHandler implements RowHandler<ServerRevision> {

	public ServerRevision handle(Row r) {
		final ServerRevision s = new ServerRevision();
		s.setServer(r.nextString());
		s.setRevision(r.nextLong());
		return s;
	}

}
