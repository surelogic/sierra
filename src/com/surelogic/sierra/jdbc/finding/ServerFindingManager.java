package com.surelogic.sierra.jdbc.finding;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.surelogic.common.jdbc.DBType;
import com.surelogic.common.jdbc.JDBCUtils;
import com.surelogic.sierra.jdbc.project.ProjectRecordFactory;
import com.surelogic.sierra.jdbc.record.FindingRecord;
import com.surelogic.sierra.jdbc.record.MatchRecord;
import com.surelogic.sierra.jdbc.record.ProjectRecord;
import com.surelogic.sierra.jdbc.record.ScanRecord;
import com.surelogic.sierra.jdbc.record.ScanSummaryRecord;
import com.surelogic.sierra.jdbc.record.TimeseriesRecord;
import com.surelogic.sierra.jdbc.record.UpdateBaseMapper;
import com.surelogic.sierra.jdbc.record.UpdateRecordMapper;
import com.surelogic.sierra.jdbc.scan.ScanRecordFactory;
import com.surelogic.sierra.jdbc.timeseries.TimeseriesRecordFactory;
import com.surelogic.sierra.jdbc.user.User;
import com.surelogic.sierra.tool.message.Audit;
import com.surelogic.sierra.tool.message.AuditEvent;
import com.surelogic.sierra.tool.message.AuditTrail;
import com.surelogic.sierra.tool.message.Importance;
import com.surelogic.sierra.tool.message.Match;
import com.surelogic.sierra.tool.message.Merge;
import com.surelogic.sierra.tool.message.SyncResponse;
import com.surelogic.sierra.tool.message.SyncTrailResponse;
import com.surelogic.sierra.tool.message.TrailObsoletion;

public final class ServerFindingManager extends FindingManager {

	private final PreparedStatement selectObsoletedTrails;
	private final PreparedStatement selectNewAudits;
	private final PreparedStatement selectNextScan;
	private final PreparedStatement selectPreviousScan;
	private final PreparedStatement linesOfCode;
	private final UpdateRecordMapper scanSummaryMapper;
	private final PreparedStatement findingDifferenceCount;
	private final PreparedStatement findingIntersectCount;
	private final PreparedStatement findingCount;
	private final PreparedStatement artifactCount;
	private final PreparedStatement insertCommitRecord;
	private final PreparedStatement selectMergeInfo;

	private ServerFindingManager(final Connection conn) throws SQLException {
		super(conn);
		if (DBType.ORACLE == JDBCUtils.getDb(conn)) {
			findingDifferenceCount = conn
					.prepareStatement("SELECT COUNT(*) FROM "
							+ "   ((SELECT FINDING_ID FROM SCAN_OVERVIEW WHERE SCAN_ID = ?) MINUS"
							+ "    (SELECT FINDING_ID FROM SCAN_OVERVIEW WHERE SCAN_ID = ?))");
		} else {
			findingDifferenceCount = conn
					.prepareStatement("SELECT COUNT(*) FROM "
							+ "   (SELECT FINDING_ID FROM SCAN_OVERVIEW WHERE SCAN_ID = ? EXCEPT"
							+ "    SELECT FINDING_ID FROM SCAN_OVERVIEW WHERE SCAN_ID = ?) FINDINGS");
		}
		selectObsoletedTrails = conn
				.prepareStatement("SELECT OBS.UUID,OBS.OBSOLETED_BY_REVISION,F.UUID FROM FINDING OBS, FINDING F"
						+ "   WHERE"
						+ "   OBS.PROJECT_ID = ? AND"
						+ "   OBS.OBSOLETED_BY_ID IS NOT NULL AND"
						+ "   OBS.OBSOLETED_BY_REVISION > ? AND"
						+ "   F.ID = OBS.OBSOLETED_BY_ID");

		selectNewAudits = conn
				.prepareStatement("SELECT F.ID,A.UUID,A.EVENT,A.VALUE,A.DATE_TIME,A.REVISION,U.USER_NAME"
						+ "   FROM FINDING F, SIERRA_AUDIT A, SIERRA_USER U"
						+ "   WHERE"
						+ "   F.PROJECT_ID = ? AND"
						+ "   A.FINDING_ID = F.ID AND"
						+ "   A.REVISION IS NOT NULL AND"
						+ "   A.REVISION > ? AND"
						+ "   U.ID = A.USER_ID"
						+ "   ORDER BY F.ID,A.REVISION,A.DATE_TIME");
		selectNextScan = conn
				.prepareStatement("SELECT S.ID,S.SCAN_DATE_TIME FROM QUALIFIER_SCAN_RELTN QSR, SCAN S WHERE"
						+ "   QSR.QUALIFIER_ID = ? AND"
						+ "   S.ID = QSR.SCAN_ID AND"
						+ "   S.PROJECT_ID = ? AND"
						+ "   S.SCAN_DATE_TIME = ("
						+ "      SELECT MIN(S.SCAN_DATE_TIME)"
						+ "      FROM QUALIFIER_SCAN_RELTN QSR, SCAN S WHERE"
						+ "         QSR.QUALIFIER_ID = ? AND"
						+ "         S.ID = QSR.SCAN_ID AND"
						+ "         S.PROJECT_ID = ? AND S.SCAN_DATE_TIME > (SELECT SCAN_DATE_TIME FROM SCAN WHERE ID = ?))");
		selectPreviousScan = conn
				.prepareStatement("SELECT S.ID,S.SCAN_DATE_TIME FROM QUALIFIER_SCAN_RELTN QSR, SCAN S WHERE"
						+ "   QSR.QUALIFIER_ID = ? AND"
						+ "   S.ID = QSR.SCAN_ID AND"
						+ "   S.PROJECT_ID = ? AND"
						+ "   S.SCAN_DATE_TIME = ("
						+ "      SELECT MAX(S.SCAN_DATE_TIME)"
						+ "      FROM QUALIFIER_SCAN_RELTN QSR, SCAN S WHERE"
						+ "         QSR.QUALIFIER_ID = ? AND"
						+ "         S.ID = QSR.SCAN_ID AND"
						+ "         S.PROJECT_ID = ? AND S.SCAN_DATE_TIME < (SELECT SCAN_DATE_TIME FROM SCAN WHERE ID = ?))");
		findingIntersectCount = conn
				.prepareStatement("SELECT COUNT(*) FROM"
						+ "   (SELECT FINDING_ID FROM SCAN_OVERVIEW WHERE SCAN_ID = ? INTERSECT"
						+ "    SELECT FINDING_ID FROM SCAN_OVERVIEW WHERE SCAN_ID = ?) FINDINGS");
		findingCount = conn
				.prepareStatement("SELECT COUNT(*) FROM SCAN_OVERVIEW WHERE SCAN_ID = ?");
		artifactCount = conn
				.prepareStatement("SELECT COUNT(*) FROM ARTIFACT WHERE SCAN_ID = ?");
		linesOfCode = conn
				.prepareStatement("SELECT SUM(LINES_OF_CODE) FROM METRIC_CU WHERE SCAN_ID = ?");
		scanSummaryMapper = new UpdateBaseMapper(
				conn,
				"INSERT INTO SCAN_SUMMARY (SCAN_ID,QUALIFIER_ID,NEW_FINDINGS,FIXED_FINDINGS,UNCHANGED_FINDINGS,ARTIFACT_COUNT,TOTAL_FINDINGS,LINES_OF_CODE) VALUES (?,?,?,?,?,?,?,?)",
				"SELECT NEW_FINDINGS,FIXED_FINDINGS,UNCHANGED_FINDINGS,ARTIFACT_COUNT,TOTAL_FINDINGS,LINES_OF_CODE FROM SCAN_SUMMARY WHERE SCAN_ID = ? AND QUALIFIER_ID = ?",
				"DELETE FROM SCAN_SUMMARY WHERE SCAN_ID = ? AND QUALIFIER_ID = ?",
				"UPDATE SCAN_SUMMARY SET NEW_FINDINGS = ?, FIXED_FINDINGS = ?, UNCHANGED_FINDINGS = ?, ARTIFACT_COUNT = ?, TOTAL_FINDINGS = ?, LINES_OF_CODE = ? WHERE SCAN_ID = ? AND QUALIFIER_ID = ?",
				false);
		insertCommitRecord = conn
				.prepareStatement("INSERT INTO COMMIT_AUDITS (PROJECT_ID,USER_ID,REVISION) VALUES (?,?,?)");
		selectMergeInfo = conn
				.prepareStatement("SELECT F.UUID,F.SUMMARY,F.IMPORTANCE,LM.PACKAGE_NAME,LM.CLASS_NAME,LM.HASH,FT.UUID,LM.REVISION"
						+ "   FROM LOCATION_MATCH LM, FINDING F, FINDING_TYPE FT"
						+ "   WHERE F.ID = ? AND LM.FINDING_ID = F.ID AND FT.ID = LM.FINDING_TYPE_ID");
	}

	public void generateOverview(final String projectName,
			final String scanUid, final Set<String> timeseries)
			throws SQLException {
		final ProjectRecord projectRec = ProjectRecordFactory.getInstance(conn)
				.newProject();
		projectRec.setName(projectName);
		if (projectRec.select()) {
			final ScanRecord scan = ScanRecordFactory.getInstance(conn)
					.newScan();
			scan.setUid(scanUid);
			if (scan.select()) {
				final List<TimeseriesRecord> timeseriesRecs = new ArrayList<TimeseriesRecord>(
						timeseries.size());
				final TimeseriesRecordFactory qFac = TimeseriesRecordFactory
						.getInstance(conn);
				for (final String q : timeseries) {
					final TimeseriesRecord qRec = qFac.newTimeseries();
					qRec.setName(q);
					if (qRec.select()) {
						timeseriesRecs.add(qRec);
					} else {
						throw new IllegalArgumentException(
								"No timeseries exists with name " + q);
					}
					populateScanSummary(scan, qRec, projectRec);
				}
				log.info("Populating scan overview for scan with uid "
						+ scan.getUid() + ".");
				populateScanOverview(scan.getId());
			} else {
				throw new IllegalArgumentException("No scan exists with uid "
						+ scanUid);
			}
		} else {
			throw new IllegalArgumentException("No project exists with name"
					+ projectName);
		}
	}

	/**
	 * Commit the given audit trails.
	 * 
	 * @param projectId
	 * @param userId
	 * @param revision
	 * @param trails
	 * @return the in-order list of uids that the audits were applied to.
	 * @throws SQLException
	 */
	public List<String> commitAuditTrails(final long projectId,
			final Long userId, final Long revision,
			final List<AuditTrail> trails) throws SQLException {
		final List<String> uids = new ArrayList<String>(trails.size());
		for (final AuditTrail trail : trails) {
			final FindingRecord findingRecord = factory.newFinding();
			final String finding = trail.getFinding();
			findingRecord.setUid(finding);
			if (findingRecord.select()) {
				Long findingId = findingRecord.getId();
				Long obsoletedById = findingRecord.getObsoletedById();
				while ((obsoletedById = findingRecord.getObsoletedById()) != null) {
					findingRecord.setId(obsoletedById);
					findingRecord.select();
					findingId = findingRecord.getId();
				}
				for (final Audit audit : trail.getAudits()) {
					final String value = audit.getValue();
					final Date time = audit.getTimestamp();
					switch (audit.getEvent()) {
					case COMMENT:
						comment(audit.getUuid(), userId, findingId, value,
								time, revision);
						break;
					case IMPORTANCE:
						setImportance(audit.getUuid(), userId, findingId,
								Importance.fromValue(value), time, revision);
						break;
					case READ:
						markAsRead(audit.getUuid(), userId, findingId, time,
								revision);
						break;
					case SUMMARY:
						changeSummary(audit.getUuid(), userId, findingId,
								value, time, revision);
					}
				}
				uids.add(findingRecord.getUid());
			}
		}
		insertCommitRecord.setLong(1, projectId);
		insertCommitRecord.setLong(2, userId);
		insertCommitRecord.setLong(3, revision);
		insertCommitRecord.execute();
		return uids;
	}

	/**
	 * Find or generate a finding for each merge, and return the trails.
	 * 
	 * 
	 * @param projectId
	 * @param revision
	 * @param merges
	 * @return an in-order list of finding uids
	 * @throws SQLException
	 */
	public List<String> mergeAuditTrails(final long projectId,
			final Long revision, final List<Merge> merges) throws SQLException {
		final List<String> trails = new ArrayList<String>(merges.size());
		for (final Merge merge : merges) {
			final Match match = merge.getMatch();
			final MatchRecord.PK matchId = new MatchRecord.PK();
			final Long findingTypeId = ftManager.getFindingTypeId(match
					.getFindingType());
			if (findingTypeId != null) {
				matchId.setClassName(match.getClassName());
				matchId.setFindingTypeId(findingTypeId);
				matchId.setHash(match.getHash());
				matchId.setPackageName(match.getPackageName());
				matchId.setProjectId(projectId);
			} else {
				throw new IllegalArgumentException("No finding type with id "
						+ match.getFindingType() + " is present.");
			}
			final MatchRecord matchRecord = factory.newMatch();
			matchRecord.setId(matchId);
			String uuid;
			if (!matchRecord.select()) {
				// We do not have a finding. We will create one.
				final FindingRecord findingRecord = factory.newFinding();
				uuid = UUID.randomUUID().toString();
				findingRecord.setUid(uuid);
				findingRecord.setProjectId(projectId);
				findingRecord.setSummary(merge.getSummary());
				findingRecord.setImportance(merge.getImportance());
				findingRecord.insert();
				matchRecord.setFindingId(findingRecord.getId());
				matchRecord.setRevision(revision);
				matchRecord.insert();
			} else {
				final FindingRecord findingRecord = getFinding(matchRecord
						.getFindingId());
				uuid = findingRecord.getUid();
				if (uuid == null) {
					// This is a local finding. We will update it to match the
					// client's importance and summary
					uuid = UUID.randomUUID().toString();
					findingRecord.setUid(uuid);
					findingRecord.setSummary(merge.getSummary());
					findingRecord.setImportance(merge.getImportance());
					findingRecord.update();
				}
			}
			trails.add(uuid);
		}
		return trails;
	}

	/**
	 * Return a list of trails obsoleted since the provided revision.
	 * 
	 * @param project
	 * @param revision
	 * @return
	 * @throws SQLException
	 */
	public List<TrailObsoletion> getObsoletedTrails(final String project,
			final Long revision) throws SQLException {
		final List<TrailObsoletion> trails = new ArrayList<TrailObsoletion>();

		final ProjectRecord projectRecord = ProjectRecordFactory.getInstance(
				conn).newProject();
		projectRecord.setName(project);
		if (projectRecord.select()) {
			int idx = 1;
			selectObsoletedTrails.setLong(idx++, projectRecord.getId());
			selectObsoletedTrails.setLong(idx++, revision);
			final ResultSet set = selectObsoletedTrails.executeQuery();
			try {
				while (set.next()) {
					idx = 1;
					final TrailObsoletion o = new TrailObsoletion();
					o.setObsoletedTrail(set.getString(idx++));
					o.setRevision(set.getLong(idx++));
					o.setTrail(set.getString(idx++));
					trails.add(o);
				}
			} finally {
				set.close();
			}
		}
		return trails;
	}

	public SyncResponse getAuditUpdates(final String project,
			final long revision) throws SQLException {
		final SyncResponse response = new SyncResponse();
		final List<SyncTrailResponse> trails = response.getTrails();
		final ProjectRecord projectRecord = ProjectRecordFactory.getInstance(
				conn).newProject();
		projectRecord.setName(project);
		if (projectRecord.select()) {
			int idx = 1;
			selectNewAudits.setLong(idx++, projectRecord.getId());
			selectNewAudits.setLong(idx++, revision);
			final ResultSet set = selectNewAudits.executeQuery();
			long findingId = -1;
			List<Audit> audits = null;
			try {
				while (set.next()) {
					idx = 1;
					final long nextId = set.getLong(idx++);
					if (!(nextId == findingId)) {
						findingId = nextId;
						final SyncTrailResponse trail = new SyncTrailResponse();
						selectMergeInfo.setLong(1, findingId);
						final ResultSet mergeSet = selectMergeInfo
								.executeQuery();
						final Merge merge = new Merge();
						final Match match = new Match();
						merge.setMatch(match);
						try {
							mergeSet.next();
							int mergeIdx = 1;
							trail.setFinding(mergeSet.getString(mergeIdx++));
							merge.setSummary(mergeSet.getString(mergeIdx++));
							merge.setImportance(Importance.values()[mergeSet
									.getInt(mergeIdx++)]);
							match
									.setPackageName(mergeSet
											.getString(mergeIdx++));
							match.setClassName(mergeSet.getString(mergeIdx++));
							match.setHash(mergeSet.getLong(mergeIdx++));
							match
									.setFindingType(mergeSet
											.getString(mergeIdx++));
							match.setRevision(mergeSet.getLong(mergeIdx++));
						} finally {
							mergeSet.close();
						}
						trail.setMerge(merge);
						trails.add(trail);
						audits = trail.getAudits();
					}
					final Audit audit = new Audit();
					audit.setUuid(set.getString(idx++));
					audit.setEvent(AuditEvent.valueOf(set.getString(idx++)));
					audit.setValue(set.getString(idx++));
					audit.setTimestamp(set.getTimestamp(idx++));
					audit.setRevision(set.getLong(idx++));
					audit.setUser(set.getString(idx++));
					audits.add(audit);
				}
			} finally {
				set.close();
			}
		}
		return response;
	}

	/**
	 * Update the scan summary row for this scan/timeseries pair, and also the
	 * adjacent scan summaries in this timeseries, since they may have changed
	 * with the addition of this scan. This row contains some useful metrics for
	 * reporting, and one of these should be created for each timeseries a scan
	 * is published to.
	 * 
	 * @param scan
	 * @param timeseries
	 * @param project
	 * @throws SQLException
	 */
	private void populateScanSummary(final ScanRecord scan,
			final TimeseriesRecord timeseries, final ProjectRecord project)
			throws SQLException {
		int idx = 1;
		// Add scan summary information
		selectNextScan.setLong(idx++, timeseries.getId());
		selectNextScan.setLong(idx++, project.getId());
		selectNextScan.setLong(idx++, timeseries.getId());
		selectNextScan.setLong(idx++, project.getId());
		selectNextScan.setLong(idx++, scan.getId());
		ResultSet set = selectNextScan.executeQuery();
		try {
			if (set.next()) {
				refreshScanSummary(set.getLong(1), timeseries.getId(), project
						.getId());
			}
		} finally {
			set.close();
		}
		refreshScanSummary(scan.getId(), timeseries.getId(), project.getId());
		idx = 1;
		selectPreviousScan.setLong(idx++, timeseries.getId());
		selectPreviousScan.setLong(idx++, project.getId());
		selectPreviousScan.setLong(idx++, timeseries.getId());
		selectPreviousScan.setLong(idx++, project.getId());
		selectPreviousScan.setLong(idx++, scan.getId());
		set = selectPreviousScan.executeQuery();
		try {
			if (set.next()) {
				refreshScanSummary(set.getLong(1), timeseries.getId(), project
						.getId());
			}
		} finally {
			set.close();
		}
	}

	/**
	 * Insert or update a scan summary for the given scan with respect to the
	 * given timeseries.
	 * 
	 * @param scanId
	 * @param timeseriesId
	 * @param projectId
	 * @throws SQLException
	 */
	public void refreshScanSummary(final Long scanId, final Long timeseriesId,
			final Long projectId) throws SQLException {
		final ScanSummaryRecord summary = new ScanSummaryRecord(
				scanSummaryMapper);
		summary.setId(new ScanSummaryRecord.PK(scanId, timeseriesId));
		final boolean summaryExists = summary.select();
		int idx = 1;
		selectPreviousScan.setLong(idx++, timeseriesId);
		selectPreviousScan.setLong(idx++, projectId);
		selectPreviousScan.setLong(idx++, timeseriesId);
		selectPreviousScan.setLong(idx++, projectId);
		selectPreviousScan.setLong(idx++, scanId);
		final ResultSet set = selectPreviousScan.executeQuery();
		try {
			if (set.next()) {
				final long previousScanId = set.getLong(1);
				findingDifferenceCount.setLong(1, scanId);
				findingDifferenceCount.setLong(2, previousScanId);
				ResultSet count = findingDifferenceCount.executeQuery();
				try {
					count.next();
					summary.setNewFindings(count.getLong(1));
				} finally {
					count.close();
				}
				findingIntersectCount.setLong(1, scanId);
				findingIntersectCount.setLong(2, previousScanId);
				count = findingIntersectCount.executeQuery();
				try {
					count.next();
					summary.setUnchangedFindings(count.getLong(1));
				} finally {
					count.close();
				}
				findingDifferenceCount.setLong(1, previousScanId);
				findingDifferenceCount.setLong(2, scanId);
				count = findingDifferenceCount.executeQuery();
				try {
					count.next();
					summary.setFixedFindings(count.getLong(1));
				} finally {
					count.close();
				}
				findingCount.setLong(1, scanId);
				count = findingCount.executeQuery();
				try {
					count.next();
					summary.setTotalFindings(count.getLong(1));
				} finally {
					count.close();
				}
			} else {
				findingCount.setLong(1, scanId);
				final ResultSet count = findingCount.executeQuery();
				try {
					count.next();
					final Long findings = count.getLong(1);
					summary.setUnchangedFindings(0L);
					summary.setNewFindings(findings);
					summary.setFixedFindings(0L);
					summary.setTotalFindings(findings);
				} finally {
					count.close();
				}
			}
			if (!summaryExists) {
				artifactCount.setLong(1, scanId);
				ResultSet count = artifactCount.executeQuery();
				try {
					count.next();
					summary.setArtifacts(count.getLong(1));
				} finally {
					count.close();
				}
				linesOfCode.setLong(1, scanId);
				count = linesOfCode.executeQuery();
				try {
					count.next();
					summary.setLinesOfCode(count.getLong(1));
				} finally {
					count.close();
				}
			}
		} finally {
			set.close();
		}

		if (summaryExists) {
			summary.update();
		} else {
			summary.insert();
		}
	}

	/**
	 * Comment on a finding.
	 * 
	 * @param findingId
	 * @param user
	 * @param comment
	 */
	public void comment(final long findingId, final User user,
			final long revision, final String comment) throws SQLException {
		comment(user.getId(), findingId, comment, new Date(), revision);
	}

	/**
	 * Mark the finding as read.
	 * 
	 * @param findingId
	 * @param user
	 */
	public void markAsRead(final long findingId, final User user,
			final long revision) throws SQLException {
		markAsRead(user.getId(), findingId, new Date(), revision);
	}

	/**
	 * Change the importance of a finding
	 * 
	 * @param findingId
	 * @param user
	 * @param importance
	 */
	public void setImportance(final long findingId, final User user,
			final long revision, final Importance importance)
			throws SQLException {
		setImportance(user.getId(), findingId, importance, new Date(), revision);
	}

	public static ServerFindingManager getInstance(final Connection conn)
			throws SQLException {
		return new ServerFindingManager(conn);
	}

}
