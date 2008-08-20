package com.surelogic.sierra.jdbc.server;

import com.surelogic.common.jdbc.Query;
import com.surelogic.sierra.jdbc.user.User;

public abstract class NullUserQuery implements UserQuery<Void> {

	public Void perform(final Query query, final Server server, final User user) {
		doPerform(query, server, user);
		return null;
	}

	public abstract void doPerform(final Query query, final Server server,
			final User user);

}
