package com.surelogic.sierra.jdbc.finding;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.logging.Level;

import com.surelogic.common.jdbc.ConnectionQuery;
import com.surelogic.common.jdbc.DBType;
import com.surelogic.common.jdbc.JDBCUtils;
import com.surelogic.common.jobs.SLProgressMonitor;
import com.surelogic.sierra.jdbc.project.ProjectRecordFactory;
import com.surelogic.sierra.jdbc.record.FindingRecord;
import com.surelogic.sierra.jdbc.record.LongRelationRecord;
import com.surelogic.sierra.jdbc.record.MatchRecord;
import com.surelogic.sierra.jdbc.record.ProjectRecord;
import com.surelogic.sierra.jdbc.record.RelationRecord;
import com.surelogic.sierra.jdbc.record.ScanRecord;
import com.surelogic.sierra.jdbc.scan.ScanManager;
import com.surelogic.sierra.jdbc.scan.ScanRecordFactory;
import com.surelogic.sierra.jdbc.settings.ScanFilterDO;
import com.surelogic.sierra.jdbc.settings.ScanFilters;
import com.surelogic.sierra.jdbc.settings.SettingQueries;
import com.surelogic.sierra.jdbc.settings.TypeFilterDO;
import com.surelogic.sierra.jdbc.tool.FindingFilter;
import com.surelogic.sierra.tool.message.Audit;
import com.surelogic.sierra.tool.message.AuditEvent;
import com.surelogic.sierra.tool.message.Importance;
import com.surelogic.sierra.tool.message.Match;
import com.surelogic.sierra.tool.message.Merge;
import com.surelogic.sierra.tool.message.SyncTrailRequest;
import com.surelogic.sierra.tool.message.SyncTrailResponse;

public final class ClientFindingManager extends FindingManager {

	private final PreparedStatement populatePartialScanOverview;
	private final PreparedStatement populatePartialScanRelationOverview;
	private final PreparedStatement selectArtifactsByCompilation;
	private final PreparedStatement selectArtifactsByTool;
	private final PreparedStatement deleteFindingFromOverview;
	private final PreparedStatement deleteFindingRelationFromOverview;
	private final PreparedStatement deleteFindingRelationOverview;
	private final PreparedStatement deleteOverview;
	private final PreparedStatement checkAndInsertTempId;
	private final PreparedStatement insertTempId;
	private final PreparedStatement populateTempIds;
	private final PreparedStatement deleteTempIds;
	private final PreparedStatement populateFindingOverviewCurrentFindings;
	private final PreparedStatement populateFindingRelationOverviewCurrentFindings;
	private final PreparedStatement populateFindingRelationOverviewFixedFindings;
	private final PreparedStatement populateFindingOverviewFixedFindings;
	private final PreparedStatement updateFindingOverviewImportance;
	private final PreparedStatement updateFindingOverviewSummary;
	private final PreparedStatement updateFindingOverviewComment;
	private final PreparedStatement selectLatestScanByProject;
	private final PreparedStatement selectOldestScanByProject;
	private final PreparedStatement selectLocalMerge;
	private final PreparedStatement findLocalAudits;
	private final PreparedStatement selectUnrevisionedAudits;
	private final PreparedStatement updateUnrevisionedAudit;
	private final PreparedStatement selectFindingFindingType;

	private ClientFindingManager(final Connection conn) throws SQLException {
		super(conn);
		final Statement st = conn.createStatement();
		String tempTableName;
		try {
			if (DBType.ORACLE == JDBCUtils.getDb(conn)) {
				try {
					st
							.execute("CREATE GLOBAL TEMPORARY TABLE TEMP_FINDING_IDS (ID NUMBER NOT NULL) ON COMMIT DELETE ROWS");
				} catch (final SQLException e) {
					log.log(Level.WARNING, e.getMessage(), e);
					// Do nothing, the table is probably already there.
				}
				tempTableName = "TEMP_FINDING_IDS";
			} else {
				try {
					st
							.execute("DECLARE GLOBAL TEMPORARY TABLE TEMP_FINDING_IDS (ID BIGINT NOT NULL) NOT LOGGED");
				} catch (final SQLException e) {
					log.log(Level.WARNING, e.getMessage(), e);
					// Do nothing, the table is probably already there.
				}
				tempTableName = "SESSION.TEMP_FINDING_IDS";
			}
		} finally {
			st.close();
		}
		populatePartialScanOverview = conn
				.prepareStatement("INSERT INTO SCAN_OVERVIEW (FINDING_ID,SCAN_ID,LINE_OF_CODE,ARTIFACT_COUNT,TOOL,CU,ASSURANCE_TYPE)"
						+ " SELECT AFR.FINDING_ID, ?, MAX(SL.LINE_OF_CODE), COUNT(AFR.ARTIFACT_ID), "
						+ "        CASE WHEN COUNT(DISTINCT T.ID) = 1 THEN MAX(T.NAME) ELSE '(From Multiple Tools)' END,"
						+ "        MAX(CU.CU), MAX(A.ASSURANCE_TYPE)"
						+ " FROM "
						+ tempTableName
						+ " TF, ARTIFACT_FINDING_RELTN AFR, ARTIFACT A, SOURCE_LOCATION SL, COMPILATION_UNIT CU, ARTIFACT_TYPE ART, TOOL T"
						+ " WHERE AFR.FINDING_ID = TF.ID AND"
						+ "       A.ID = AFR.ARTIFACT_ID AND"
						+ "       A.SCAN_ID = ? AND"
						+ "       SL.ID = A.PRIMARY_SOURCE_LOCATION_ID AND"
						+ "       CU.ID = SL.COMPILATION_UNIT_ID AND"
						+ "       ART.ID = A.ARTIFACT_TYPE_ID AND"
						+ "       T.ID = ART.TOOL_ID"
						+ " GROUP BY AFR.FINDING_ID");
		populatePartialScanRelationOverview = conn
				.prepareStatement("INSERT INTO SCAN_FINDING_RELATION_OVERVIEW (SCAN_ID,PARENT_FINDING_ID,CHILD_FINDING_ID,RELATION_TYPE) "
						+ "SELECT DISTINCT "
						+ "   S.ID,PFR.FINDING_ID,CFR.FINDING_ID,ANR.RELATION_TYPE "
						+ "FROM ARTIFACT_NUMBER_RELTN ANR, ARTIFACT P, ARTIFACT C, ARTIFACT_FINDING_RELTN PFR, ARTIFACT_FINDING_RELTN CFR, SCAN S, "
						+ tempTableName
						+ " TFP, "
						+ tempTableName
						+ " TFC "
						+ "WHERE S.ID = ? AND "
						+ "      ANR.SCAN_ID = S.ID AND "
						+ "      P.SCAN_ID = ANR.SCAN_ID AND "
						+ "      P.SCAN_NUMBER = ANR.PARENT_NUMBER AND "
						+ "      C.SCAN_ID = ANR.SCAN_ID AND "
						+ "      C.SCAN_NUMBER = ANR.CHILD_NUMBER AND "
						+ "      PFR.FINDING_ID = TFP.ID AND "
						+ "      PFR.ARTIFACT_ID = P.ID AND "
						+ "      CFR.ARTIFACT_ID = C.ID AND "
						+ "      CFR.FINDING_ID = TFC.ID");
		final String beginFindingOverviewUpdate = "UPDATE FINDINGS_OVERVIEW"
				+ " SET AUDITED = 'Yes'"
				+ ", LAST_CHANGED = CASE WHEN (? > LAST_CHANGED) THEN ? ELSE LAST_CHANGED END"
				+ ", AUDIT_COUNT = AUDIT_COUNT + 1";
		final String endFindingOverviewUpdate = " WHERE FINDING_ID = ?";
		updateFindingOverviewImportance = conn
				.prepareStatement(beginFindingOverviewUpdate
						+ ", IMPORTANCE = ?" + endFindingOverviewUpdate);
		updateFindingOverviewSummary = conn
				.prepareStatement(beginFindingOverviewUpdate + ", SUMMARY = ?"
						+ endFindingOverviewUpdate);
		updateFindingOverviewComment = conn
				.prepareStatement(beginFindingOverviewUpdate
						+ endFindingOverviewUpdate);
		populateFindingRelationOverviewCurrentFindings = conn
				.prepareStatement("INSERT INTO FINDING_RELATION_OVERVIEW (PARENT_FINDING_ID,CHILD_FINDING_ID,PROJECT_ID,RELATION_TYPE,STATUS) "
						+ "SELECT DISTINCT"
						+ "  L.PARENT_FINDING_ID, "
						+ "  L.CHILD_FINDING_ID, "
						+ "  ?, "
						+ "  L.RELATION_TYPE, "
						+ "  CASE WHEN O.CHILD_FINDING_ID IS NULL THEN 'New' ELSE 'Unchanged' END "
						+ "FROM "
						+ tempTableName
						+ " TF INNER JOIN SCAN_FINDING_RELATION_OVERVIEW L ON (L.PARENT_FINDING_ID = TF.ID OR L.CHILD_FINDING_ID = TF.ID) AND L.SCAN_ID = ? "
						+ "    LEFT OUTER JOIN SCAN_FINDING_RELATION_OVERVIEW O ON "
						+ "     O.PARENT_FINDING_ID = L.PARENT_FINDING_ID AND "
						+ "     O.CHILD_FINDING_ID = L.CHILD_FINDING_ID AND "
						+ "     O.RELATION_TYPE = L.RELATION_TYPE AND O.SCAN_ID = ?");
		populateFindingRelationOverviewFixedFindings = conn
				.prepareStatement("INSERT INTO FINDING_RELATION_OVERVIEW  (PARENT_FINDING_ID,CHILD_FINDING_ID,PROJECT_ID,RELATION_TYPE,STATUS) "
						+ "SELECT DISTINCT"
						+ "  O.PARENT_FINDING_ID, "
						+ "  O.CHILD_FINDING_ID, "
						+ "  ?, "
						+ "  O.RELATION_TYPE, "
						+ "  'Fixed'"
						+ "FROM "
						+ tempTableName
						+ " TF INNER JOIN SCAN_FINDING_RELATION_OVERVIEW O ON (O.PARENT_FINDING_ID = TF.ID OR O.CHILD_FINDING_ID = TF.ID) AND O.SCAN_ID = ? "
						+ "   LEFT OUTER JOIN SCAN_FINDING_RELATION_OVERVIEW L ON "
						+ "     L.PARENT_FINDING_ID = O.PARENT_FINDING_ID AND "
						+ "     L.CHILD_FINDING_ID = O.CHILD_FINDING_ID AND "
						+ "     L.RELATION_TYPE = O.RELATION_TYPE  AND L.SCAN_ID = ? "
						+ " WHERE L.CHILD_FINDING_ID IS NULL");
		populateFindingOverviewCurrentFindings = conn
				.prepareStatement("INSERT INTO FINDINGS_OVERVIEW (FINDING_ID,PROJECT_ID,AUDITED,LAST_CHANGED,IMPORTANCE,STATUS,LINE_OF_CODE,ARTIFACT_COUNT,AUDIT_COUNT,PROJECT,PACKAGE,CLASS,FINDING_TYPE,CATEGORY,TOOL,SUMMARY,CU,ASSURANCE_TYPE)"
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
						+ "	            WHEN RECENT.ID IS NOT NULL THEN 'New'"
						+ "	            ELSE 'Unchanged'"
						+ "        END,"
						+ "        SO.LINE_OF_CODE,"
						+ "        SO.ARTIFACT_COUNT,"
						+ "        CASE WHEN COUNT.COUNT IS NULL THEN 0 ELSE COUNT.COUNT END,"
						+ "        ?,"
						+ "        LM.PACKAGE_NAME,"
						+ "        LM.CLASS_NAME,"
						+ "        FT.UUID,"
						+ "        FC.NAME,"
						+ "        SO.TOOL,"
						+ "        F.SUMMARY,"
						+ "        SO.CU,"
						+ "        SO.ASSURANCE_TYPE"
						+ " FROM"
						+ "    "
						+ tempTableName
						+ " TF"
						+ "    INNER JOIN FINDING F ON F.ID = TF.ID"
						+ "    LEFT OUTER JOIN RECENT_FINDINGS RECENT ON RECENT.ID = F.ID"
						+ "    INNER JOIN SCAN_OVERVIEW SO ON SO.FINDING_ID = F.ID AND SO.SCAN_ID = ?"
						+ "    LEFT OUTER JOIN ("
						+ "       SELECT"
						+ "          A.FINDING_ID \"ID\", COUNT(*) \"COUNT\""
						+ "       FROM SIERRA_AUDIT A"
						+ "       GROUP BY A.FINDING_ID) AS COUNT ON COUNT.ID = F.ID"
						+ "    INNER JOIN LOCATION_MATCH LM ON LM.FINDING_ID = F.ID"
						+ "    INNER JOIN FINDING_TYPE FT ON FT.ID = LM.FINDING_TYPE_ID"
						+ "    INNER JOIN FINDING_CATEGORY FC ON FC.ID = FT.CATEGORY_ID");
		populateFindingOverviewFixedFindings = conn
				.prepareStatement("INSERT INTO FINDINGS_OVERVIEW (FINDING_ID,PROJECT_ID,AUDITED,LAST_CHANGED,IMPORTANCE,STATUS,LINE_OF_CODE,ARTIFACT_COUNT,AUDIT_COUNT,PROJECT,PACKAGE,CLASS,FINDING_TYPE,CATEGORY,TOOL,SUMMARY,CU,ASSURANCE_TYPE)"
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
						+ "        'Fixed',"
						+ "        SO.LINE_OF_CODE,"
						+ "        CASE WHEN SO.ARTIFACT_COUNT IS NULL THEN 0 ELSE SO.ARTIFACT_COUNT END,"
						+ "        CASE WHEN COUNT.COUNT IS NULL THEN 0 ELSE COUNT.COUNT END,"
						+ "        ?,"
						+ "        LM.PACKAGE_NAME,"
						+ "        LM.CLASS_NAME,"
						+ "        FT.UUID,"
						+ "        FC.NAME,"
						+ "        SO.TOOL,"
						+ "        F.SUMMARY,"
						+ "        SO.CU,"
						+ "        SO.ASSURANCE_TYPE"
						+ " FROM"
						+ "    "
						+ tempTableName
						+ " TF"
						+ "    INNER JOIN FINDING F ON F.ID = TF.ID"
						+ "    INNER JOIN FIXED_FINDINGS FIXED ON FIXED.ID = F.ID"
						+ "    INNER JOIN SCAN_OVERVIEW SO ON SO.FINDING_ID = F.ID AND SO.SCAN_ID = ?"
						+ "    LEFT OUTER JOIN ("
						+ "       SELECT"
						+ "          A.FINDING_ID \"ID\", COUNT(*) \"COUNT\""
						+ "       FROM SIERRA_AUDIT A"
						+ "       GROUP BY A.FINDING_ID) AS COUNT ON COUNT.ID = F.ID"
						+ "    INNER JOIN LOCATION_MATCH LM ON LM.FINDING_ID = F.ID"
						+ "    INNER JOIN FINDING_TYPE FT ON FT.ID = LM.FINDING_TYPE_ID"
						+ "    INNER JOIN FINDING_CATEGORY FC ON FC.ID = FT.CATEGORY_ID");
		deleteTempIds = conn.prepareStatement("DELETE FROM " + tempTableName);
		insertTempId = conn.prepareStatement("INSERT INTO " + tempTableName
				+ " (ID) VALUES (?)");
		checkAndInsertTempId = conn
				.prepareStatement("INSERT INTO "
						+ tempTableName
						+ " (ID) SELECT DISTINCT FINDING_ID FROM SCAN_OVERVIEW WHERE FINDING_ID = ?");
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
		deleteFindingFromOverview = conn
				.prepareStatement("DELETE FROM FINDINGS_OVERVIEW WHERE FINDING_ID = ?");
		deleteFindingRelationFromOverview = conn
				.prepareStatement("DELETE FROM FINDING_RELATION_OVERVIEW WHERE PARENT_FINDING_ID = ? OR CHILD_FINDING_ID = ?");
		deleteFindingRelationOverview = conn
				.prepareStatement("DELETE FROM FINDING_RELATION_OVERVIEW WHERE PROJECT_ID = ?");
		deleteOverview = conn
				.prepareStatement("DELETE FROM FINDINGS_OVERVIEW WHERE PROJECT_ID = ?");
		selectLatestScanByProject = conn
				.prepareStatement("SELECT SCAN_ID FROM LATEST_SCANS WHERE PROJECT = ?");
		selectOldestScanByProject = conn
				.prepareStatement("SELECT SCAN_ID FROM OLDEST_SCANS WHERE PROJECT = ?");
		findLocalAudits = conn
				.prepareStatement("SELECT F.ID,A.DATE_TIME,A.EVENT,A.VALUE"
						+ " FROM SIERRA_AUDIT A, FINDING F WHERE "
						+ " F.IS_READ = 'Y' AND A.REVISION IS NULL AND"
						+ " F.ID = A.FINDING_ID AND F.PROJECT_ID = ?"
						+ " ORDER BY A.FINDING_ID");
		selectUnrevisionedAudits = conn
				.prepareStatement("SELECT SA.ID FROM SIERRA_AUDIT SA, FINDING F, PROJECT P WHERE P.NAME = ? AND F.PROJECT_ID = P.ID AND SA.FINDING_ID = F.ID AND SA.REVISION IS NULL");
		updateUnrevisionedAudit = conn
				.prepareStatement("UPDATE SIERRA_AUDIT SET REVISION = ?, USER_ID = ? WHERE ID = ?");
		selectLocalMerge = conn
				.prepareStatement("SELECT F.SUMMARY,F.IMPORTANCE,LM.PACKAGE_NAME,LM.CLASS_NAME,LM.HASH,FT.UUID,LM.REVISION"
						+ "   FROM LOCATION_MATCH LM, FINDING F, FINDING_TYPE FT"
						+ "   WHERE F.ID = ? AND LM.FINDING_ID = F.ID AND FT.ID = LM.FINDING_TYPE_ID");
		selectArtifactsByCompilation = conn
				.prepareStatement("SELECT A.ID,A.PRIORITY,A.SEVERITY,A.MESSAGE,S.PROJECT_ID,SL.HASH,SL.CLASS_NAME,CU.PACKAGE_NAME,ART.FINDING_TYPE_ID"
						+ " FROM SCAN S, ARTIFACT A, ARTIFACT_TYPE ART, SOURCE_LOCATION SL, COMPILATION_UNIT CU"
						+ " WHERE"
						+ " S.ID = ? AND A.SCAN_ID = S.ID AND SL.ID = A.PRIMARY_SOURCE_LOCATION_ID AND CU.ID = SL.COMPILATION_UNIT_ID AND ART.ID = A.ARTIFACT_TYPE_ID AND CU.PACKAGE_NAME = ? AND CU.CU = ?");
		selectArtifactsByTool = conn
				.prepareStatement("SELECT A.ID,A.PRIORITY,A.SEVERITY,A.MESSAGE,S.PROJECT_ID,SL.HASH,SL.CLASS_NAME,CU.PACKAGE_NAME,ART.FINDING_TYPE_ID"
						+ " FROM SCAN S, ARTIFACT A, ARTIFACT_TYPE ART, TOOL T, SOURCE_LOCATION SL, COMPILATION_UNIT CU"
						+ " WHERE"
						+ " S.ID = ? AND A.SCAN_ID = S.ID AND SL.ID = A.PRIMARY_SOURCE_LOCATION_ID AND CU.ID = SL.COMPILATION_UNIT_ID AND ART.ID = A.ARTIFACT_TYPE_ID AND ART.TOOL_ID = T.ID AND T.NAME = ?");
		selectFindingFindingType = conn
				.prepareStatement("SELECT DISTINCT FT.UUID FROM LOCATION_MATCH LM, FINDING_TYPE FT WHERE FT.ID = LM.FINDING_TYPE_ID AND LM.FINDING_ID = ?");
	}

	/**
	 * Make a user comment on an existing finding. This method is for use by a
	 * client.
	 * 
	 * @param findingId
	 * @param comment
	 * @throws SQLException
	 */
	public void comment(final long findingId, final String comment)
			throws SQLException {
		checkFinding(findingId);
		final Timestamp now = JDBCUtils.now();
		comment(null, findingId, comment, now, null);
		int idx = 1;
		updateFindingOverviewComment.setTimestamp(idx++, now);
		updateFindingOverviewComment.setTimestamp(idx++, now);
		updateFindingOverviewComment.setLong(idx++, findingId);
		updateFindingOverviewComment.execute();
	}

	/**
	 * Set the importance of a particular finding. This method is for use by a
	 * client.
	 * 
	 * @param findingId
	 * @param importance
	 * @throws SQLException
	 */
	public void setImportance(final long findingId, final Importance importance)
			throws SQLException {
		checkFinding(findingId);
		final Timestamp now = JDBCUtils.now();
		setImportance(null, findingId, importance, now, null);
		int idx = 1;
		updateFindingOverviewImportance.setTimestamp(idx++, now);
		updateFindingOverviewImportance.setTimestamp(idx++, now);
		updateFindingOverviewImportance.setString(idx++, importance
				.toStringSentenceCase());
		updateFindingOverviewImportance.setLong(idx++, findingId);
		updateFindingOverviewImportance.execute();
	}

	public void setImportance(final Collection<Long> findingIds,
			final Importance importance, final SLProgressMonitor monitor)
			throws SQLException {
		monitor.begin(findingIds.size());
		final Timestamp now = JDBCUtils.now();
		for (final Long findingId : findingIds) {
			checkFinding(findingId);
			setImportance(null, findingId, importance, now, null);
			int idx = 1;
			updateFindingOverviewImportance.setTimestamp(idx++, now);
			updateFindingOverviewImportance.setTimestamp(idx++, now);
			updateFindingOverviewImportance.setString(idx++, importance
					.toStringSentenceCase());
			updateFindingOverviewImportance.setLong(idx++, findingId);
			updateFindingOverviewImportance.execute();
			monitor.worked(1);
		}
		monitor.done();
	}

	/**
	 * Change the summary that should be displayed for a particular finding.
	 * 
	 * @param findingId
	 * @param summary
	 * @throws SQLException
	 */
	public void changeSummary(final long findingId, final String summary)
			throws SQLException {
		checkFinding(findingId);
		final Timestamp now = JDBCUtils.now();
		changeSummary(null, findingId, summary, now, null);
		int idx = 1;
		updateFindingOverviewSummary.setTimestamp(idx++, now);
		updateFindingOverviewSummary.setTimestamp(idx++, now);
		updateFindingOverviewSummary.setString(idx++, summary);
		updateFindingOverviewSummary.setLong(idx++, findingId);
		updateFindingOverviewSummary.execute();
	}

	/**
	 * Regenerate the findings overview for the given project.
	 * 
	 * @param projectName
	 * @param scan
	 *            the uid of the latest scan. This must be the latest scan.
	 * @param monitor
	 * @throws SQLException
	 */
	public void generateOverview(final String projectName, final String scan,
			final SLProgressMonitor monitor) throws SQLException {
		monitor.subTask("Generating overview");

		final ProjectRecord p = ProjectRecordFactory.getInstance(conn)
				.newProject();
		p.setName(projectName);
		if (p.select()) {
			final ScanRecord scanRecord = ScanRecordFactory.getInstance(conn)
					.newScan();
			scanRecord.setUid(scan);
			if (scanRecord.select()) {
				final boolean debug = log.isLoggable(Level.FINE);
				if (debug) {
					log.fine("Populating scan overview for scan "
							+ scanRecord.getUid() + ".");
				}
				populateScanOverview(scanRecord.getId());
				monitor.worked(1);

				if (debug) {
					log.fine("Clearing overview for project " + p.getName()
							+ ".");
				}
				deleteOverview.setLong(1, p.getId());
				deleteOverview.execute();
				deleteFindingRelationOverview.setLong(1, p.getId());
				deleteFindingRelationOverview.execute();
				monitor.worked(1);

				if (debug) {
					log.fine("Calculating ids in overview for project "
							+ p.getName() + ".");
				}
				int idx = 1;
				populateTempIds.setString(idx++, projectName);
				populateTempIds.setString(idx++, projectName);
				populateTempIds.execute();
				monitor.worked(1);

				if (debug) {
					log.fine("Populating overview for project " + p.getName()
							+ ".");
				}
				idx = 1;
				populateFindingOverviewCurrentFindings.setString(idx++,
						projectName);
				populateFindingOverviewCurrentFindings.setLong(idx++,
						scanRecord.getId());
				populateFindingOverviewCurrentFindings.execute();
				selectOldestScanByProject.setString(1, projectName);
				final ResultSet set = selectOldestScanByProject.executeQuery();
				try {
					if (set.next()) {
						final long oldestScanId = set.getLong(1);
						idx = 1;
						populateFindingOverviewFixedFindings.setString(idx++,
								projectName);
						populateFindingOverviewFixedFindings.setLong(idx++,
								oldestScanId);
						populateFindingOverviewFixedFindings.execute();

						populateFindingRelationOverviewCurrentFindings.setLong(
								1, p.getId());
						populateFindingRelationOverviewCurrentFindings.setLong(
								2, scanRecord.getId());
						populateFindingRelationOverviewCurrentFindings.setLong(
								3, oldestScanId);
						populateFindingRelationOverviewCurrentFindings
								.execute();
						populateFindingRelationOverviewFixedFindings.setLong(1,
								p.getId());
						populateFindingRelationOverviewFixedFindings.setLong(2,
								oldestScanId);
						populateFindingRelationOverviewFixedFindings.setLong(3,
								scanRecord.getId());
						populateFindingRelationOverviewFixedFindings.execute();
					} else {
						// We still need to populate new finding relations
						populateFindingRelationOverviewCurrentFindings.setLong(
								1, p.getId());
						populateFindingRelationOverviewCurrentFindings.setLong(
								2, scanRecord.getId());
						populateFindingRelationOverviewCurrentFindings.setLong(
								3, -1);
						populateFindingRelationOverviewCurrentFindings
								.execute();
					}
				} finally {
					set.close();
				}
				monitor.worked(1);

				log.fine("Deleting temp ids");
				deleteTempIds.execute();

				if (debug) {
					log.fine("Calculating finding relations");
				}
				monitor.worked(1);
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
	public void deleteFindings(final String projectName,
			final SLProgressMonitor monitor) throws SQLException {
		final ProjectRecord pRec = ProjectRecordFactory.getInstance(conn)
				.newProject();
		pRec.setName(projectName);
		if (pRec.select()) {
			deleteOverview.setLong(1, pRec.getId());
			deleteOverview.execute();
			deleteFindingRelationOverview.setLong(1, pRec.getId());
			deleteFindingRelationOverview.execute();
		}
		super.deleteFindings(projectName, monitor);
	}

	/**
	 * Update the findings for a scan that has been partially updated. This
	 * should also regenerate the findings overview.
	 * 
	 * @param projectName
	 * @param uid
	 * @param tools
	 * @param filter
	 * @param previousFindingIds
	 *            the set of finding ids belonging to the compilation units
	 *            before the scan was altered
	 * @param monitor
	 */
	public void updateScanFindings(final String projectName, final String uid,
			final List<String> tools, final FindingFilter filter,
			final Set<Long> previousFindingIds, final SLProgressMonitor monitor) {
		final Set<Long> scanFindingIds = new HashSet<Long>();
		try {
			final ScanRecord scan = ScanRecordFactory.getInstance(conn)
					.newScan();
			scan.setUid(uid);
			if (!scan.select()) {
				throw new IllegalArgumentException("No scan with uid " + uid
						+ " exists in the database");
			}
			final Long projectId = scan.getProjectId();
			int total = 0;
			for (final String tool : tools) {
				int counter = 0;
				int idx = 1;
				selectArtifactsByTool.setLong(idx++, scan.getId());
				selectArtifactsByTool.setString(idx++, tool);
				final ResultSet result = selectArtifactsByTool.executeQuery();
				try {
					while (result.next()) {
						final ArtifactResult art = createArtifactResult(result);
						final Long findingId = getFindingId(filter, projectId,
								art);
						final LongRelationRecord afr = factory
								.newArtifactFinding();
						afr.setId(new RelationRecord.PK<Long, Long>(art.id,
								findingId));
						afr.insert();
						if ((++counter % FETCH_SIZE) == 0) {
							conn.commit();
						}
						if ((counter % CHECK_SIZE) == 0) {
							if (monitor != null) {
								if (monitor.isCanceled()) {
									conn.rollback();
									ScanManager.getInstance(conn).deleteScan(
											uid, null);
									return;
								}
								monitor.worked(1);
							}
						}
						scanFindingIds.add(findingId);
					}
				} finally {
					result.close();
				}
				total += counter;
			}
			conn.commit();
			generatePartialScanOverview(scan.getId(), scanFindingIds);
			/*
			 * Regenerate the findings overview for all current and previous
			 * findings.
			 */
			scanFindingIds.addAll(previousFindingIds);
			regenerateFindingsOverview(projectName, scanFindingIds, monitor);
			if (log.isLoggable(Level.FINE)) {
				log.fine("All new findings (" + total + ") persisted for scan "
						+ uid + " in project " + projectName + ".");
			}
		} catch (final SQLException e) {
			sqlError(e);
		}
	}

	/**
	 * Update the findings for a scan that has been partially updated. This
	 * should also regenerate the findings overview.
	 * 
	 * @param projectName
	 * @param uid
	 * @param compilations
	 * @param filter
	 * @param previousFindingIds
	 *            the set of finding ids belonging to the compilation units
	 *            before the scan was altered
	 * @param monitor
	 */
	public void updateScanFindings(final String projectName, final String uid,
			final Map<String, List<String>> compilations,
			final FindingFilter filter, final Set<Long> previousFindingIds,
			final SLProgressMonitor monitor) {
		final Set<Long> scanFindingIds = new HashSet<Long>();
		try {
			final ScanRecord scan = ScanRecordFactory.getInstance(conn)
					.newScan();
			scan.setUid(uid);
			if (!scan.select()) {
				throw new IllegalArgumentException("No scan with uid " + uid
						+ " exists in the database");
			}
			final Long projectId = scan.getProjectId();
			int total = 0;
			for (final Entry<String, List<String>> packageCompilations : compilations
					.entrySet()) {
				final String pakkage = packageCompilations.getKey();
				int counter = 0;
				for (final String compilation : packageCompilations.getValue()) {
					int idx = 1;
					selectArtifactsByCompilation.setLong(idx++, scan.getId());
					selectArtifactsByCompilation.setString(idx++, pakkage);
					selectArtifactsByCompilation.setString(idx++, compilation);
					final ResultSet result = selectArtifactsByCompilation
							.executeQuery();
					try {
						while (result.next()) {
							final ArtifactResult art = createArtifactResult(result);
							final Long findingId = getFindingId(filter,
									projectId, art);
							final LongRelationRecord afr = factory
									.newArtifactFinding();
							afr.setId(new RelationRecord.PK<Long, Long>(art.id,
									findingId));
							afr.insert();
							if ((++counter % FETCH_SIZE) == 0) {
								conn.commit();
							}
							if ((counter % CHECK_SIZE) == 0) {
								if (monitor != null) {
									if (monitor.isCanceled()) {
										conn.rollback();
										ScanManager.getInstance(conn)
												.deleteScan(uid, null);
										return;
									}
									monitor.worked(1);
								}
							}
							scanFindingIds.add(findingId);
						}
					} finally {
						result.close();
					}
				}
				total += counter;
			}
			conn.commit();
			generatePartialScanOverview(scan.getId(), scanFindingIds);
			/*
			 * Regenerate the findings overview for all current and previous
			 * findings.
			 */
			scanFindingIds.addAll(previousFindingIds);
			regenerateFindingsOverview(projectName, scanFindingIds, monitor);
			if (log.isLoggable(Level.FINE)) {
				log.fine("All new findings (" + total + ") persisted for scan "
						+ uid + " in project " + projectName + ".");
			}
		} catch (final SQLException e) {
			sqlError(e);
		}
	}

	private void generatePartialScanOverview(final long scanId,
			final Set<Long> findingIds) throws SQLException {
		for (final long id : findingIds) {
			insertTempId.setLong(1, id);
			insertTempId.execute();
		}
		populatePartialScanOverview.setLong(1, scanId);
		populatePartialScanOverview.setLong(2, scanId);
		populatePartialScanOverview.execute();
		populatePartialScanRelationOverview.setLong(1, scanId);
		populatePartialScanRelationOverview.execute();
		deleteTempIds.execute();
		if (log.isLoggable(Level.FINE)) {
			log.fine("Generated partial scan overview for scan " + scanId
					+ ": " + findingIds.size() + " new");
		}
	}

	/**
	 * Regenerate findings overview information for the selected findings. This
	 * method cannot add new findings to the overview, merely regenerate
	 * existing ones.
	 * 
	 * @param findingIds
	 */
	private void regenerateFindingsOverview(final String projectName,
			final Set<Long> findingIds, final SLProgressMonitor monitor)
			throws SQLException {
		int count = 0;
		for (final long id : findingIds) {
			deleteFindingFromOverview.setLong(1, id);
			deleteFindingFromOverview.executeUpdate();
			deleteFindingRelationFromOverview.setLong(1, id);
			deleteFindingRelationFromOverview.setLong(2, id);
			deleteFindingRelationFromOverview.execute();
			checkAndInsertTempId.setLong(1, id);
			checkAndInsertTempId.execute();
			if (count++ % 3 == 0) {
				monitor.worked(1);
			}
		}
		final ProjectRecord p = ProjectRecordFactory.getInstance(conn)
				.newProject();
		p.setName(projectName);
		p.select();
		selectLatestScanByProject.setString(1, projectName);
		final ResultSet latestScanSet = selectLatestScanByProject
				.executeQuery();
		try {
			if (latestScanSet.next()) {
				final long latestScanId = latestScanSet.getLong(1);
				populateFindingOverviewCurrentFindings
						.setString(1, projectName);
				populateFindingOverviewCurrentFindings.setLong(2, latestScanId);
				populateFindingOverviewCurrentFindings.execute();
				selectOldestScanByProject.setString(1, projectName);
				final ResultSet oldestScanSet = selectOldestScanByProject
						.executeQuery();
				try {
					if (oldestScanSet.next()) {
						final long oldestScanId = oldestScanSet.getLong(1);
						populateFindingOverviewFixedFindings.setString(1,
								projectName);
						populateFindingOverviewFixedFindings.setLong(2,
								oldestScanId);
						populateFindingOverviewFixedFindings.execute();

						populateFindingRelationOverviewCurrentFindings.setLong(
								1, p.getId());
						populateFindingRelationOverviewCurrentFindings.setLong(
								2, latestScanId);
						populateFindingRelationOverviewCurrentFindings.setLong(
								3, oldestScanId);
						populateFindingRelationOverviewCurrentFindings
								.execute();
						populateFindingRelationOverviewFixedFindings.setLong(1,
								p.getId());
						populateFindingRelationOverviewFixedFindings.setLong(2,
								oldestScanId);
						populateFindingRelationOverviewFixedFindings.setLong(3,
								latestScanId);
						populateFindingRelationOverviewFixedFindings.execute();
					} else {
						// We still need to populate new relations
						populateFindingRelationOverviewCurrentFindings.setLong(
								1, p.getId());
						populateFindingRelationOverviewCurrentFindings.setLong(
								2, latestScanId);
						populateFindingRelationOverviewCurrentFindings.setLong(
								3, -1);
						populateFindingRelationOverviewCurrentFindings
								.execute();
					}
				} finally {
					oldestScanSet.close();
				}
				deleteTempIds.execute();
			}
		} finally {
			latestScanSet.close();
		}
		if (log.isLoggable(Level.FINE)) {
			log.info("Regenerated findings overview for project " + projectName
					+ ": " + findingIds.size() + " old");
		}
	}

	/*
	 * public List<> countNewLocalAudits(String projectName) { }
	 */

	private abstract class AuditHandler<T> {
		@SuppressWarnings("unchecked")
		List<T> results = new ArrayList();

		abstract void handle(ResultSet audits) throws SQLException;
	}

	private <T> void processNewLocalAudits(final String projectName,
			final AuditHandler<T> handler) throws SQLException {
		final ProjectRecord rec = ProjectRecordFactory.getInstance(conn)
				.newProject();
		rec.setName(projectName);
		if (rec.select()) {
			findLocalAudits.setLong(1, rec.getId());
			final ResultSet set = findLocalAudits.executeQuery();
			try {
				handler.handle(set);
			} finally {
				set.close();
			}
		} else {
			throw new IllegalArgumentException("No project with name "
					+ projectName + " exists.");
		}
	}

	public List<FindingAudits> getNewLocalAudits(final String projectName)
			throws SQLException {
		final AuditHandler<FindingAudits> handler = new AuditHandler<FindingAudits>() {
			@Override
			void handle(final ResultSet set) throws SQLException {
				long oldId = -1;
				List<Audit> audits = null;
				while (set.next()) {
					int idx = 1;
					final long newId = set.getLong(idx++);
					if (newId != oldId) {
						oldId = newId;
						audits = new ArrayList<Audit>();
						results.add(new FindingAudits(newId, audits));
					}
					final Audit a = new Audit(set.getTimestamp(idx++),
							AuditEvent.valueOf(set.getString(idx++)), set
									.getString(idx++));
					audits.add(a);
				}
			}
		};
		processNewLocalAudits(projectName, handler);
		return handler.results;
	}

	public List<SyncTrailRequest> getNewLocalAuditTrails(
			final String projectName, final SLProgressMonitor monitor)
			throws SQLException {
		final AuditHandler<SyncTrailRequest> handler = new AuditHandler<SyncTrailRequest>() {
			@Override
			void handle(final ResultSet audits) throws SQLException {
				getSyncTrailRequests(results, audits);
			}
		};
		processNewLocalAudits(projectName, handler);
		return handler.results;
	}

	/**
	 * @param set
	 *            The set of local audits
	 */
	private void getSyncTrailRequests(final List<SyncTrailRequest> trails,
			final ResultSet set) throws SQLException {
		long oldId = -1;
		List<Audit> audits = null;
		while (set.next()) {
			int idx = 1;
			final long newId = set.getLong(idx++);
			if (!(newId == oldId)) {
				oldId = newId;
				audits = new LinkedList<Audit>();
				final SyncTrailRequest trail = new SyncTrailRequest();
				selectLocalMerge.setLong(1, oldId);
				final ResultSet mergeSet = selectLocalMerge.executeQuery();
				final Merge merge = new Merge();
				final Match match = new Match();
				merge.setMatch(match);
				try {
					mergeSet.next();
					int mergeIdx = 1;
					merge.setSummary(mergeSet.getString(mergeIdx++));
					merge.setImportance(Importance.values()[mergeSet
							.getInt(mergeIdx++)]);
					match.setPackageName(mergeSet.getString(mergeIdx++));
					match.setClassName(mergeSet.getString(mergeIdx++));
					match.setHash(mergeSet.getLong(mergeIdx++));
					match.setFindingType(mergeSet.getString(mergeIdx++));
					match.setRevision(mergeSet.getLong(mergeIdx++));
				} finally {
					mergeSet.close();
				}
				trail.setMerge(merge);
				trail.setAudits(audits);
				trails.add(trail);
			}
			audits.add(new Audit(set.getTimestamp(idx++), AuditEvent
					.valueOf(set.getString(idx++)), set.getString(idx++)));
		}
	}

	public void updateLocalFindings(final String projectName,
			final List<SyncTrailResponse> trails,
			final SLProgressMonitor monitor) throws SQLException {
		final ProjectRecord project = ProjectRecordFactory.getInstance(conn)
				.newProject();
		project.setName(projectName);
		if (!project.select()) {
			project.insert();
		}
		final Set<Long> findingIds = new HashSet<Long>();
		final MatchRecord match = factory.newMatch();
		final MatchRecord.PK id = new MatchRecord.PK();
		match.setId(id);
		for (final SyncTrailResponse trail : trails) {
			final String findingUid = trail.getFinding();
			final Merge merge = trail.getMerge();
			final Match m = merge.getMatch();
			id.setClassName(m.getClassName());
			id.setFindingTypeId(ftManager.getFindingTypeId(m.getFindingType()));
			id.setHash(m.getHash());
			id.setPackageName(m.getPackageName());
			id.setProjectId(project.getId());
			long findingId;
			if (match.select()) {
				final FindingRecord finding = getFinding(match.getFindingId());
				if (!findingUid.equals(finding.getUid())) {
					finding.setUid(findingUid);
					finding.setImportance(merge.getImportance());
					finding.setSummary(merge.getSummary());
					finding.update();
				}
				findingId = finding.getId();
				if (!(m.getRevision().equals(match.getRevision()))) {
					match.setRevision(m.getRevision());
					match.update();
				}
			} else {
				final FindingRecord finding = factory.newFinding();
				finding.setImportance(merge.getImportance());
				finding.setProjectId(project.getId());
				finding.setSummary(merge.getSummary());
				finding.setUid(findingUid);
				finding.insert();
				findingId = finding.getId();
				match.setFindingId(findingId);
				match.setRevision(m.getRevision());
				match.insert();
			}
			findingIds.add(findingId);
			for (final Audit a : trail.getAudits()) {
				final Long userId = getUserId(a.getUser());
				switch (a.getEvent()) {
				case COMMENT:
					comment(userId, findingId, a.getValue(), a.getTimestamp(),
							a.getRevision());
					break;
				case IMPORTANCE:
					setImportance(userId, findingId, Importance.valueOf(a
							.getValue()), a.getTimestamp(), a.getRevision());
					break;
				case READ:
					markAsRead(userId, findingId, a.getTimestamp(), a
							.getRevision());
					break;
				case SUMMARY:
					changeSummary(userId, findingId, a.getValue(), a
							.getTimestamp(), a.getRevision());
					break;
				default:
				}
			}
		}
		regenerateFindingsOverview(projectName, findingIds, monitor);
	}

	/**
	 * Remove the finding type linked to this finding id from the global
	 * settings.
	 * 
	 * @param findingId
	 * @param monitor
	 * @throws SQLException
	 */
	public void filterFindingTypeFromScans(final long findingId,
			final SLProgressMonitor monitor) throws SQLException {
		selectFindingFindingType.setLong(1, findingId);
		final ResultSet set = selectFindingFindingType.executeQuery();
		try {
			set.next();
			final String type = set.getString(1);
			final ScanFilters filters = new ScanFilters(new ConnectionQuery(
					conn));
			final ScanFilterDO scanFilter = filters
					.getScanFilter(SettingQueries.LOCAL_UUID);
			for (final TypeFilterDO filter : scanFilter.getFilterTypes()) {
				if (type.equals(filter.getFindingType())) {
					filter.setFiltered(true);
					filters.writeScanFilter(scanFilter);
					return;
				}
			}
			// We did not find the type filter, so we make one
			final TypeFilterDO filter = new TypeFilterDO(type, null, true);
			scanFilter.getFilterTypes().add(filter);
			filters.writeScanFilter(scanFilter);
		} finally {
			set.close();
		}
	}

	/**
	 * Check for existence of the finding, and return it's project.
	 * 
	 * @param findingId
	 * @throws SQLException
	 */
	private void checkFinding(final long findingId) throws SQLException {
		if (getFinding(findingId) == null) {
			throw new IllegalArgumentException(findingId
					+ " is not a valid finding id.");
		}
	}

	public static ClientFindingManager getInstance(final Connection conn)
			throws SQLException {
		return new ClientFindingManager(conn);
	}

	public void updateLocalAuditRevision(final String projectName,
			final String user, final long commitRevision,
			final SLProgressMonitor monitor) throws SQLException {
		final long userId = getUserId(user);
		selectUnrevisionedAudits.setString(1, projectName);
		final ResultSet set = selectUnrevisionedAudits.executeQuery();
		try {
			while (set.next()) {
				updateUnrevisionedAudit.setLong(1, commitRevision);
				updateUnrevisionedAudit.setLong(2, userId);
				updateUnrevisionedAudit.setLong(3, set.getLong(1));
				updateUnrevisionedAudit.execute();
			}
		} finally {
			set.close();
		}
	}

}
