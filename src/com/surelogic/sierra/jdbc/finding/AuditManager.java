package com.surelogic.sierra.jdbc.finding;

import java.sql.Connection;

public class AuditManager {

	private final Connection conn;

	private AuditManager(Connection conn) {
		this.conn = conn;
	}

	public static AuditManager getInstance(Connection conn) {
		return new AuditManager(conn);
	}
}
