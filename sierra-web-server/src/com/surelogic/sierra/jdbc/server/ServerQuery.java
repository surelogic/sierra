package com.surelogic.sierra.jdbc.server;

import com.surelogic.common.jdbc.Query;

public interface ServerQuery<T> {
	T perform(Query q, Server s);
}
