package com.surelogic.sierra.jdbc.finding;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.surelogic.common.SLProgressMonitor;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.jdbc.record.AuditRecord;
import com.surelogic.sierra.jdbc.record.FindingRecord;
import com.surelogic.sierra.jdbc.record.LongRelationRecord;
import com.surelogic.sierra.jdbc.record.MatchRecord;
import com.surelogic.sierra.jdbc.record.RelationRecord;
import com.surelogic.sierra.jdbc.record.ScanRecord;
import com.surelogic.sierra.jdbc.scan.ScanManager;
import com.surelogic.sierra.jdbc.scan.ScanRecordFactory;
import com.surelogic.sierra.jdbc.tool.FindingFilter;
import com.surelogic.sierra.jdbc.tool.FindingTypeManager;
import com.surelogic.sierra.jdbc.user.ClientUser;
import com.surelogic.sierra.tool.message.AuditEvent;
import com.surelogic.sierra.tool.message.Importance;
import com.surelogic.sierra.tool.message.Match;
import com.surelogic.sierra.tool.message.Priority;
import com.surelogic.sierra.tool.message.Severity;

public class FindingManager {

	protected static final Logger log = SLLogger
			.getLoggerFor(FindingManager.class);

	protected static final int FETCH_SIZE = 1000;
	protected static final int CHECK_SIZE = 10;

	protected final Connection conn;
	protected final FindingTypeManager ftManager;
	protected final FindingRecordFactory factory;

	private final PreparedStatement selectFindingById;
	private final PreparedStatement touchFinding;
	private final PreparedStatement updateFindingImportance;
	private final PreparedStatement updateFindingSummary;
	private final PreparedStatement obsoleteArtifacts;
	private final PreparedStatement obsoleteAudits;
	private final PreparedStatement obsoleteMatches;
	private final PreparedStatement obsoleteFinding;
	private final PreparedStatement obsoleteOverview;
	private final PreparedStatement latestAuditRevision;
	private final PreparedStatement deleteMatches;
	private final PreparedStatement deleteFindings;

	private final PreparedStatement deleteScanOverview;
	private final PreparedStatement populateScanOverview;
	private final PreparedStatement populateScanRelationOverview;

	private final PreparedStatement scanArtifacts;

	protected FindingManager(Connection conn) throws SQLException {
		this.conn = conn;
		factory = FindingRecordFactory.getInstance(conn);
		ftManager = FindingTypeManager.getInstance(conn);
		touchFinding = conn
				.prepareStatement("UPDATE FINDING SET IS_READ = 'Y', LAST_CHANGED = CASE WHEN (? > LAST_CHANGED) THEN ? ELSE LAST_CHANGED END WHERE ID = ?");
		updateFindingImportance = conn
				.prepareStatement("UPDATE FINDING SET IMPORTANCE = ?, LAST_CHANGED = ? WHERE ID = ?");
		updateFindingSummary = conn
				.prepareStatement("UPDATE FINDING SET SUMMARY = ?, LAST_CHANGED = ? WHERE ID = ?");
		obsoleteArtifacts = conn
				.prepareStatement("UPDATE ARTIFACT_FINDING_RELTN SET FINDING_ID = ? WHERE FINDING_ID = ?");
		obsoleteAudits = conn
				.prepareStatement("UPDATE SIERRA_AUDIT SET FINDING_ID = ? WHERE FINDING_ID = ?");
		obsoleteMatches = conn
				.prepareStatement("UPDATE LOCATION_MATCH SET FINDING_ID = ? WHERE FINDING_ID = ?");
		obsoleteOverview = conn
				.prepareStatement("UPDATE SCAN_OVERVIEW SET FINDING_ID = ? WHERE FINDING_ID = ?");
		obsoleteFinding = conn
				.prepareStatement("UPDATE FINDING SET OBSOLETED_BY_ID = ?, OBSOLETED_BY_REVISION = ? WHERE ID = ?");
		latestAuditRevision = conn
				.prepareStatement("SELECT MAX(A.REVISION) FROM PROJECT P, FINDING F, SIERRA_AUDIT A WHERE P.NAME = ? AND F.PROJECT_ID = P.ID AND A.FINDING_ID = F.ID");
		deleteMatches = conn
				.prepareStatement("DELETE FROM LOCATION_MATCH WHERE PROJECT_ID = (SELECT P.ID FROM PROJECT P WHERE P.NAME = ?)");
		deleteFindings = conn
				.prepareStatement("DELETE FROM FINDING WHERE PROJECT_ID = (SELECT P.ID FROM PROJECT P WHERE P.NAME = ?)");
		deleteScanOverview = conn
				.prepareStatement("DELETE FROM SCAN_OVERVIEW WHERE SCAN_ID = ?");
		populateScanOverview = conn
				.prepareStatement("INSERT INTO SCAN_OVERVIEW (FINDING_ID,SCAN_ID,LINE_OF_CODE,ARTIFACT_COUNT,TOOL,CU)"
						+ " SELECT AFR.FINDING_ID, ?, MAX(SL.LINE_OF_CODE), COUNT(AFR.ARTIFACT_ID), "
						+ "        CASE WHEN COUNT(DISTINCT T.ID) = 1 THEN MAX(T.NAME) ELSE '(From Multiple Tools)' END,"
						+ "        MAX(CU.CU)"
						+ " FROM ARTIFACT A, SOURCE_LOCATION SL, COMPILATION_UNIT CU, ARTIFACT_FINDING_RELTN AFR, ARTIFACT_TYPE ART, TOOL T"
						+ " WHERE A.SCAN_ID = ? AND"
						+ "       SL.ID = A.PRIMARY_SOURCE_LOCATION_ID AND"
						+ "       CU.ID = SL.COMPILATION_UNIT_ID AND"
						+ "       AFR.ARTIFACT_ID = A.ID AND"
						+ "       ART.ID = A.ARTIFACT_TYPE_ID AND"
						+ "       T.ID = ART.TOOL_ID"
						+ " GROUP BY AFR.FINDING_ID");
		populateScanRelationOverview = conn
				.prepareStatement("INSERT INTO SCAN_FINDING_RELATION_OVERVIEW (SCAN_ID,PARENT_FINDING_ID,CHILD_FINDING_ID,RELATION_TYPE) "
						+ "SELECT "
						+ "   S.ID,PFR.FINDING_ID,CFR.FINDING_ID,ANR.RELATION_TYPE"
						+ "FROM ARTIFACT_NUMBER_RELTN ANR, ARTIFACT P, ARTIFACT C, ARTIFACT_FINDING_RELTN PFR, ARTIFACT_FINDING_RELTN CFR, SCAN S"
						+ "WHERE S.ID = ? AND "
						+ "      ANR.SCAN_ID = S.ID AND "
						+ "      P.SCAN_ID = ANR.SCAN_ID AND "
						+ "      P.SCAN_NUMBER = ANR.PARENT_NUMBER AND "
						+ "      C.SCAN_ID = ANR.SCAN_ID AND "
						+ "      C.SCAN_NUMBER = ANR.CHILD_NUMBER AND "
						+ "      PFR.ARTIFACT_ID = P.ID AND "
						+ "      CFR.ARTIFACT_ID = C.ID ");
		scanArtifacts = conn
				.prepareStatement("SELECT A.ID,A.PRIORITY,A.SEVERITY,A.MESSAGE,S.PROJECT_ID,SL.HASH,SL.CLASS_NAME,CU.PACKAGE_NAME,ART.FINDING_TYPE_ID"
						+ " FROM SCAN S, ARTIFACT A, ARTIFACT_TYPE ART, SOURCE_LOCATION SL, COMPILATION_UNIT CU"
						+ " WHERE"
						+ " S.ID = ? AND A.SCAN_ID = S.ID AND SL.ID = A.PRIMARY_SOURCE_LOCATION_ID AND CU.ID = SL.COMPILATION_UNIT_ID AND ART.ID = A.ARTIFACT_TYPE_ID");
		scanArtifacts.setFetchSize(FETCH_SIZE);
		selectFindingById = conn
				.prepareStatement("SELECT UUID,PROJECT_ID,IMPORTANCE,SUMMARY,IS_READ,OBSOLETED_BY_ID,OBSOLETED_BY_REVISION FROM FINDING WHERE ID = ?");

	}

	/**
	 * Generate findings for the scan with the given uid.
	 * 
	 * @param uid
	 */
	public void generateFindings(String projectName, String uid,
			FindingFilter filter, SLProgressMonitor monitor) {
		try {
			final ScanRecord scan = ScanRecordFactory.getInstance(conn)
					.newScan();
			scan.setUid(uid);
			if (!scan.select()) {
				throw new IllegalArgumentException("No scan with uid " + uid
						+ " exists in the database");
			}
			final Long projectId = scan.getProjectId();
			scanArtifacts.setLong(1, scan.getId());
			final ResultSet result = scanArtifacts.executeQuery();
			int counter = 0;
			try {
				while (result.next()) {
					final ArtifactResult art = createArtifactResult(result);
					final Long findingId = getFindingId(filter, projectId, art);
					final LongRelationRecord afr = factory.newArtifactFinding();
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
								ScanManager.getInstance(conn).deleteScan(uid,
										null);
								return;
							}
							monitor.worked(1);
						}
					}
				}
			} finally {
				result.close();
			}
			if (log.isLoggable(Level.FINE)) {
				log.fine("All new findings (" + counter
						+ ") persisted for scan " + uid + " in project "
						+ projectName + ".");
			}
		} catch (final SQLException e) {
			sqlError(e);
		}
	}

	protected Long getFindingId(FindingFilter filter, Long projectId,
			ArtifactResult art) throws SQLException {
		Long findingId;
		if (!art.m.select()) {
			// We don't have a match, so we need to produce an
			// entirely
			// new finding
			final MatchRecord m = art.m;
			final Importance importance = filter.calculateImportance(art.m
					.getId().getFindingTypeId(), art.p, art.s);
			final FindingRecord f = factory.newFinding();
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
		return findingId;
	}

	protected ArtifactResult createArtifactResult(ResultSet result)
			throws SQLException {
		final ArtifactResult art = new ArtifactResult();
		int idx = 1;
		art.id = result.getLong(idx++);
		art.p = Priority.values()[result.getInt(idx++)];
		art.s = Severity.values()[result.getInt(idx++)];
		art.message = result.getString(idx++);
		art.m = factory.newMatch();
		// R.PROJECT_ID,S.HASH,CU.CLASS_NAME,CU.PACKAGE_NAME,A.FINDING_TYPE_ID
		final MatchRecord.PK pk = new MatchRecord.PK();
		pk.setProjectId(result.getLong(idx++));
		pk.setHash(result.getLong(idx++));
		pk.setClassName(result.getString(idx++));
		pk.setPackageName(result.getString(idx++));
		pk.setFindingTypeId(result.getLong(idx++));
		art.m.setId(pk);
		return art;
	}

	/**
	 * Delete all findings for the given project.
	 * 
	 * @param projectName
	 * @param monitor
	 * @throws SQLException
	 */
	public void deleteFindings(String projectName, SLProgressMonitor monitor)
			throws SQLException {

		if (monitor != null) {
			monitor.subTask("Deleting matches for project " + projectName);
		}
		deleteMatches.setString(1, projectName);
		deleteMatches.executeUpdate();
		if (monitor != null) {
			if (monitor.isCanceled()) {
				return;
			}
			monitor.worked(1);
		}
		if (monitor != null) {
			monitor.subTask("Deleting findings for project " + projectName);
		}
		deleteFindings.setString(1, projectName);
		deleteFindings.executeUpdate();
		if (monitor != null) {
			if (monitor.isCanceled()) {
				return;
			}
			monitor.worked(1);
		}
	}

	public long getLatestAuditRevision(String projectName) throws SQLException {
		latestAuditRevision.setString(1, projectName);
		final ResultSet set = latestAuditRevision.executeQuery();
		try {
			if (set.next()) {
				return set.getLong(1);
			} else {
				return -1L;
			}
		} finally {
			set.close();
		}
	}

	protected void comment(Long userId, Long findingId, String comment,
			Date time, Long revision) throws SQLException {
		newAudit(userId, findingId, comment, AuditEvent.COMMENT, time, revision);
		touchFinding(findingId, time);
	}

	protected void setImportance(Long userId, Long findingId,
			Importance importance, Date time, Long revision)
			throws SQLException {
		newAudit(userId, findingId, importance.toString(),
				AuditEvent.IMPORTANCE, time, revision);
		updateFindingImportance.setInt(1, importance.ordinal());
		updateFindingImportance.setTimestamp(2, new Timestamp(time.getTime()));
		updateFindingImportance.setLong(3, findingId);
		updateFindingImportance.execute();
		touchFinding(findingId, time);
	}

	protected void markAsRead(Long userId, Long findingId, Date time,
			Long revision) throws SQLException {
		newAudit(userId, findingId, null, AuditEvent.READ, time, revision);
		touchFinding(findingId, time);
	}

	protected void changeSummary(Long userId, Long findingId, String summary,
			Date time, Long revision) throws SQLException {
		newAudit(userId, findingId, summary, AuditEvent.SUMMARY, time, revision);
		updateFindingSummary.setString(1, summary);
		updateFindingSummary.setTimestamp(2, new Timestamp(time.getTime()));
		updateFindingSummary.setLong(3, findingId);
		updateFindingSummary.execute();
		touchFinding(findingId, time);
	}

	protected void populateScanOverview(Long scanId) throws SQLException {
		deleteScanOverview.setLong(1, scanId);
		deleteScanOverview.execute();
		populateScanOverview.setLong(1, scanId);
		populateScanOverview.setLong(2, scanId);
		populateScanOverview.execute();
		populateScanRelationOverview.setLong(1, scanId);
		populateScanRelationOverview.execute();
	}

	private void touchFinding(Long findingId, Date time) throws SQLException {
		touchFinding.setTimestamp(1, new Timestamp(time.getTime()));
		touchFinding.setTimestamp(2, new Timestamp(time.getTime()));
		touchFinding.setLong(3, findingId);
		touchFinding.execute();
	}

	private void newAudit(Long userId, Long findingId, String value,
			AuditEvent event, Date time, Long revision) throws SQLException {
		final AuditRecord record = factory.newAudit();
		record.setUserId(userId);
		record.setTimestamp(time);
		record.setEvent(event);
		record.setValue(value);
		record.setFindingId(findingId);
		record.setRevision(revision);
		if (!record.select()) {
			record.insert();
		}
	}

	/**
	 * Get a finding by it's id.
	 * 
	 * @param findingId
	 * @return
	 * @throws SQLException
	 */
	protected FindingRecord getFinding(long findingId) throws SQLException {
		final FindingRecord record = factory.newFinding();
		selectFindingById.setLong(1, findingId);
		final ResultSet set = selectFindingById.executeQuery();
		try {
			if (set.next()) {
				int idx = 1;
				record.setId(findingId);
				record.setUid(set.getString(idx++));
				record.readAttributes(set, idx);
				return record;
			} else {
				return null;
			}
		} finally {
			set.close();
		}
	}

	/**
	 * Obsolete a finding with another finding. This involves merging all of the
	 * matches and audits of the old finding into the new finding, and then
	 * marking the old finding as obsolete.
	 * 
	 * @param obsolete
	 * @param finding
	 * @param revision
	 * @throws SQLException
	 */
	protected void obsolete(Long obsolete, Long finding, Long revision)
			throws SQLException {
		obsoleteArtifacts.setLong(1, finding);
		obsoleteArtifacts.setLong(2, obsolete);
		obsoleteArtifacts.execute();
		obsoleteMatches.setLong(1, finding);
		obsoleteMatches.setLong(2, obsolete);
		obsoleteMatches.execute();
		obsoleteAudits.setLong(1, finding);
		obsoleteAudits.setLong(2, obsolete);
		obsoleteAudits.execute();
		obsoleteOverview.setLong(1, finding);
		obsoleteOverview.setLong(2, obsolete);
		obsoleteOverview.execute();
		obsoleteFinding.setLong(1, finding);
		obsoleteFinding.setLong(2, revision);
		obsoleteFinding.setLong(3, obsolete);
		obsoleteFinding.execute();
	}

	/**
	 * Delete a local finding, and pass any uncommitted changes to the new
	 * finding.
	 * 
	 * @param deleted
	 * @param finding
	 */
	protected void delete(long deleted, long finding) throws SQLException {
		final FindingRecord fRec = getFinding(deleted);
		obsoleteArtifacts.setLong(1, finding);
		obsoleteArtifacts.setLong(2, deleted);
		obsoleteArtifacts.execute();
		obsoleteMatches.setLong(1, finding);
		obsoleteMatches.setLong(2, deleted);
		obsoleteMatches.execute();
		obsoleteAudits.setLong(1, finding);
		obsoleteAudits.setLong(2, deleted);
		obsoleteAudits.execute();
		obsoleteOverview.setLong(1, finding);
		obsoleteOverview.setLong(2, deleted);
		obsoleteOverview.execute();
		fRec.delete();
	}

	protected void sqlError(SQLException e) {
		throw new FindingGenerationException(e);
	}

	// FIXME Evil code!
	protected long getUserId(String user) throws SQLException {
		return ClientUser.getUser(user, conn).getId();
	}

	protected void fillKey(MatchRecord.PK pk, Match match) throws SQLException {
		pk.setClassName(match.getClassName());
		pk.setPackageName(match.getPackageName());
		pk.setHash(match.getHash());
		final String ft = match.getFindingType();
		pk.setFindingTypeId(ftManager.getFindingTypeId(ft));
	}

	protected static class ArtifactResult {
		Long id;
		Priority p;
		Severity s;
		String message;
		MatchRecord m;
	}

	public static FindingManager getInstance(Connection conn)
			throws SQLException {
		return new FindingManager(conn);
	}
}
