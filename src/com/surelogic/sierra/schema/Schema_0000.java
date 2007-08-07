package com.surelogic.sierra.schema;

import java.sql.Connection;
import java.sql.SQLException;

import com.surelogic.sierra.findbugs.FindBugsToolInfoGenerator;
import com.surelogic.sierra.pmd.PMDToolInfoGenerator;

public class Schema_0000 implements Runnable {

	private final Connection conn;

	public Schema_0000(Connection conn) {
		this.conn = conn;
	}

	public void run() {
		PMDToolInfoGenerator.generateTool(conn);
		FindBugsToolInfoGenerator.generateTool(conn);
		try {
			conn.commit();
		} catch (SQLException e) {
			throw new IllegalStateException(e);
		}
	}

}
