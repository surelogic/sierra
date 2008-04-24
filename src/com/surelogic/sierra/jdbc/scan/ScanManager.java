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
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;

import com.surelogic.common.SLProgressMonitor;
import com.surelogic.common.jdbc.DBType;
import com.surelogic.common.jdbc.JDBCUtils;
import com.surelogic.sierra.jdbc.record.CompilationUnitRecord;
import com.surelogic.sierra.jdbc.record.ScanRecord;
import com.surelogic.sierra.jdbc.tool.FindingFilter;
import com.surelogic.sierra.tool.message.ScanGenerator;

public final class ScanManager {

	private static final String MAKE_TEMP_DERBY = "DECLARE GLOBAL TEMPORARY TABLE TEMP_IDS (ID BIGINT NOT NULL) NOT LOGGED";
	private static final String MAKE_TEMP_ORACLE = "CREATE GLOBAL TEMPORARY TABLE TEMP_IDS (ID NUMBER NOT NULL) ON COMMIT DELETE ROWS";

	private final Connection conn;
	private final ScanRecordFactory factory;
	private final PreparedStatement deleteSources;
	private final PreparedStatement deleteCompilations;
	private final PreparedStatement insertTempSources;
	private final PreparedStatement insertTempCompilations;
	private final PreparedStatement getOldestScanByProject;
	private final PreparedStatement getLatestScanByProject;
	private final PreparedStatement selectArtifactsByCompilation;
	private final PreparedStatement updateArtifactScan;
	private final PreparedStatement deleteMetricByCompilation;
	private final PreparedStatement updateMetricScan;
	private final PreparedStatement selectCurrentFindingByCompilation;
	private final PreparedStatement deleteScanOverviewByFinding;
	private final PreparedStatement updateScanOverviewByFinding;

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
				.prepareStatement("SELECT SCAN_UUID FROM OLDEST_SCANS WHERE PROJECT = ?");
		this.getLatestScanByProject = conn
				.prepareStatement("SELECT SCAN_UUID FROM LATEST_SCANS WHERE PROJECT = ?");
		this.selectArtifactsByCompilation = conn
				.prepareStatement("SELECT A.ID FROM SCAN S, ARTIFACT A, SOURCE_LOCATION SL, COMPILATION_UNIT CU WHERE"
						+ "   CU.ID = ? AND"
						+ "   A.SCAN_ID = ? AND"
						+ "   SL.ID = A.PRIMARY_SOURCE_LOCATION_ID AND"
						+ "   CU.ID = SL.COMPILATION_UNIT_ID");
		this.updateArtifactScan = conn
				.prepareStatement("UPDATE ARTIFACT SET SCAN_ID = ? WHERE ID = ?");
		this.deleteMetricByCompilation = conn
				.prepareStatement("DELETE FROM METRIC_CU WHERE COMPILATION_UNIT_ID = ? AND SCAN_ID = ?");
		this.updateMetricScan = conn
				.prepareStatement("UPDATE METRIC_CU SET SCAN_ID = ? WHERE COMPILATION_UNIT_ID = ? AND SCAN_ID = ?");
		this.selectCurrentFindingByCompilation = conn
				.prepareStatement("SELECT DISTINCT FINDING_ID FROM SOURCE_LOCATION SL, ARTIFACT A, ARTIFACT_FINDING_RELTN AFR"
						+ "   WHERE SL.COMPILATION_UNIT_ID = ? AND"
						+ "      A.PRIMARY_SOURCE_LOCATION_ID = SL.ID AND"
						+ "      A.SCAN_ID IN (?,?) AND"
						+ "      AFR.ARTIFACT_ID = A.ID");
		this.deleteScanOverviewByFinding = conn
				.prepareStatement("DELETE FROM SCAN_OVERVIEW WHERE SCAN_ID = ? AND FINDING_ID = ?");
		this.updateScanOverviewByFinding = conn
				.prepareStatement("UPDATE SCAN_OVERVIEW SET SCAN_ID = ? WHERE SCAN_ID = ? AND FINDING_ID = ?");
	}

	public ScanGenerator getScanGenerator(FindingFilter filter) {
		return new JDBCScanGenerator(conn, factory, this, filter);
	}

	/**
	 * Returns information about the latest scan of this project, or
	 * <code>null</code> if there are no scans for this project.
	 * 
	 * @param projectName
	 * @return
	 * @throws SQLException
	 */
	public ScanInfo getLatestScanInfo(String projectName) throws SQLException {
		getLatestScanByProject.setString(1, projectName);
		final ResultSet set = getLatestScanByProject.executeQuery();
		try {
			if (set.next()) {
				return getScanInfo(set.getString(1));
			} else {
				return null;
			}
		} finally {
			set.close();
		}
	}

	/**
	 * Return information about the specified scan, or <code>null</code> if no
	 * scan with this uid exists.
	 * 
	 * @param scanUid
	 * @return
	 */
	public ScanInfo getScanInfo(String scanUid) throws SQLException {
		final ScanRecord record = factory.newScan();
		record.setUid(scanUid);
		if (record.select()) {
			return new ScanInfo(record);
		} else {
			return null;
		}
	}

	public void finalizeScan(String scanUid) throws SQLException {
		final ScanRecord record = factory.newScan();
		record.setUid(scanUid);
		if (record.select()) {
			record.setStatus(ScanStatus.FINISHED);
			record.update();
		} else {
			throw new IllegalArgumentException("No scan with id " + scanUid + " exists.");
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
		monitor.subTask("Deleting oldest scan");
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

	/**
	 * Return a generator used to persist a partial scan. This is currently only
	 * allowed in the client.
	 * 
	 * @param projectName
	 * @param compilations
	 * @param findingIds
	 *            a set of finding ids. All findingIds, that are affected by
	 *            scan persistence will be added to this set.
	 * @return
	 */
	public ScanGenerator getPartialScanGenerator(String projectName,
			FindingFilter filter, Map<String, List<String>> compilations,
			Set<Long> findingIds) {
		try {
			String oldestScan = null;
			String latestScan = null;
			getOldestScanByProject.setString(1, projectName);
			ResultSet set = getOldestScanByProject.executeQuery();
			try {
				if (set.next()) {
					oldestScan = set.getString(1);
				}
			} finally {
				set.close();
			}
			getLatestScanByProject.setString(1, projectName);
			set = getLatestScanByProject.executeQuery();
			try {
				if (set.next()) {
					latestScan = set.getString(1);
				}
			} finally {
				set.close();
			}
			if (latestScan == null) {
				// New scan, treat this as a normal scan
				return new JDBCScanGenerator(conn, factory, this, filter, false);
			} else {
				final ScanRecord latest = factory.newScan();
				latest.setUid(latestScan);
				latest.select();
				final ScanRecord oldest = factory.newScan();
				if (oldestScan == null) {
					// We have to create an older scan. We will copy it over
					// from the latest scan.
					oldest.setJavaVendor(latest.getJavaVendor());
					oldest.setJavaVersion(latest.getJavaVersion());
					oldest.setPartial(true);
					oldest.setProjectId(latest.getProjectId());
					oldest.setStatus(ScanStatus.FINISHED);
					oldest.setTimestamp(latest.getTimestamp());
					oldest.setUserId(latest.getUserId());
					oldest.setUid(UUID.randomUUID().toString());
					oldest.insert();
				} else {
					// Use the existing oldest scan
					oldest.setUid(oldestScan);
					oldest.select();
				}
				for (Entry<String, List<String>> packageCompilations : compilations
						.entrySet()) {
					final String pakkage = packageCompilations.getKey();
					for (final String compilation : packageCompilations
							.getValue()) {
						CompilationUnitRecord cu = factory.newCompilationUnit();
						cu.setPackageName(pakkage);
						cu.setCompilation(compilation);
						if (cu.select()) {
							int idx = 1;
							selectArtifactsByCompilation.setLong(idx++, cu
									.getId());
							selectArtifactsByCompilation.setLong(idx++, latest
									.getId());
							set = selectArtifactsByCompilation.executeQuery();
							try {
								final List<Long> artifactIds = new ArrayList<Long>();
								while (set.next()) {
									artifactIds.add(set.getLong(1));
								}
								for (long artifactId : artifactIds) {
									idx = 1;
									updateArtifactScan.setLong(idx++, oldest
											.getId());
									updateArtifactScan.setLong(idx++,
											artifactId);
									updateArtifactScan.execute();
								}
							} finally {
								set.close();
							}
							idx = 1;
							selectCurrentFindingByCompilation.setLong(idx++, cu
									.getId());
							selectCurrentFindingByCompilation.setLong(idx++,
									latest.getId());
							selectCurrentFindingByCompilation.setLong(idx++,
									oldest.getId());
							set = selectCurrentFindingByCompilation
									.executeQuery();
							try {
								while (set.next()) {
									findingIds.add(set.getLong(1));
								}
							} finally {
								set.close();
							}
							idx = 1;
							deleteMetricByCompilation
									.setLong(idx++, cu.getId());
							deleteMetricByCompilation.setLong(idx++, oldest
									.getId());
							deleteMetricByCompilation.execute();
							idx = 1;
							updateMetricScan.setLong(idx++, oldest.getId());
							updateMetricScan.setLong(idx++, cu.getId());
							updateMetricScan.setLong(idx++, latest.getId());
							updateMetricScan.execute();
						}
					}
					for (long findingId : findingIds) {
						int idx = 1;
						deleteScanOverviewByFinding.setLong(idx++, oldest
								.getId());
						deleteScanOverviewByFinding.setLong(idx++, findingId);
						deleteScanOverviewByFinding.execute();
						idx = 1;
						updateScanOverviewByFinding.setLong(idx++, oldest
								.getId());
						updateScanOverviewByFinding.setLong(idx++, latest
								.getId());
						updateScanOverviewByFinding.setLong(idx++, findingId);
						updateScanOverviewByFinding.execute();
					}

				}
				conn.commit();
				// Copy the appropriate artifacts to the previous scan, then run
				// against the latest scan
				return new JDBCPartialScanGenerator(conn, factory, this,
						latest, filter);
			}
		} catch (SQLException e) {
			try {
				conn.rollback();
			} catch (SQLException e1) {
				// We already have an exception
			}
			throw new ScanPersistenceException(
					"Could not persist partial scan for project " + projectName
							+ " and compilations " + compilations + ".", e);
		}
	}
}
