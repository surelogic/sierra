package com.surelogic.sierra.client.eclipse.jobs;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;

import com.surelogic.common.SLProgressMonitor;
import com.surelogic.sierra.client.eclipse.Data;
import com.surelogic.sierra.jdbc.scan.ScanManager;
import com.surelogic.sierra.jdbc.scan.ScanPersistenceException;
import com.surelogic.sierra.tool.analyzer.ScanGenerator;
import com.surelogic.sierra.tool.message.MessageWarehouse;

public final class ScanDocumentUtility {

	private ScanDocumentUtility() {
		// no instances
	}

	/**
	 * Parses a scan document into the database and generates findings. When
	 * this method is completed the scan document has been fully loaded into the
	 * Sierra client and is ready to be examined via the user interface.
	 * 
	 * @param scanDocument
	 *            the scan document.
	 * @param monitor
	 *            a progress monitor, may be <code>null</code> if progress is
	 *            not tracked.
	 * @throws ScanPersistenceException
	 */
	public static void loadScanDocument(final File scanDocument,
			final SLProgressMonitor monitor) throws ScanPersistenceException {
		try {
			Connection conn = Data.getConnection();
			conn.setAutoCommit(false);
			try {
				ScanGenerator gen = ScanManager.getInstance(conn)
						.getScanGenerator();
				MessageWarehouse.getInstance().parseScanDocument(scanDocument,
						gen, monitor);

			} finally {
				conn.close();
			}
		} catch (SQLException e) {
			// Could not get a valid connection
			throw new IllegalStateException(e);
		}
	}
}