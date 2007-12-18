package com.surelogic.sierra.jdbc.finding;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.logging.Level;

import com.surelogic.common.SLProgressMonitor;
import com.surelogic.sierra.jdbc.DBType;
import com.surelogic.sierra.jdbc.EmptyProgressMonitor;
import com.surelogic.sierra.jdbc.JDBCUtils;
import com.surelogic.sierra.jdbc.project.ProjectRecordFactory;
import com.surelogic.sierra.jdbc.record.FindingRecord;
import com.surelogic.sierra.jdbc.record.LongRelationRecord;
import com.surelogic.sierra.jdbc.record.MatchRecord;
import com.surelogic.sierra.jdbc.record.ProjectRecord;
import com.surelogic.sierra.jdbc.record.RelationRecord;
import com.surelogic.sierra.jdbc.record.ScanRecord;
import com.surelogic.sierra.jdbc.scan.ScanManager;
import com.surelogic.sierra.jdbc.scan.ScanRecordFactory;
import com.surelogic.sierra.jdbc.settings.SettingsManager;
import com.surelogic.sierra.jdbc.tool.FindingFilter;
import com.surelogic.sierra.tool.message.Audit;
import com.surelogic.sierra.tool.message.AuditEvent;
import com.surelogic.sierra.tool.message.AuditTrail;
import com.surelogic.sierra.tool.message.AuditTrailUpdate;
import com.surelogic.sierra.tool.message.FindingTypeFilter;
import com.surelogic.sierra.tool.message.Importance;
import com.surelogic.sierra.tool.message.Match;
import com.surelogic.sierra.tool.message.Merge;
import com.surelogic.sierra.tool.message.MergeResponse;
import com.surelogic.sierra.tool.message.Priority;
import com.surelogic.sierra.tool.message.Severity;
import com.surelogic.sierra.tool.message.TrailObsoletion;

public final class ClientFindingManager extends FindingManager {

	private final PreparedStatement populatePartialScanOverview;
	private final PreparedStatement selectArtifactsByCompilation;
	private final PreparedStatement deleteFindingFromOverview;
	private final PreparedStatement deleteOverview;
	private final PreparedStatement checkAndInsertTempId;
	private final PreparedStatement insertTempId;
	private final PreparedStatement populateTempIds;
	private final PreparedStatement deleteTempIds;
	private final PreparedStatement populateFindingOverviewCurrentFindings;
	private final PreparedStatement populateFindingOverviewFixedFindings;
	private final PreparedStatement updateFindingOverviewImportance;
	private final PreparedStatement updateFindingOverviewSummary;
	private final PreparedStatement updateFindingOverviewComment;
	private final PreparedStatement selectLatestScanByProject;
	private final PreparedStatement selectOldestScanByProject;
	private final PreparedStatement updateFindingUid;
	private final PreparedStatement updateMatchRevision;
	private final PreparedStatement findLocalMerges;
	private final PreparedStatement findLocalAudits;
	private final PreparedStatement selectFindingFindingType;

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
					log.log(Level.WARNING, e.getMessage(), e);
					// Do nothing, the table is probably already there.
				}
				tempTableName = "TEMP_FINDING_IDS";
			} else {
				try {
					st
							.execute("DECLARE GLOBAL TEMPORARY TABLE TEMP_FINDING_IDS (ID BIGINT NOT NULL) NOT LOGGED");
				} catch (SQLException e) {
					log.log(Level.WARNING, e.getMessage(), e);
					// Do nothing, the table is probably already there.
				}
				tempTableName = "SESSION.TEMP_FINDING_IDS";
			}
		} finally {
			st.close();
		}
		populatePartialScanOverview = conn
				.prepareStatement("INSERT INTO SCAN_OVERVIEW (FINDING_ID,SCAN_ID,LINE_OF_CODE,ARTIFACT_COUNT,TOOL,CU)"
						+ " SELECT AFR.FINDING_ID, ?, MAX(SL.LINE_OF_CODE), COUNT(AFR.ARTIFACT_ID), "
						+ "        CASE WHEN COUNT(DISTINCT T.ID) = 1 THEN MAX(T.NAME) ELSE '(From Multiple Tools)' END,"
						+ "        MAX(CU.CU)"
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
		String beginFindingOverviewUpdate = "UPDATE FINDINGS_OVERVIEW"
				+ " SET AUDITED = 'Yes'"
				+ ", LAST_CHANGED = CASE WHEN (? > LAST_CHANGED) THEN ? ELSE LAST_CHANGED END"
				+ ", AUDIT_COUNT = AUDIT_COUNT + 1";
		String endFindingOverviewUpdate = " WHERE FINDING_ID = ?";
		updateFindingOverviewImportance = conn
				.prepareStatement(beginFindingOverviewUpdate
						+ ", IMPORTANCE = ?" + endFindingOverviewUpdate);
		updateFindingOverviewSummary = conn
				.prepareStatement(beginFindingOverviewUpdate + ", SUMMARY = ?"
						+ endFindingOverviewUpdate);
		updateFindingOverviewComment = conn
				.prepareStatement(beginFindingOverviewUpdate
						+ endFindingOverviewUpdate);
		populateFindingOverviewCurrentFindings = conn
				.prepareStatement("INSERT INTO FINDINGS_OVERVIEW (FINDING_ID,PROJECT_ID,AUDITED,LAST_CHANGED,IMPORTANCE,STATUS,LINE_OF_CODE,ARTIFACT_COUNT,AUDIT_COUNT,PROJECT,PACKAGE,CLASS,FINDING_TYPE,CATEGORY,TOOL,SUMMARY,CU)"
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
						+ "        FT.NAME,"
						+ "        FC.NAME,"
						+ "        SO.TOOL,"
						+ "        F.SUMMARY,"
						+ "        SO.CU"
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
				.prepareStatement("INSERT INTO FINDINGS_OVERVIEW (FINDING_ID,PROJECT_ID,AUDITED,LAST_CHANGED,IMPORTANCE,STATUS,LINE_OF_CODE,ARTIFACT_COUNT,AUDIT_COUNT,PROJECT,PACKAGE,CLASS,FINDING_TYPE,CATEGORY,TOOL,SUMMARY,CU)"
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
						+ "        FT.NAME,"
						+ "        FC.NAME,"
						+ "        SO.TOOL,"
						+ "        F.SUMMARY,"
						+ "        SO.CU"
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
		deleteOverview = conn
				.prepareStatement("DELETE FROM FINDINGS_OVERVIEW WHERE PROJECT_ID = ?");
		selectLatestScanByProject = conn
				.prepareStatement("SELECT SCAN_ID FROM LATEST_SCANS WHERE PROJECT = ?");
		selectOldestScanByProject = conn
				.prepareStatement("SELECT SCAN_ID FROM OLDEST_SCANS WHERE PROJECT = ?");
		updateFindingUid = conn
				.prepareStatement("UPDATE FINDING SET UUID = ? WHERE ID = ?");
		updateMatchRevision = conn
				.prepareStatement("UPDATE LOCATION_MATCH SET REVISION = ? WHERE FINDING_ID = ? AND REVISION IS NULL");
		findLocalMerges = conn
				.prepareStatement("SELECT M.FINDING_ID,M.PACKAGE_NAME,M.CLASS_NAME,M.HASH,FT.UUID"
						+ " FROM LOCATION_MATCH NEW_M, FINDING F, LOCATION_MATCH M, FINDING_TYPE FT"
						+ " WHERE NEW_M.PROJECT_ID = ? AND"
						+ " NEW_M.REVISION IS NULL AND"
						+ " F.ID = NEW_M.FINDING_ID AND"
						+ " F.IS_READ = 'Y' AND"
						+ " M.FINDING_ID = F.ID AND"
						+ " FT.ID = M.FINDING_TYPE_ID" + " ORDER BY FINDING_ID");
		findLocalAudits = conn
				.prepareStatement("SELECT F.UUID,A.DATE_TIME,A.EVENT,A.VALUE"
						+ " FROM SIERRA_AUDIT A, FINDING F WHERE "
						+ " F.IS_READ = 'Y' AND A.REVISION IS NULL AND"
						+ " F.ID = A.FINDING_ID AND F.PROJECT_ID = ?"
						+ " AND F.UUID IS NOT NULL ORDER BY A.FINDING_ID");
		selectArtifactsByCompilation = conn
				.prepareStatement("SELECT A.ID,A.PRIORITY,A.SEVERITY,A.MESSAGE,S.PROJECT_ID,SL.HASH,SL.CLASS_NAME,CU.PACKAGE_NAME,ART.FINDING_TYPE_ID"
						+ " FROM SCAN S, ARTIFACT A, ARTIFACT_TYPE ART, SOURCE_LOCATION SL, COMPILATION_UNIT CU"
						+ " WHERE"
						+ " S.ID = ? AND A.SCAN_ID = S.ID AND SL.ID = A.PRIMARY_SOURCE_LOCATION_ID AND CU.ID = SL.COMPILATION_UNIT_ID AND ART.ID = A.ARTIFACT_TYPE_ID AND CU.PACKAGE_NAME = ? AND CU.CU = ?");
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
	public void comment(long findingId, String comment) throws SQLException {
		checkFinding(findingId);
		Timestamp now = JDBCUtils.now();
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
	public void setImportance(long findingId, Importance importance)
			throws SQLException {
		checkFinding(findingId);
		Timestamp now = JDBCUtils.now();
		setImportance(null, findingId, importance, now, null);
		int idx = 1;
		updateFindingOverviewImportance.setTimestamp(idx++, now);
		updateFindingOverviewImportance.setTimestamp(idx++, now);
		updateFindingOverviewImportance.setString(idx++, importance
				.toStringSentenceCase());
		updateFindingOverviewImportance.setLong(idx++, findingId);
		updateFindingOverviewImportance.execute();
	}

	public void setImportance(Set<Long> findingIds, Importance importance,
			SLProgressMonitor monitor) throws SQLException {
		monitor.beginTask("Updating finding data", findingIds.size());
		Timestamp now = JDBCUtils.now();
		for (Long findingId : findingIds) {
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
	public void changeSummary(long findingId, String summary)
			throws SQLException {
		checkFinding(findingId);
		Timestamp now = JDBCUtils.now();
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
				populateFindingOverviewCurrentFindings.setString(idx++,
						projectName);
				populateFindingOverviewCurrentFindings.setLong(idx++,
						scanRecord.getId());
				populateFindingOverviewCurrentFindings.execute();
				selectOldestScanByProject.setString(1, projectName);
				final ResultSet set = selectOldestScanByProject.executeQuery();
				try {
					if (set.next()) {
						idx = 1;
						populateFindingOverviewFixedFindings.setString(idx++,
								projectName);
						populateFindingOverviewFixedFindings.setLong(idx++, set
								.getLong(1));
						populateFindingOverviewFixedFindings.execute();
					}
				} finally {
					set.close();
				}
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
	public void updateScanFindings(String projectName, String uid,
			Map<String, List<String>> compilations, FindingFilter filter,
			Set<Long> previousFindingIds, SLProgressMonitor monitor) {
		final Set<Long> scanFindingIds = new HashSet<Long>();
		try {
			ScanRecord scan = ScanRecordFactory.getInstance(conn).newScan();
			scan.setUid(uid);
			if (!scan.select()) {
				throw new IllegalArgumentException("No scan with uid " + uid
						+ " exists in the database");
			}
			Long projectId = scan.getProjectId();
			for (Entry<String, List<String>> packageCompilations : compilations
					.entrySet()) {
				final String pakkage = packageCompilations.getKey();
				int counter = 0;
				for (String compilation : packageCompilations.getValue()) {
					int idx = 1;
					selectArtifactsByCompilation.setLong(idx++, scan.getId());
					selectArtifactsByCompilation.setString(idx++, pakkage);
					selectArtifactsByCompilation.setString(idx++, compilation);
					ResultSet result = selectArtifactsByCompilation
							.executeQuery();
					try {
						while (result.next()) {
							ArtifactResult art = new ArtifactResult();
							idx = 1;
							art.id = result.getLong(idx++);
							art.p = Priority.values()[result.getInt(idx++)];
							art.s = Severity.values()[result.getInt(idx++)];
							art.message = result.getString(idx++);
							art.m = factory.newMatch();
							MatchRecord.PK pk = new MatchRecord.PK();
							pk.setProjectId(result.getLong(idx++));
							pk.setHash(result.getLong(idx++));
							pk.setClassName(result.getString(idx++));
							pk.setPackageName(result.getString(idx++));
							pk.setFindingTypeId(result.getLong(idx++));
							art.m.setId(pk);
							Long findingId;
							if (!art.m.select()) {
								// We don't have a match, so we need to produce
								// an entirely new finding.
								MatchRecord m = art.m;
								Importance importance = filter
										.calculateImportance(art.m.getId()
												.getFindingTypeId(), art.p,
												art.s);
								FindingRecord f = factory.newFinding();
								f.setProjectId(projectId);
								f.setImportance(importance);
								f.setSummary(art.message);
								f.insert();
								m.setFindingId(f.getId());
								m.insert();
								findingId = f.getId();
							} else {
								findingId = art.m.getFindingId();
							}
							LongRelationRecord afr = factory
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
			}
			conn.commit();
			generatePartialScanOverview(scan.getId(), scanFindingIds);
			regenerateFindingsOverview(projectName, previousFindingIds, monitor);
			log.info("All new findings persisted for scan " + uid
					+ " in project " + projectName + ".");
		} catch (SQLException e) {
			sqlError(e);
		}
	}

	private void generatePartialScanOverview(long scanId, Set<Long> findingIds)
			throws SQLException {
		for (long id : findingIds) {
			insertTempId.setLong(1, id);
			insertTempId.execute();
		}
		populatePartialScanOverview.setLong(1, scanId);
		populatePartialScanOverview.setLong(2, scanId);
		populatePartialScanOverview.execute();
		deleteTempIds.execute();
	}

	/**
	 * Regenerate findings overview information for the selected findings. This
	 * method cannot add new findings to the overview, merely regenerate
	 * existing ones.
	 * 
	 * @param findingIds
	 */
	private void regenerateFindingsOverview(String projectName,
			Set<Long> findingIds, SLProgressMonitor monitor)
			throws SQLException {
		int count = 0;
		for (long id : findingIds) {
			deleteFindingFromOverview.setLong(1, id);
			deleteFindingFromOverview.executeUpdate();
			checkAndInsertTempId.setLong(1, id);
			checkAndInsertTempId.execute();
			if (count++ % 3 == 0) {
				monitor.worked(1);
			}
		}
		selectLatestScanByProject.setString(1, projectName);
		ResultSet latestScanSet = selectLatestScanByProject.executeQuery();
		try {
			if (latestScanSet.next()) {
				populateFindingOverviewCurrentFindings
						.setString(1, projectName);
				populateFindingOverviewCurrentFindings.setLong(2, latestScanSet
						.getLong(1));
				populateFindingOverviewCurrentFindings.execute();
				selectOldestScanByProject.setString(1, projectName);
				ResultSet oldestScanSet = selectOldestScanByProject
						.executeQuery();
				try {
					if (oldestScanSet.next()) {
						populateFindingOverviewFixedFindings.setString(1,
								projectName);
						populateFindingOverviewFixedFindings.setLong(2,
								oldestScanSet.getLong(1));
						populateFindingOverviewFixedFindings.execute();
					}
				} finally {
					oldestScanSet.close();
				}
				deleteTempIds.execute();
			}
		} finally {
			latestScanSet.close();
		}
	}

	/**
	 * Update local findings to reflect data from the server.
	 * 
	 * @param projectName
	 * @param obsoletions
	 * @param updates
	 * @param monitor
	 * @throws SQLException
	 */
	public void updateLocalFindings(String projectName,
			List<TrailObsoletion> obsoletions, List<AuditTrailUpdate> updates,
			SLProgressMonitor monitor) throws SQLException {
		ProjectRecord project = ProjectRecordFactory.getInstance(conn)
				.newProject();
		project.setName(projectName);
		if (project.select()) {
			Set<Long> findingIds = new HashSet<Long>();
			if (obsoletions != null) {
				for (TrailObsoletion to : obsoletions) {
					FindingRecord obsoletedFinding = factory.newFinding();
					obsoletedFinding.setUid(to.getObsoletedTrail());
					if (obsoletedFinding.select()) {
						FindingRecord newFinding = factory.newFinding();
						newFinding.setUid(to.getTrail());
						if (!newFinding.select()) {
							newFinding.insert();
						}
						obsolete(obsoletedFinding.getId(), newFinding.getId(),
								to.getRevision());
						findingIds.add(obsoletedFinding.getId());
						findingIds.add(newFinding.getId());
					} else {
						log.log(Level.WARNING, "A trail obsoletion for uid "
								+ to.getObsoletedTrail() + " to uid "
								+ to.getTrail() + " could not be resolved");
					}
				}
			}
			if (updates != null) {
				for (AuditTrailUpdate update : updates) {
					// TODO make sure that everything is ordered by revision and
					// time
					FindingRecord finding = factory.newFinding();
					finding.setUid(update.getTrail());
					finding.setProjectId(project.getId());
					if (!finding.select()) {
						finding.setImportance(update.getImportance());
						finding.setSummary(update.getSummary());
						finding.insert();
					}
					MatchRecord mRec = factory.newMatch();
					MatchRecord.PK pk = new MatchRecord.PK();
					pk.setProjectId(project.getId());
					mRec.setId(pk);
					List<Match> matches = update.getMatch();
					if (matches != null) {
						for (Match m : matches) {
							fillKey(pk, m);
							if (mRec.select()) {
								if (!mRec.getFindingId()
										.equals(finding.getId())) {
									// This must be a local match, so delete it.
									// Update the match revision first.
									mRec.setRevision(m.getRevision());
									mRec.update();
									delete(mRec.getFindingId(), finding.getId());
								}
							} else {
								mRec.setFindingId(finding.getId());
								mRec.setRevision(m.getRevision());
								mRec.insert();
							}
						}
					}
					List<Audit> audits = update.getAudit();
					if (audits != null) {
						for (Audit a : audits) {
							Long userId = getUserId(a.getUser());
							switch (a.getEvent()) {
							case COMMENT:
								comment(userId, finding.getId(), a.getValue(),
										a.getTimestamp(), a.getRevision());
								break;
							case IMPORTANCE:
								setImportance(userId, finding.getId(),
										Importance.valueOf(a.getValue()), a
												.getTimestamp(), a
												.getRevision());
								break;
							case READ:
								markAsRead(userId, finding.getId(), a
										.getTimestamp(), a.getRevision());
								break;
							case SUMMARY:
								changeSummary(userId, finding.getId(), a
										.getValue(), a.getTimestamp(), a
										.getRevision());
								break;
							default:
							}
						}
					}
					findingIds.add(finding.getId());
				}
			}
			regenerateFindingsOverview(projectName, findingIds,
					EmptyProgressMonitor.instance());
		} else {
			throw new IllegalArgumentException("No project with name "
					+ projectName + " exists.");
		}
	}

	public List<AuditTrail> getNewLocalAudits(String projectName,
			SLProgressMonitor monitor) throws SQLException {
		List<AuditTrail> trails = new ArrayList<AuditTrail>();
		ProjectRecord rec = ProjectRecordFactory.getInstance(conn).newProject();
		rec.setName(projectName);
		if (rec.select()) {
			findLocalAudits.setLong(1, rec.getId());
			ResultSet set = findLocalAudits.executeQuery();
			try {
				String oldUid = null;
				List<Audit> audits = null;
				while (set.next()) {
					int idx = 1;
					String newUid = set.getString(idx++);
					if (!newUid.equals(oldUid)) {
						oldUid = newUid;
						audits = new LinkedList<Audit>();
						AuditTrail trail = new AuditTrail();
						trail.setFinding(newUid);
						trail.setAudits(audits);
						trails.add(trail);
					}
					audits.add(new Audit(set.getTimestamp(idx++), AuditEvent
							.valueOf(set.getString(idx++)), set
							.getString(idx++)));
				}
			} finally {
				set.close();
			}
		} else {
			throw new IllegalArgumentException("No project with name "
					+ projectName + " exists.");
		}
		return trails;
	}

	public List<Merge> getNewLocalMerges(String projectName,
			SLProgressMonitor monitor) throws SQLException {
		List<Merge> merges = new ArrayList<Merge>();
		ProjectRecord rec = ProjectRecordFactory.getInstance(conn).newProject();
		rec.setName(projectName);
		rec.select();
		findLocalMerges.setLong(1, rec.getId());
		ResultSet set = findLocalMerges.executeQuery();
		Long findingId = null;
		List<Match> matches = null;
		try {
			while (set.next()) {
				int idx = 1;
				Long newFindingId = set.getLong(idx++);
				if (!newFindingId.equals(findingId)) {
					findingId = newFindingId;
					FindingRecord finding = getFinding(findingId);
					Merge merge = new Merge();
					merge.setImportance(finding.getImportance());
					merge.setSummary(finding.getSummary());
					matches = new LinkedList<Match>();
					merge.setMatch(matches);
					merges.add(merge);
				}
				Match m = new Match();
				m.setPackageName(set.getString(idx++));
				m.setClassName(set.getString(idx++));
				m.setHash(set.getLong(idx++));
				m.setFindingType(set.getString(idx++));
				matches.add(m);
			}
		} finally {
			set.close();
		}
		return merges;
	}

	/**
	 * Add trail uids from the server to local findings.
	 * 
	 * @param projectName
	 * @param revision
	 * @param trails
	 * @param merges
	 * @param monitor
	 * @throws SQLException
	 */
	public void updateLocalTrailUids(String projectName,
			List<MergeResponse> trails, List<Merge> merges,
			SLProgressMonitor monitor) throws SQLException {
		if ((trails != null) && (merges != null)) {
			ProjectRecord projectRec = ProjectRecordFactory.getInstance(conn)
					.newProject();
			projectRec.setName(projectName);
			if (projectRec.select()) {
				MatchRecord match = factory.newMatch();
				MatchRecord.PK pk = new MatchRecord.PK();
				match.setId(pk);
				pk.setProjectId(projectRec.getId());
				Iterator<MergeResponse> trailIter = trails.iterator();
				Iterator<Merge> mergeIter = merges.iterator();
				while (mergeIter.hasNext() && trailIter.hasNext()) {
					final MergeResponse response = trailIter.next();
					String trail = response.getTrail();
					long revision = response.getRevision();
					Match m = mergeIter.next().getMatch().get(0);
					fillKey(pk, m);
					if (match.select()) {
						updateFindingUid.setString(1, trail);
						updateFindingUid.setLong(2, match.getFindingId());
						updateFindingUid.execute();
						updateMatchRevision.setLong(1, revision);
						updateMatchRevision.setLong(2, match.getFindingId());
						updateMatchRevision.execute();
					} else {
						log.log(Level.WARNING, "Could not locate finding for "
								+ match + ".  The trail will not be updated.");
					}
				}
			} else {
				throw new IllegalArgumentException("No project with name "
						+ projectName + " exists.");
			}
		}
	}

	public void filterFindingTypeFromScans(long findingId,
			SLProgressMonitor monitor) throws SQLException {
		selectFindingFindingType.setLong(1, findingId);
		final ResultSet set = selectFindingFindingType.executeQuery();
		try {
			final SettingsManager sMan = SettingsManager.getInstance(conn);
			List<FindingTypeFilter> filters = sMan.getGlobalSettings();
			while (set.next()) {
				String type = set.getString(1);
				boolean exists = false;
				for (FindingTypeFilter filter : filters) {
					if (type.equals(filter.getName())) {
						filter.setFiltered(true);
						exists = true;
					}
				}
				if (!exists) {
					FindingTypeFilter newFilter = new FindingTypeFilter();
					newFilter.setName(type);
					newFilter.setFiltered(true);
					filters.add(newFilter);
				}
			}
			sMan.writeGlobalSettings(filters);
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
	private void checkFinding(long findingId) throws SQLException {
		if (getFinding(findingId) == null) {
			throw new IllegalArgumentException(findingId
					+ " is not a valid finding id.");
		}
	}

	public static ClientFindingManager getInstance(Connection conn)
			throws SQLException {
		return new ClientFindingManager(conn);
	}

}
