package com.surelogic.sierra.jdbc.finding;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Date;
import java.util.List;

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
	private final PreparedStatement populateOverview;
	private final PreparedStatement selectFindingProject;

	private ClientFindingManager(Connection conn) throws SQLException {
		super(conn);
		try {
			conn
					.createStatement()
					.execute(
							"DECLARE GLOBAL TEMPORARY TABLE TEMP_FINDING_IDS (ID BIGINT NOT NULL) NOT LOGGED");
		} catch (SQLException e) {
			// Do nothing, the table is probably already there.
		}

		deleteFindingFromOverview = conn
				.prepareStatement("DELETE FROM FINDINGS_OVERVIEW WHERE FINDING_ID = ?");
		deleteOverview = conn
				.prepareStatement("DELETE FROM FINDINGS_OVERVIEW WHERE PROJECT_ID = ?");
		populateOverview = conn
				.prepareStatement("INSERT INTO FINDINGS_OVERVIEW"
						+ " SELECT F.PROJECT_ID,F.ID,"
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
						+ "        FAC.LINE_OF_CODE,"
						+ "        CASE WHEN FAC.COUNT IS NULL THEN 0 ELSE FAC.COUNT END,"
						+ "        CASE WHEN COUNT.COUNT IS NULL THEN 0 ELSE COUNT.COUNT END,"
						+ "        ?,"
						+ "        LM.PACKAGE_NAME,"
						+ "        LM.CLASS_NAME,"
						+ "        FT.NAME,"
						+ "        FAC.TOOL,"
						+ "        F.SUMMARY"
						+ " FROM"
						+ "    SESSION.TEMP_FINDING_IDS TF"
						+ "    INNER JOIN FINDING F ON F.ID = TF.ID"
						+ "    LEFT OUTER JOIN FIXED_FINDINGS FIXED ON FIXED.ID = F.ID"
						+ "    LEFT OUTER JOIN RECENT_FINDINGS RECENT ON RECENT.ID = F.ID"
						+ "    LEFT OUTER JOIN ("
						+ "       SELECT AFR.FINDING_ID \"ID\","
						+ "              MAX(SL.LINE_OF_CODE) \"LINE_OF_CODE\","
						+ "              COUNT(AFR.ARTIFACT_ID) \"COUNT\","
						+ "              CASE WHEN COUNT(DISTINCT T.ID) = 1 THEN MAX(T.NAME) ELSE 'Many' END \"TOOL\" "
						+ "       FROM ARTIFACT A, SOURCE_LOCATION SL, ARTIFACT_FINDING_RELTN AFR, ARTIFACT_TYPE ART, TOOL T"
						+ "       WHERE A.SCAN_ID = ? AND "
						+ "             SL.ID = A.PRIMARY_SOURCE_LOCATION_ID AND"
						+ "             AFR.ARTIFACT_ID = A.ID AND "
						+ "             ART.ID = A.ARTIFACT_TYPE_ID AND "
						+ "             T.ID = ART.TOOL_ID"
						+ "       GROUP BY AFR.FINDING_ID) AS FAC ON FAC.ID = F.ID"
						+ "    LEFT OUTER JOIN ("
						+ "       SELECT"
						+ "          A.FINDING_ID \"ID\", COUNT(*) \"COUNT\""
						+ "       FROM AUDIT A"
						+ "       WHERE A.EVENT='COMMENT'"
						+ "       GROUP BY A.FINDING_ID) AS COUNT ON COUNT.ID = F.ID"
						+ "    INNER JOIN LOCATION_MATCH LM ON LM.FINDING_ID = F.ID"
						+ "    INNER JOIN FINDING_TYPE FT ON FT.ID = LM.FINDING_TYPE_ID");
		deleteTempIds = conn
				.prepareStatement("DELETE FROM SESSION.TEMP_FINDING_IDS");
		populateSingleTempId = conn
				.prepareStatement("INSERT INTO SESSION.TEMP_FINDING_IDS (ID) VALUES (?)");
		populateTempIds = conn
				.prepareStatement("INSERT INTO SESSION.TEMP_FINDING_IDS"
						+ "   SELECT AFR.FINDING_ID FROM SCAN S, ARTIFACT A, ARTIFACT_FINDING_RELTN AFR"
						+ "   WHERE "
						+ "      S.ID IN ((SELECT SCAN_ID FROM OLDEST_SCANS WHERE PROJECT = ?) UNION (SELECT SCAN_ID FROM LATEST_SCANS WHERE PROJECT = ?))"
						+ "      AND A.SCAN_ID = S.ID"
						+ "      AND AFR.ARTIFACT_ID = A.ID");
		selectFindingProject = conn
				.prepareStatement("SELECT P.NAME FROM FINDING F, PROJECT P WHERE F.ID = ? AND P.ID = F.PROJECT_ID");
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
		populateOverview.setString(1, projectName);
		populateOverview.execute();
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
				log.info("Clearing overview");
				deleteOverview.setLong(1, p.getId());
				deleteOverview.execute();
				log.info("Calculating ids");
				int idx = 1;
				populateTempIds.setString(idx++, projectName);
				populateTempIds.setString(idx++, projectName);
				populateTempIds.execute();
				log.info("Populating overview");
				idx = 1;
				populateOverview.setString(idx++, projectName);
				populateOverview.setLong(idx++, scanRecord.getId());
				populateOverview.execute();
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
		if (set.next()) {
			regenerateOverview(set.getString(1), Collections
					.singletonList(findingId));
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
