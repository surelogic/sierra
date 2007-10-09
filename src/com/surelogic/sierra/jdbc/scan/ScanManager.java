package com.surelogic.sierra.jdbc.scan;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;

import com.surelogic.common.SLProgressMonitor;
import com.surelogic.sierra.jdbc.record.ScanRecord;
import com.surelogic.sierra.tool.analyzer.ScanGenerator;

public class ScanManager {

	private static final String MAKE_TEMP = "DECLARE GLOBAL TEMPORARY TABLE TEMP_IDS (ID BIGINT NOT NULL) NOT LOGGED";

	private static final String INSERT_TEMP_SOURCES = "INSERT INTO SESSION.TEMP_IDS"
			+ " SELECT NO_PRIMARY.ID FROM ("
			+ " SELECT SL.ID \"ID\""
			+ " FROM SOURCE_LOCATION SL"
			+ " LEFT OUTER JOIN ARTIFACT A ON A.PRIMARY_SOURCE_LOCATION_ID = SL.ID"
			+ " WHERE A.PRIMARY_SOURCE_LOCATION_ID IS NULL"
			+ " ) AS NO_PRIMARY"
			+ " LEFT OUTER JOIN ARTIFACT_SOURCE_LOCATION_RELTN ASLR ON ASLR.SOURCE_LOCATION_ID = NO_PRIMARY.ID"
			+ " WHERE ASLR.SOURCE_LOCATION_ID IS NULL";

	private static final String DELETE_UNUSED_SOURCES = "DELETE FROM SOURCE_LOCATION WHERE ID IN ("
			+ " SELECT ID FROM SESSION.TEMP_IDS)";

	private static final String INSERT_TEMP_COMPILATIONS = "INSERT INTO SESSION.TEMP_IDS"
			+ " SELECT NO_SOURCE.ID FROM ("
			+ " SELECT CU.ID"
			+ " FROM COMPILATION_UNIT CU"
			+ " LEFT OUTER JOIN SOURCE_LOCATION SL ON SL.COMPILATION_UNIT_ID = CU.ID"
			+ " WHERE SL.COMPILATION_UNIT_ID IS NULL) AS NO_SOURCE"
			+ " LEFT OUTER JOIN METRIC_CU CM ON CM.COMPILATION_UNIT_ID = NO_SOURCE.ID"
			+ " WHERE CM.COMPILATION_UNIT_ID IS NULL";

	private static final String DELETE_UNUSED_COMPILATIONS = "DELETE FROM COMPILATION_UNIT WHERE ID IN ("
			+ " SELECT ID FROM SESSION.TEMP_IDS)";

	private static final String OLDEST_SCAN_BY_PROJECT = "SELECT SCAN_UUID FROM OLDEST_SCANS WHERE PROJECT = ?";

	private final Connection conn;
	private final ScanRecordFactory factory;
	private final PreparedStatement deleteSources;
	private final PreparedStatement deleteCompilations;
	private final PreparedStatement insertTempSources;
	private final PreparedStatement insertTempCompilations;
	private final PreparedStatement getOldestScanByProject;

	private ScanManager(Connection conn) throws SQLException {
		this.conn = conn;
		this.factory = ScanRecordFactory.getInstance(conn);

		/** Make sure this is called only once per connection */
		try {
			conn.createStatement().execute(MAKE_TEMP);
		} catch (SQLException sql) {
			// XXX Silently ignore this...this needs to be corrected eventually
		}

		this.deleteCompilations = conn
				.prepareStatement(DELETE_UNUSED_COMPILATIONS);
		this.deleteSources = conn.prepareStatement(DELETE_UNUSED_SOURCES);
		this.insertTempSources = conn.prepareStatement(INSERT_TEMP_SOURCES);
		this.insertTempCompilations = conn
				.prepareStatement(INSERT_TEMP_COMPILATIONS);
		this.getOldestScanByProject = conn
				.prepareStatement(OLDEST_SCAN_BY_PROJECT);
	}

	public ScanGenerator getScanGenerator() {
		return new JDBCScanGenerator(conn, factory, this);
	}

	public void deleteScans(Collection<String> uids, SLProgressMonitor monitor)
			throws SQLException {
		for (String uid : uids) {
			if (monitor != null) {
				if (monitor.isCanceled()) {
					return;
				}
			}
			ScanRecord rec = factory.newScan();
			rec.setUid(uid);
			if (rec.select()) {
				rec.delete();
			}
			work(monitor);
		}
		if (monitor != null) {
			if (monitor.isCanceled())
				return;
		}
		conn.commit();
		insertTempSources.execute();
		deleteSources.execute();
		conn.commit();
		work(monitor);
		if (monitor != null) {
			if (monitor.isCanceled())
				return;
		}
		insertTempCompilations.execute();
		deleteCompilations.execute();
		conn.commit();
		work(monitor);
	}

	private static void work(SLProgressMonitor monitor) {
		if (monitor != null) {
			monitor.worked(1);
		}
	}

	/**
	 * Remove the scan with the given uid from the database. This method quietly
	 * does nothing if the scan is not in the database.
	 * 
	 * @param uid
	 * @throws SQLException
	 */
	public void deleteScan(String uid, SLProgressMonitor monitor)
			throws SQLException {
		deleteScans(Collections.singleton(uid), monitor);
	}

	public void deleteOldestScan(String projectName, SLProgressMonitor monitor)
			throws SQLException {
		getOldestScanByProject.setString(1, projectName);
		ResultSet set = getOldestScanByProject.executeQuery();
		try {
			if (set.next()) {
				deleteScan(set.getString(1), monitor);
			}
		} finally {
			set.close();
		}
	}

	public static ScanManager getInstance(Connection conn) throws SQLException {
		return new ScanManager(conn);
	}
}
