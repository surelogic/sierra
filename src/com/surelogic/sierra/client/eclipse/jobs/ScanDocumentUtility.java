package com.surelogic.sierra.client.eclipse.jobs;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

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
	 * Parses a partial scan document into the database and generates findings.
	 * When this method is completed the scan document has been fully loaded
	 * into the Sierra client and is ready to be examined via the user
	 * interface.
	 * 
	 * @param scanDocument
	 *            the scan document.
	 * @param monitor
	 *            a progress monitor, may be <code>null</code> if progress is
	 *            not tracked.
	 * @param projectName
	 *            the name of the project for the given scan document, may be
	 *            <code>null</code> NEED FIX
	 * @param compilations
	 *            the map of compilation units in this partial scan. The keys of
	 *            compilationUnit are package names, and the values are
	 *            compilation names (without the .java extension)
	 * 
	 * @throws ScanPersistenceException
	 */
	public static void loadPartialScanDocument(final File scanDocument,
			final SLProgressMonitor monitor, final String projectName,
			final Map<String, List<String>> compilations)
			throws ScanPersistenceException {
		Throwable exc = null;
		try {
			Connection conn = Data.transactionConnection();
			try {
				ScanManager sMan = ScanManager.getInstance(conn);
				ScanGenerator gen = sMan.getPartialScanGenerator(projectName,
						compilations);
				MessageWarehouse.getInstance().parseScanDocument(scanDocument,
						gen, monitor);
				conn.commit();
			} catch (Exception e) {
				exc = e;
				conn.rollback();
			} finally {
				try {
					conn.close();
				} catch (Exception e) {
					if (exc == null) {
						exc = e;
					}
				}
			}
			if (exc != null) {
				throw new ScanPersistenceException(exc);
			}
		} catch (SQLException e) {
			// Could not get a valid connection
			throw new IllegalStateException(e);
		}
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
		Throwable exc = null;
		try {
			Connection conn = Data.transactionConnection();
			try {
				ScanManager sMan = ScanManager.getInstance(conn);
				if (projectName != null) {
					sMan.deleteOldestScan(projectName, monitor);
				}
				conn.commit();
				ScanGenerator gen = sMan.getScanGenerator();
				MessageWarehouse.getInstance().parseScanDocument(scanDocument,
						gen, monitor);
				conn.commit();
			} catch (Exception e) {
				exc = e;
				conn.rollback();
			} finally {
				try {
					conn.close();
				} catch (Exception e) {
					if (exc == null) {
						exc = e;
					}
				}
			}
			if (exc != null) {
				throw new ScanPersistenceException(exc);
			}
		} catch (SQLException e) {
			// Could not get a valid connection
			throw new IllegalStateException(e);
		}
	}
}