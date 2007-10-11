package com.surelogic.sierra.jdbc;

import java.util.List;

interface ServerContext {

	List<String> getQualifiers();
	
	String getUser();
}
