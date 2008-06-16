package com.surelogic.sierra.table;

import java.sql.Connection;

import com.surelogic.sierra.gwt.client.data.Report;
import com.surelogic.sierra.gwt.client.data.ReportTable;

/**
 * The interface for generating tables off of reports.
 * <p>
 * Implementing objects should be safe to shared by several servlet instances.
 * Thus, these objects should not contain any state.
 * <p>
 * Implementing objects must have a no-argument constructor.
 */
public interface IDatabaseTable {
	/**
	 * Generate a table from the given report and database connection.
	 * 
	 * @param report
	 * @param conn
	 *            a non-null connection to the Sierra database. This connection
	 *            will be closed by the caller of this method.
	 * @return the resulting table
	 */
	ReportTable generate(Report report, Connection conn);

}
