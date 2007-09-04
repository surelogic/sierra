package com.surelogic.sierra.client.eclipse.jobs;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.surelogic.common.SLProgressMonitor;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.client.eclipse.Data;
import com.surelogic.sierra.jdbc.scan.ScanManager;
import com.surelogic.sierra.jdbc.scan.ScanPersistenceException;
import com.surelogic.sierra.tool.analyzer.ScanGenerator;
import com.surelogic.sierra.tool.message.MessageWarehouse;

public final class ScanDocumentUtility {

	private static final Logger LOG = SLLogger.getLogger("sierra");

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
			try {
				conn.setAutoCommit(false);
				ScanGenerator gen = ScanManager.getInstance(conn)
						.getScanGenerator();
				MessageWarehouse.getInstance().parseScanDocument(scanDocument,
						gen, monitor);
			} catch (SQLException e) {
				LOG.log(Level.SEVERE, "SQL Exception while persisting run.", e);
			} catch (Exception e) {
				LOG.log(Level.SEVERE,
						"Exception occurred while persisting run.", e);
			} finally {
				conn.close();
			}
		} catch (SQLException e) {
			LOG.log(Level.SEVERE, "SQL Exception while persisting run.", e);
		}
	}
}