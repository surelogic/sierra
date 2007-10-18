package com.surelogic.sierra.jdbc.server;

import java.sql.Connection;
import java.sql.SQLException;

public class Archiver {

	private final Connection conn;
	
	private Archiver(Connection conn) throws SQLException{
		this.conn = conn;
	}

	public Archiver getInstance(Connection conn) throws SQLException {
		return new Archiver(conn);
	}
	
	
	
}
