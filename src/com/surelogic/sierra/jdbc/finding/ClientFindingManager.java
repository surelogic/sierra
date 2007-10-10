package com.surelogic.sierra.jdbc.finding;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.surelogic.common.SLProgressMonitor;
import com.surelogic.sierra.jdbc.DBType;
import com.surelogic.sierra.jdbc.JDBCUtils;
import com.surelogic.sierra.jdbc.project.ProjectRecordFactory;
import com.surelogic.sierra.jdbc.record.FindingRecord;
import com.surelogic.sierra.jdbc.record.ProjectRecord;
import com.surelogic.sierra.jdbc.record.ScanRecord;
import com.surelogic.sierra.jdbc.scan.ScanRecordFactory;
import com.surelogic.sierra.tool.message.Importance;

public final class ClientFindingManager extends FindingManager {
	private final PreparedStatement deleteFindingFromOverview;
	private final PreparedStatement deleteOverview;
	private final PreparedStatement populateSingleTempId;
	private final PreparedStatement populateTempIds;
	private final PreparedStatement deleteTempIds;
	private final PreparedStatement selectFindingById;
	private final PreparedStatement populateFindingOverview;
	private final PreparedStatement selectFindingProject;

	private ClientFindingManager(Connection conn) throws SQLException {
		super(conn);
		Statement st = conn.createStatement();
		String tempTableName;
		try {
			if (DBType.ORACLE == JDBCUtils.getDb(conn)) {
				try {
					st
							.execute("CREATE GLOBAL TEMPORARY TABLE TEMP_FINDING_IDS (ID NUMBER NOT NULL) ON COMMIT DELETE ROWS");
				} catch (SQLException e) {
					// Do nothing, the table is probably already there.
				}
				tempTableName = "TEMP_FINDING_IDS";
			} else {

				try {
					st
							.execute("DECLARE GLOBAL TEMPORARY TABLE TEMP_FINDING_IDS (ID BIGINT NOT NULL) NOT LOGGED");
				} catch (SQLException e) {
					// Do nothing, the table is probably already there.
				}
				tempTableName = "SESSION.TEMP_FINDING_IDS";
			}
		} finally {
			st.close();
		}
		populateFindingOverview = conn
				.prepareStatement("INSERT INTO FINDINGS_OVERVIEW"
						+ " SELECT F.ID,F.PROJECT_ID,"
						+ "        CASE WHEN F.IS_READ = 'Y' THEN 'Yes' ELSE 'No' END,"
						+ "        F.LAST_CHANGED,"
						+ "        CASE"
						+ "             WHEN F.IMPORTANCE=0 THEN 'Irrelevant'"
						+ " 	        WHEN F.IMPORTANCE=1 THEN 'Low'"
						+ "             WHEN F.IMPORTANCE=2 THEN 'Medium'"
						+ "             WHEN F.IMPORTANCE=3 THEN 'High'"
						+ "             WHEN F.IMPORTANCE=4 THEN 'Critical'"
						+ "        END,"
						+ "        CASE"
						+ "             WHEN FIXED.ID IS NOT NULL THEN 'Fixed'"
						+ "	            WHEN RECENT.ID IS NOT NULL THEN 'New'"
						+ "	            ELSE 'Unchanged'"
						+ "        END,"
						+ "        SO.LINE_OF_CODE,"
						+ "        CASE WHEN SO.ARTIFACT_COUNT IS NULL THEN 0 ELSE SO.ARTIFACT_COUNT END,"
						+ "        CASE WHEN COUNT.COUNT IS NULL THEN 0 ELSE COUNT.COUNT END,"
						+ "        ?,"
						+ "        LM.PACKAGE_NAME,"
						+ "        LM.CLASS_NAME,"
						+ "        FT.NAME,"
						+ "        SO.TOOL,"
						+ "        F.SUMMARY"
						+ " FROM"
						+ "    "
						+ tempTableName
						+ " TF"
						+ "    INNER JOIN FINDING F ON F.ID = TF.ID"
						+ "    LEFT OUTER JOIN FIXED_FINDINGS FIXED ON FIXED.ID = F.ID"
						+ "    LEFT OUTER JOIN RECENT_FINDINGS RECENT ON RECENT.ID = F.ID"
						+ "    LEFT OUTER JOIN SCAN_OVERVIEW SO ON SO.FINDING_ID = F.ID AND SO.SCAN_ID = ?"
						+ "    LEFT OUTER JOIN ("
						+ "       SELECT"
						+ "          A.FINDING_ID \"ID\", COUNT(*) \"COUNT\""
						+ "       FROM SIERRA_AUDIT A"
						+ "       WHERE A.EVENT='COMMENT'"
						+ "       GROUP BY A.FINDING_ID) AS COUNT ON COUNT.ID = F.ID"
						+ "    INNER JOIN LOCATION_MATCH LM ON LM.FINDING_ID = F.ID"
						+ "    INNER JOIN FINDING_TYPE FT ON FT.ID = LM.FINDING_TYPE_ID");
		deleteTempIds = conn.prepareStatement("DELETE FROM " + tempTableName);
		populateSingleTempId = conn.prepareStatement("INSERT INTO "
				+ tempTableName + " (ID) VALUES (?)");
		populateTempIds = conn
				.prepareStatement("INSERT INTO "
						+ tempTableName
						+ "   SELECT AFR.FINDING_ID FROM SCAN S, ARTIFACT A, ARTIFACT_FINDING_RELTN AFR"
						+ "   WHERE"
						+ "      S.ID IN (SELECT SCAN_ID FROM OLDEST_SCANS WHERE PROJECT = ?)"
						+ "      AND A.SCAN_ID = S.ID"
						+ "      AND AFR.ARTIFACT_ID = A.ID"
						+ " UNION"
						+ "   SELECT AFR.FINDING_ID FROM SCAN S, ARTIFACT A, ARTIFACT_FINDING_RELTN AFR"
						+ "   WHERE"
						+ "      S.ID IN (SELECT SCAN_ID FROM LATEST_SCANS WHERE PROJECT = ?)"
						+ "      AND A.SCAN_ID = S.ID"
						+ "      AND AFR.ARTIFACT_ID = A.ID");

		selectFindingProject = conn
				.prepareStatement("SELECT P.NAME FROM FINDING F, PROJECT P WHERE F.ID = ? AND P.ID = F.PROJECT_ID");
		deleteFindingFromOverview = conn
				.prepareStatement("DELETE FROM FINDINGS_OVERVIEW WHERE FINDING_ID = ?");
		deleteOverview = conn
				.prepareStatement("DELETE FROM FINDINGS_OVERVIEW WHERE PROJECT_ID = ?");
		selectFindingById = conn
				.prepareStatement("SELECT UUID,PROJECT_ID,IMPORTANCE,SUMMARY,OBSOLETED_BY_ID,OBSOLETED_BY_REVISION FROM FINDING WHERE ID = ?");
	}

	/**
	 * Make a user comment on an existing finding. This method is for use by a
	 * client.
	 * 
	 * @param findingId
	 * @param comment
	 * @throws SQLException
	 */
	public void comment(Long findingId, String comment) throws SQLException {
		checkIsRead(findingId);
		comment(null, findingId, comment, new Date(), null);
		regenerateFindingOverview(findingId);
	}

	/**
	 * Set the importance of a particular finding. This method is for use by a
	 * client.
	 * 
	 * @param findingId
	 * @param importance
	 * @throws SQLException
	 */
	public void setImportance(Long findingId, Importance importance)
			throws SQLException {
		checkIsRead(findingId);
		setImportance(null, findingId, importance, new Date(), null);
		regenerateFindingOverview(findingId);
	}

	/**
	 * Indicate that the user has looked over this finding. This method is for
	 * use by a client.
	 * 
	 * @param findingId
	 * @throws SQLException
	 */
	public void markAsRead(Long findingId) throws SQLException {
		checkIsRead(findingId);
		markAsRead(null, findingId, new Date(), null);
		regenerateFindingOverview(findingId);
	}

	/**
	 * Change the summary that should be displayed for a particular finding.
	 * 
	 * @param findingId
	 * @param summary
	 * @throws SQLException
	 */
	public void changeSummary(Long findingId, String summary)
			throws SQLException {
		checkIsRead(findingId);
		changeSummary(null, findingId, summary, new Date(), null);
	}

	/**
	 * Regenerate findings overview information for the selected findings. This
	 * method cannot add new findings to the overview, merely regenerate
	 * existing ones.
	 * 
	 * @param findingIds
	 */
	public void regenerateOverview(String projectName, List<Long> findingIds)
			throws SQLException {
		for (Long id : findingIds) {
			deleteFindingFromOverview.setLong(1, id);
			if (deleteFindingFromOverview.executeUpdate() == 1) {
				populateSingleTempId.setLong(1, id);
				populateSingleTempId.execute();
			}
		}
		populateFindingOverview.setString(1, projectName);
		populateFindingOverview.execute();
	}

	/**
	 * Regenerate the findings overview for the given project.
	 * 
	 * @param projectName
	 * @param scan
	 *            the uid of the latest scan. This must be the latest scan.
	 * @throws SQLException
	 */
	public void generateOverview(String projectName, String scan)
			throws SQLException {
		ProjectRecord p = ProjectRecordFactory.getInstance(conn).newProject();
		p.setName(projectName);
		if (p.select()) {
			ScanRecord scanRecord = ScanRecordFactory.getInstance(conn)
					.newScan();
			scanRecord.setUid(scan);
			if (scanRecord.select()) {
				log.info("Populating scan overview for scan "
						+ scanRecord.getUid() + ".");
				populateScanOverview(scanRecord.getId());
				log.info("Clearing overview for project " + p.getName() + ".");
				deleteOverview.setLong(1, p.getId());
				deleteOverview.execute();
				log.info("Calculating ids in overview for project "
						+ p.getName() + ".");
				int idx = 1;
				populateTempIds.setString(idx++, projectName);
				populateTempIds.setString(idx++, projectName);
				populateTempIds.execute();
				log
						.info("Populating overview for project " + p.getName()
								+ ".");
				idx = 1;
				populateFindingOverview.setString(idx++, projectName);
				populateFindingOverview.setLong(idx++, scanRecord.getId());
				populateFindingOverview.execute();
				log.info("Deleting temp ids");
				deleteTempIds.execute();
			} else {
				throw new IllegalArgumentException("No scan with uid " + scan
						+ " exists in the database");
			}
		} else {
			throw new IllegalArgumentException(projectName
					+ " is not a valid project name.");
		}
	}

	@Override
	public void deleteFindings(String projectName, SLProgressMonitor monitor)
			throws SQLException {
		ProjectRecord pRec = ProjectRecordFactory.getInstance(conn)
				.newProject();
		pRec.setName(projectName);
		if (pRec.select()) {
			deleteOverview.setLong(1, pRec.getId());
			deleteOverview.execute();
		}
		super.deleteFindings(projectName, monitor);
	}

	/**
	 * For use in the client, this method regenerates the overview for a single
	 * finding.
	 * 
	 * @param findingId
	 * @throws SQLException
	 */
	private void regenerateFindingOverview(Long findingId) throws SQLException {
		selectFindingProject.setLong(1, findingId);
		ResultSet set = selectFindingProject.executeQuery();
		try {
			if (set.next()) {
				regenerateOverview(set.getString(1), Collections
						.singletonList(findingId));
			}
		} finally {
			set.close();
		}
	}

	/**
	 * For use in the client. Checks to see if a finding has been read. If it
	 * has not, it marks it as read.
	 * 
	 * @param findingId
	 * @throws SQLException
	 */
	protected void checkIsRead(Long findingId) throws SQLException {
		FindingRecord f = getFinding(findingId);
		if (f == null)
			throw new IllegalArgumentException(findingId
					+ " is not a valid finding id.");
		if (!f.isRead()) {
			markAsRead(null, findingId, new Date(), null);
		}
	}

	private FindingRecord getFinding(Long findingId) throws SQLException {
		FindingRecord record = factory.newFinding();
		selectFindingById.setLong(1, findingId);
		ResultSet set = selectFindingById.executeQuery();
		try {
			if (set.next()) {
				int idx = 1;
				record.setUid(set.getString(idx++));
				record.readAttributes(set, idx);
			} else {
				return null;
			}
		} finally {
			set.close();
		}
		record.setId(findingId);
		if (record.select()) {
			return record;
		} else {
			return null;
		}
	}

	public static ClientFindingManager getInstance(Connection conn)
			throws SQLException {
		return new ClientFindingManager(conn);
	}

}
