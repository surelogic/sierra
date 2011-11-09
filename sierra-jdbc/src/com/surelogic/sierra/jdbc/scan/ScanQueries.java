package com.surelogic.sierra.jdbc.scan;

import java.sql.Connection;

import com.surelogic.common.jdbc.ConnectionQuery;
import com.surelogic.common.jdbc.DBTransaction;
import com.surelogic.common.jobs.SLProgressMonitor;

public final class ScanQueries {

	private ScanQueries() {
		// Not instantiable
	}

	public static DBTransaction<Void> deleteUnfinishedScans(
			final SLProgressMonitor mon) {
		return new DBTransaction<Void>() {
			public Void perform(final Connection conn) throws Exception {
				ScanManager.getInstance(conn).deleteScans(
						new Scans(new ConnectionQuery(conn)).unfinishedScans(),
						mon);
				return null;
			}
		};
	}
}
