package com.surelogic.sierra.client.eclipse.data;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.surelogic.common.SLProgressMonitor;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.client.eclipse.Data;
import com.surelogic.sierra.jdbc.run.RunManager;
import com.surelogic.sierra.jdbc.run.RunPersistenceException;
import com.surelogic.sierra.tool.analyzer.RunGenerator;
import com.surelogic.sierra.tool.message.FindingTypeFilter;
import com.surelogic.sierra.tool.message.MessageWarehouse;
import com.surelogic.sierra.tool.message.Settings;

public final class RunDocumentUtility {
	private static final Logger log = SLLogger.getLogger("sierra");

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
	 * @throws RunPersistenceException
	 */
	public static void loadRunDocument(final File runDocument,
			final SLProgressMonitor monitor) throws RunPersistenceException {
		try {
			Connection conn = Data.getConnection();
			try {
				conn.setAutoCommit(false);
				Settings settings = new Settings();
				settings.setRuleFilter(new ArrayList<FindingTypeFilter>());
				RunGenerator gen = RunManager.getInstance(conn)
						.getRunGenerator(settings);
				MessageWarehouse.getInstance().parseRunDocument(runDocument,
						gen, monitor);
			} catch (SQLException e) {
				log.log(Level.SEVERE, "SQL Exception while persisting run.", e);
			} catch (Exception e) {
				log.log(Level.SEVERE,
						"Exception occurred while persisting run.", e);
			} finally {
				conn.close();
			}
		} catch (SQLException e) {
			log.log(Level.SEVERE, "SQL Exception while persisting run.", e);
		}
	}
}