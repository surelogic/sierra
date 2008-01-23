package com.surelogic.sierra.jdbc.finding;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import com.surelogic.sierra.jdbc.DBType;
import com.surelogic.sierra.jdbc.JDBCUtils;
import com.surelogic.sierra.jdbc.project.ProjectRecordFactory;
import com.surelogic.sierra.jdbc.qualifier.QualifierRecordFactory;
import com.surelogic.sierra.jdbc.record.AuditRecord;
import com.surelogic.sierra.jdbc.record.FindingRecord;
import com.surelogic.sierra.jdbc.record.MatchRecord;
import com.surelogic.sierra.jdbc.record.ProjectRecord;
import com.surelogic.sierra.jdbc.record.QualifierRecord;
import com.surelogic.sierra.jdbc.record.ScanRecord;
import com.surelogic.sierra.jdbc.record.ScanSummaryRecord;
import com.surelogic.sierra.jdbc.record.UpdateBaseMapper;
import com.surelogic.sierra.jdbc.record.UpdateRecordMapper;
import com.surelogic.sierra.jdbc.scan.ScanRecordFactory;
import com.surelogic.sierra.tool.message.Audit;
import com.surelogic.sierra.tool.message.AuditEvent;
import com.surelogic.sierra.tool.message.AuditTrail;
import com.surelogic.sierra.tool.message.AuditTrailUpdate;
import com.surelogic.sierra.tool.message.Match;
import com.surelogic.sierra.tool.message.Merge;
import com.surelogic.sierra.tool.message.MergeResponse;
import com.surelogic.sierra.tool.message.TrailObsoletion;

public final class ServerFindingManager extends FindingManager {

	private final PreparedStatement selectObsoletedTrails;
	private final PreparedStatement selectUpdatedMatches;
	private final PreparedStatement selectUpdatedAudits;
	private final PreparedStatement selectNextScan;
	private final PreparedStatement selectPreviousScan;
	private final PreparedStatement linesOfCode;
	private final UpdateRecordMapper scanSummaryMapper;
	private final PreparedStatement findingDifferenceCount;
	private final PreparedStatement findingIntersectCount;
	private final PreparedStatement findingCount;
	private final PreparedStatement artifactCount;

	private ServerFindingManager(Connection conn) throws SQLException {
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
		selectUpdatedMatches = conn
				.prepareStatement("SELECT F.UUID,LM.PACKAGE_NAME,LM.CLASS_NAME,LM.HASH,FT.UUID,LM.REVISION"
						+ "   FROM LOCATION_MATCH LM, FINDING F, FINDING_TYPE FT"
						+ "   WHERE"
						+ "   LM.PROJECT_ID = ? AND"
						+ "   LM.REVISION IS NOT NULL AND LM.REVISION > ?"
						+ "   AND F.ID = LM.FINDING_ID"
						+ "   AND FT.ID = LM.FINDING_TYPE_ID ORDER BY F.ID");
		selectUpdatedAudits = conn
				.prepareStatement("SELECT F.UUID,A.EVENT,A.VALUE,A.DATE_TIME,A.REVISION,U.USER_NAME"
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
	}

	public void generateOverview(String projectName, String scanUid,
			Set<String> qualifiers) throws SQLException {
		ProjectRecord projectRec = ProjectRecordFactory.getInstance(conn)
				.newProject();
		projectRec.setName(projectName);
		if (projectRec.select()) {
			ScanRecord scan = ScanRecordFactory.getInstance(conn).newScan();
			scan.setUid(scanUid);
			if (scan.select()) {
				List<QualifierRecord> qualifierRecs = new ArrayList<QualifierRecord>(
						qualifiers.size());
				QualifierRecordFactory qFac = QualifierRecordFactory
						.getInstance(conn);
				for (String q : qualifiers) {
					QualifierRecord qRec = qFac.newQualifier();
					qRec.setName(q);
					if (qRec.select()) {
						qualifierRecs.add(qRec);
					} else {
						throw new IllegalArgumentException(
								"No qualifier exists with name " + q);
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
	 * @param userId
	 * @param revision
	 * @param trails
	 * @return the in-order list of uids that the audits were applied to.
	 * @throws SQLException
	 */
	public List<String> commitAuditTrails(Long userId, Long revision,
			List<AuditTrail> trails) throws SQLException {
		List<String> uids = new ArrayList<String>(trails.size());
		for (AuditTrail trail : trails) {
			FindingRecord findingRecord = factory.newFinding();
			String finding = trail.getFinding();
			findingRecord.setUid(finding);
			if (findingRecord.select()) {
				Long findingId = findingRecord.getId();
				Long obsoletedById = findingRecord.getObsoletedById();
				while ((obsoletedById = findingRecord.getObsoletedById()) != null) {
					findingRecord.setId(obsoletedById);
					findingRecord.select();
					findingId = findingRecord.getId();
				}
				for (Audit audit : trail.getAudits()) {
					AuditRecord auditRecord = factory.newAudit();
					auditRecord.setEvent(audit.getEvent());
					auditRecord.setFindingId(findingId);
					auditRecord.setRevision(revision);
					auditRecord.setTimestamp(audit.getTimestamp());
					auditRecord.setUserId(userId);
					auditRecord.setValue(audit.getValue());
					auditRecord.insert();
				}
				uids.add(findingRecord.getUid());
			}
		}
		return uids;
	}

	/**
	 * Find or generate a finding for each merge, and return the trails.
	 * 
	 * WARNING: This method works for arbitrary-sized merges, EXCEPT that the
	 * trail revision makes some assumptions. We need to fix up trail revision
	 * logic if we want to do merges w/ more than one match.
	 * 
	 * @param project
	 * @param revision
	 * @param merges
	 * @return
	 * @throws SQLException
	 */
	public List<MergeResponse> mergeAuditTrails(String project,
			final Long revision, List<Merge> merges) throws SQLException {
		ProjectRecord projectRecord = ProjectRecordFactory.getInstance(conn)
				.newProject();
		projectRecord.setName(project);
		if (!projectRecord.select()) {
			projectRecord.insert();
		}
		List<MergeResponse> trails = new ArrayList<MergeResponse>();
		for (Merge merge : merges) {
			List<Match> matches = merge.getMatch();
			if (matches != null && !matches.isEmpty()) {
				List<MatchRecord.PK> matchIds = new ArrayList<MatchRecord.PK>(
						matches.size());
				// Generate the list of match ids
				for (Match m : matches) {
					MatchRecord.PK matchId = new MatchRecord.PK();
					Long findingTypeId = ftManager.getFindingTypeId(m
							.getFindingType());
					if (findingTypeId != null) {
						matchId.setClassName(m.getClassName());
						matchId.setFindingTypeId(findingTypeId);
						matchId.setHash(m.getHash());
						matchId.setPackageName(m.getPackageName());
						matchId.setProjectId(projectRecord.getId());
						matchIds.add(matchId);
					} else {
						throw new IllegalArgumentException(
								"No finding type with id " + m.getFindingType()
										+ " is present.");
					}
				}
				MatchRecord matchRecord = factory.newMatch();
				List<MatchRecord.PK> unmatched = new ArrayList<MatchRecord.PK>(
						matchIds.size());
				Set<Long> findings = new TreeSet<Long>();
				long trailRevision = revision;
				// Try to look up any existing matches
				for (MatchRecord.PK matchId : matchIds) {
					matchRecord.setId(matchId);
					if (matchRecord.select()
							&& (getFinding(matchRecord.getFindingId()).getUid() != null)) {
						trailRevision = matchRecord.getRevision();
						findings.add(matchRecord.getFindingId());
					} else {
						unmatched.add(matchId);
					}
				}
				Long findingId;
				String uuid;
				if (findings.size() == 1) {
					// The finding we want to use already exists.
					findingId = findings.iterator().next();
					uuid = getFinding(findingId).getUid();
				} else {
					// We will be creating a new finding, and applying it to all
					// matches.
					FindingRecord findingRecord = factory.newFinding();
					uuid = UUID.randomUUID().toString();
					findingRecord.setUid(uuid);
					findingRecord.setProjectId(projectRecord.getId());
					findingRecord.setSummary(merge.getSummary());
					findingRecord.setImportance(merge.getImportance());
					findingRecord.insert();
					findingId = findingRecord.getId();
					for (Long obsoleteId : findings) {
						obsolete(obsoleteId, findingId, revision);
					}
				}
				// Now assign finding to unmatched matches.
				for (MatchRecord.PK matchId : unmatched) {
					matchRecord.setId(matchId);
					if (matchRecord.select()) {
						// This is a finding without a uuid
						Long oldFinding = matchRecord.getFindingId();
						matchRecord.setFindingId(findingId);
						matchRecord.setRevision(revision);
						matchRecord.update();
						delete(oldFinding, findingId);
					} else {
						matchRecord.setFindingId(findingId);
						matchRecord.setRevision(revision);
						matchRecord.insert();
					}
				}
				final MergeResponse mergeResponse = new MergeResponse();
				mergeResponse.setRevision(trailRevision);
				mergeResponse.setTrail(uuid);
				trails.add(mergeResponse);
			}
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
	public List<TrailObsoletion> getObsoletedTrails(String project,
			Long revision) throws SQLException {
		List<TrailObsoletion> trails = new ArrayList<TrailObsoletion>();

		ProjectRecord projectRecord = ProjectRecordFactory.getInstance(conn)
				.newProject();
		projectRecord.setName(project);
		if (projectRecord.select()) {
			int idx = 1;
			selectObsoletedTrails.setLong(idx++, projectRecord.getId());
			selectObsoletedTrails.setLong(idx++, revision);
			ResultSet set = selectObsoletedTrails.executeQuery();
			try {
				while (set.next()) {
					idx = 1;
					TrailObsoletion o = new TrailObsoletion();
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

	public List<AuditTrailUpdate> getAuditUpdates(String project, Long revision)
			throws SQLException {
		List<AuditTrailUpdate> updates = new ArrayList<AuditTrailUpdate>();

		ProjectRecord projectRecord = ProjectRecordFactory.getInstance(conn)
				.newProject();
		projectRecord.setName(project);
		if (projectRecord.select()) {
			Map<String, List<Match>> matchMap = new HashMap<String, List<Match>>();
			int idx = 1;
			selectUpdatedMatches.setLong(idx++, projectRecord.getId());
			selectUpdatedMatches.setLong(idx++, revision);
			ResultSet set = selectUpdatedMatches.executeQuery();
			try {
				String uuid = null;
				List<Match> matches = null;
				while (set.next()) {
					idx = 1;
					String nextUuid = set.getString(idx++);
					if (!nextUuid.equals(uuid)) {
						uuid = nextUuid;
						matches = new LinkedList<Match>();
						matchMap.put(uuid, matches);
					}
					Match m = new Match();
					m.setPackageName(set.getString(idx++));
					m.setClassName(set.getString(idx++));
					m.setHash(set.getLong(idx++));
					m.setFindingType(set.getString(idx++));
					m.setRevision(set.getLong(idx++));
					matches.add(m);
				}
			} finally {
				set.close();
			}
			idx = 1;
			selectUpdatedAudits.setLong(idx++, projectRecord.getId());
			selectUpdatedAudits.setLong(idx++, revision);
			set = selectUpdatedAudits.executeQuery();
			try {
				String uuid = null;
				List<Audit> audits = null;
				while (set.next()) {
					idx = 1;
					String nextUuid = set.getString(idx++);
					if (!nextUuid.equals(uuid)) {
						uuid = nextUuid;
						audits = new LinkedList<Audit>();
						AuditTrailUpdate update = new AuditTrailUpdate();
						FindingRecord finding = factory.newFinding();
						finding.setUid(uuid);
						finding.select();
						update.setImportance(finding.getImportance());
						update.setSummary(finding.getSummary());
						update.setTrail(uuid);
						update.setMatch(matchMap.get(uuid));
						update.setAudit(audits);
						updates.add(update);
					}
					Audit a = new Audit();
					a.setEvent(AuditEvent.valueOf(set.getString(idx++)));
					a.setValue(set.getString(idx++));
					a.setTimestamp(set.getTimestamp(idx++));
					a.setRevision(set.getLong(idx++));
					a.setUser(set.getString(idx++));
					audits.add(a);
				}
			} finally {
				set.close();
			}
		}
		return updates;
	}

	/**
	 * Update the scan summary row for this scan/qualifier pair, and also the
	 * adjacent scan summaries in this qualifier, since they may have changed
	 * with the addition of this scan. This row contains some useful metrics for
	 * reporting, and one of these should be created for each qualifier a scan
	 * is published to.
	 * 
	 * @param scan
	 * @param qualifier
	 * @param project
	 * @throws SQLException
	 */
	private void populateScanSummary(ScanRecord scan,
			QualifierRecord qualifier, ProjectRecord project)
			throws SQLException {
		int idx = 1;
		// Add scan summary information
		selectNextScan.setLong(idx++, qualifier.getId());
		selectNextScan.setLong(idx++, project.getId());
		selectNextScan.setLong(idx++, qualifier.getId());
		selectNextScan.setLong(idx++, project.getId());
		selectNextScan.setLong(idx++, scan.getId());
		ResultSet set = selectNextScan.executeQuery();
		try {
			if (set.next()) {
				refreshScanSummary(set.getLong(1), qualifier.getId(), project
						.getId());
			}
		} finally {
			set.close();
		}
		refreshScanSummary(scan.getId(), qualifier.getId(), project.getId());
		idx = 1;
		selectPreviousScan.setLong(idx++, qualifier.getId());
		selectPreviousScan.setLong(idx++, project.getId());
		selectPreviousScan.setLong(idx++, qualifier.getId());
		selectPreviousScan.setLong(idx++, project.getId());
		selectPreviousScan.setLong(idx++, scan.getId());
		set = selectPreviousScan.executeQuery();
		try {
			if (set.next()) {
				refreshScanSummary(set.getLong(1), qualifier.getId(), project
						.getId());
			}
		} finally {
			set.close();
		}
	}

	/**
	 * Insert or update a scan summary for the given scan with respect to the
	 * given qualifier.
	 * 
	 * @param scanId
	 * @param qualifierId
	 * @param projectId
	 * @throws SQLException
	 */
	public void refreshScanSummary(Long scanId, Long qualifierId, Long projectId)
			throws SQLException {
		ScanSummaryRecord summary = new ScanSummaryRecord(scanSummaryMapper);
		summary.setId(new ScanSummaryRecord.PK(scanId, qualifierId));
		boolean summaryExists = summary.select();
		int idx = 1;
		selectPreviousScan.setLong(idx++, qualifierId);
		selectPreviousScan.setLong(idx++, projectId);
		selectPreviousScan.setLong(idx++, qualifierId);
		selectPreviousScan.setLong(idx++, projectId);
		selectPreviousScan.setLong(idx++, scanId);
		ResultSet set = selectPreviousScan.executeQuery();
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
				ResultSet count = findingCount.executeQuery();
				try {
					count.next();
					Long findings = count.getLong(1);
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

	public static ServerFindingManager getInstance(Connection conn)
			throws SQLException {
		return new ServerFindingManager(conn);
	}

}
