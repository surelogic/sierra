package com.surelogic.sierra.jdbc.scan;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.surelogic.common.SLProgressMonitor;
import com.surelogic.sierra.jdbc.DBType;
import com.surelogic.sierra.jdbc.JDBCUtils;
import com.surelogic.sierra.jdbc.project.ProjectManager;
import com.surelogic.sierra.jdbc.project.ProjectRecordFactory;
import com.surelogic.sierra.jdbc.record.ProjectRecord;
import com.surelogic.sierra.jdbc.record.ScanRecord;
import com.surelogic.sierra.tool.analyzer.ScanGenerator;

public class ScanManager {

	private static final String MAKE_TEMP_DERBY = "DECLARE GLOBAL TEMPORARY TABLE TEMP_IDS (ID BIGINT NOT NULL) NOT LOGGED";
	private static final String MAKE_TEMP_ORACLE = "CREATE GLOBAL TEMPORARY TABLE TEMP_IDS (ID NUMBER NOT NULL) ON COMMIT DELETE ROWS";

	private static final String OLDEST_SCAN_BY_PROJECT = "SELECT SCAN_UUID FROM OLDEST_SCANS WHERE PROJECT = ?";

	private final Connection conn;
	private final ScanRecordFactory factory;
	private final PreparedStatement selectScan;
	private final PreparedStatement selectQualifiers;
	private final PreparedStatement selectArtifacts;
	private final PreparedStatement selectSources;
	private final PreparedStatement selectErrors;
	
	private final PreparedStatement deleteSources;
	private final PreparedStatement deleteCompilations;
	private final PreparedStatement insertTempSources;
	private final PreparedStatement insertTempCompilations;
	private final PreparedStatement getOldestScanByProject;

	private ScanManager(Connection conn) throws SQLException {
		this.conn = conn;
		this.factory = ScanRecordFactory.getInstance(conn);

		/** Make sure this is called only once per connection */
		Statement st = conn.createStatement();
		String tempTableName;
		try {
			if (DBType.ORACLE == JDBCUtils.getDb(conn)) {
				try {
					st.execute(MAKE_TEMP_ORACLE);
				} catch (SQLException sql) {
					// XXX Silently ignore this...this needs to be corrected
					// eventually
				}
				tempTableName = "TEMP_IDS";
			} else {
				try {
					st.execute(MAKE_TEMP_DERBY);
				} catch (SQLException sql) {
					// XXX Silently ignore this...this needs to be corrected
					// eventually
				}
				tempTableName = "SESSION.TEMP_IDS";
			}
		} finally {
			st.close();
		}
		this.selectScan = conn.prepareStatement("");
		this.selectQualifiers = conn
				.prepareStatement("SELECT Q.NAME FROM QUALIFIER_SCAN_RELTN QSR, QUALIFIER Q WHERE QSR.SCAN_ID = ? AND Q.ID = QSR.QUALIFIER_ID");
		this.selectArtifacts = conn.prepareStatement("");
		this.selectSources = conn.prepareStatement("");
		this.selectErrors = conn.prepareStatement("");
		this.deleteCompilations = conn
				.prepareStatement("DELETE FROM COMPILATION_UNIT WHERE ID IN ("
						+ " SELECT ID FROM " + tempTableName + ")");
		this.deleteSources = conn
				.prepareStatement("DELETE FROM SOURCE_LOCATION WHERE ID IN ("
						+ " SELECT ID FROM " + tempTableName + ")");
		this.insertTempSources = conn
				.prepareStatement("INSERT INTO "
						+ tempTableName
						+ " SELECT NO_PRIMARY.ID FROM ("
						+ " SELECT SL.ID \"ID\""
						+ " FROM SOURCE_LOCATION SL"
						+ " LEFT OUTER JOIN ARTIFACT A ON A.PRIMARY_SOURCE_LOCATION_ID = SL.ID"
						+ " WHERE A.PRIMARY_SOURCE_LOCATION_ID IS NULL"
						+ " ) NO_PRIMARY"
						+ " LEFT OUTER JOIN ARTIFACT_SOURCE_LOCATION_RELTN ASLR ON ASLR.SOURCE_LOCATION_ID = NO_PRIMARY.ID"
						+ " WHERE ASLR.SOURCE_LOCATION_ID IS NULL");
		this.insertTempCompilations = conn
				.prepareStatement("INSERT INTO "
						+ tempTableName
						+ " SELECT NO_SOURCE.ID FROM ("
						+ " SELECT CU.ID"
						+ " FROM COMPILATION_UNIT CU"
						+ " LEFT OUTER JOIN SOURCE_LOCATION SL ON SL.COMPILATION_UNIT_ID = CU.ID"
						+ " WHERE SL.COMPILATION_UNIT_ID IS NULL) NO_SOURCE"
						+ " LEFT OUTER JOIN METRIC_CU CM ON CM.COMPILATION_UNIT_ID = NO_SOURCE.ID"
						+ " WHERE CM.COMPILATION_UNIT_ID IS NULL");
		this.getOldestScanByProject = conn
				.prepareStatement(OLDEST_SCAN_BY_PROJECT);
	}

	public ScanGenerator getScanGenerator() {
		return new JDBCScanGenerator(conn, factory, this);
	}

	public void readScan(String uid, ScanGenerator gen) throws SQLException {
		selectScan.setString(1, uid);
		String scaS = "SELECT S.ID,P.NAME,S.JAVA_VENDOR,S.JAVA_VERSION,U.USER_NAME,";
		ResultSet scan = selectScan.executeQuery();
		if (scan.next()) {
			gen.uid(uid);

			int idx = 1;
			Long id = scan.getLong(idx++);
			gen.project(scan.getString(idx++));
			gen.javaVendor(scan.getString(idx++));
			gen.javaVersion(scan.getString(idx++));
			gen.user(scan.getString(idx++));
			
			selectQualifiers.setLong(1, id);
			ResultSet qualSet = selectArtifacts.executeQuery();
			try {
				List<String> qualifiers = new ArrayList<String>();
				while (qualSet.next()) {
					qualifiers.add(qualSet.getString(1));
				}
				gen.qualifiers(qualifiers);
			} finally {
				qualSet.close();
			}
			selectArtifacts.setLong(1, id);
			
			ResultSet artifacts = selectArtifacts.executeQuery();

		} else {
			throw new IllegalArgumentException(uid
					+ " is not a valid scan uid.");
		}
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
