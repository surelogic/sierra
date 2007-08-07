package com.surelogic.sierra.client.eclipse.data;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

import com.surelogic.common.SLProgressMonitor;
import com.surelogic.sierra.client.eclipse.Data;
import com.surelogic.sierra.jdbc.run.JDBCRunGenerator;
import com.surelogic.sierra.tool.SierraLogger;
import com.surelogic.sierra.tool.analyzer.RunGenerator;
import com.surelogic.sierra.tool.message.MessageWarehouse;

public final class RunDocumentUtility {
	private static final Logger log = SierraLogger
			.getLogger(RunDocumentUtility.class.getName());

	private RunDocumentUtility() {
		// no instances
	}

	/**
	 * Parses a run document into the database and generates findings. When this
	 * method is completed the run document has been fully loaded into the
	 * Sierra client and is ready to be examined via the user interface.
	 * 
	 * @param runDocument
	 *            the run document.
	 * @param monitor
	 *            a progress monitor, may be <code>null</code> if progress is
	 *            not tracked.
	 */
	public static void loadRunDocument(final File runDocument,
			final SLProgressMonitor monitor) {
		try {
			Connection conn = Data.getConnection();
			try {
				conn.setAutoCommit(false);
				RunGenerator gen = JDBCRunGenerator.getInstance(conn);
				MessageWarehouse.getInstance().parseRunDocument(runDocument,
						gen, monitor);
			} catch (SQLException e) {
				log.severe(e.getMessage());
			} finally {
				conn.close();
			}
		} catch (SQLException e) {
			log.severe(e.getMessage());
		}
	}

}