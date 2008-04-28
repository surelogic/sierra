package com.surelogic.sierra.jdbc.server;

import com.surelogic.common.jdbc.Query;
import com.surelogic.sierra.jdbc.user.User;

public interface UserQuery<T> {
	T perform(Query q, Server s, User u);
}
