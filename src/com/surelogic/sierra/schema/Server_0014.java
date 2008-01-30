package com.surelogic.sierra.schema;

import java.sql.Connection;
import java.sql.SQLException;

import com.surelogic.common.jdbc.SchemaAction;
import com.surelogic.sierra.jdbc.user.ServerUserManager;
import com.surelogic.sierra.jdbc.user.SierraGroup;

public class Server_0014 implements SchemaAction {

	public void run(Connection c) throws SQLException {
		final ServerUserManager man = ServerUserManager.getInstance(c);
		for (SierraGroup g : SierraGroup.values()) {
			man.createGroup(g.getName(), g.getDescription());
		}
		man.createUser("sierra", "sierra");
		man.addUserToGroup("sierra", SierraGroup.ADMIN.getName());
	}

}
