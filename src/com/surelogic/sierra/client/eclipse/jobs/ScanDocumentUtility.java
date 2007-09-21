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
	 * @param projectName
	 *            the name of the project for the given scan document, may be
	 *            <code>null</code> NEED FIX
	 * @throws ScanPersistenceException
	 */
	public static void loadScanDocument(final File scanDocument,
			final SLProgressMonitor monitor, final String projectName)
			throws ScanPersistenceException {
		try {
			Connection conn = Data.getConnection();
			conn.setAutoCommit(false);
			RuntimeException exc = null;
			try {
				ScanManager sMan = ScanManager.getInstance(conn);
				if (projectName != null) {
					sMan.deleteOldestScan(projectName, monitor);
				}
				conn.commit();
				ScanGenerator gen = sMan.getScanGenerator();
				MessageWarehouse.getInstance().parseScanDocument(scanDocument,
						gen, monitor);

			} catch (RuntimeException e) {
				exc = e;
			} finally {
				try {
					conn.close();
				} catch (SQLException e) {
					if (exc == null) {
						throw e;
					}
				}
			}
			if (exc != null) {
				throw exc;
			}
		} catch (SQLException e) {
			// Could not get a valid connection
			throw new IllegalStateException(e);
		}
	}
}