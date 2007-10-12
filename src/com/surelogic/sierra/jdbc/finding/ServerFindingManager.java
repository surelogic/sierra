package com.surelogic.sierra.jdbc.finding;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
import com.surelogic.sierra.jdbc.scan.ScanRecordFactory;
import com.surelogic.sierra.tool.message.Audit;
import com.surelogic.sierra.tool.message.AuditEvent;
import com.surelogic.sierra.tool.message.AuditTrail;
import com.surelogic.sierra.tool.message.AuditTrailUpdate;
import com.surelogic.sierra.tool.message.Match;
import com.surelogic.sierra.tool.message.Merge;
import com.surelogic.sierra.tool.message.TrailObsoletion;

public final class ServerFindingManager extends FindingManager {

	private final PreparedStatement populateSeriesOverview;
	private final PreparedStatement populateTempIds;
	private final PreparedStatement deleteTempIds;
	private final PreparedStatement selectObsoletedTrails;
	private final PreparedStatement selectUpdatedMatches;
	private final PreparedStatement selectUpdatedAudits;

	private ServerFindingManager(Connection conn) throws SQLException {
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
				populateTempIds = conn
						.prepareStatement("INSERT INTO TEMP_FINDING_IDS"
								+ "  SELECT SO.FINDING_ID FROM SCAN_OVERVIEW SO WHERE SO.SCAN_ID = ?"
								+ "  MINUS "
								+ "  SELECT TSO.FINDING_ID FROM TIME_SERIES_OVERVIEW TSO WHERE QUALIFIER_ID = ? AND PROJECT_ID = ?");
			} else {
				try {
					st
							.execute("DECLARE GLOBAL TEMPORARY TABLE TEMP_FINDING_IDS (ID BIGINT NOT NULL) NOT LOGGED");
				} catch (SQLException e) {
					// Do nothing, the table is probably already there.
				}
				tempTableName = "SESSION.TEMP_FINDING_IDS";
				populateTempIds = conn
						.prepareStatement("INSERT INTO SESSION.TEMP_FINDING_IDS "
								+ "  SELECT SO.FINDING_ID FROM SCAN_OVERVIEW SO WHERE SO.SCAN_ID = ?"
								+ "  EXCEPT"
								+ "  SELECT TSO.FINDING_ID FROM TIME_SERIES_OVERVIEW TSO WHERE QUALIFIER_ID = ? AND PROJECT_ID = ?");
			}
		} finally {
			st.close();
		}
		deleteTempIds = conn.prepareStatement("DELETE FROM " + tempTableName);
		populateSeriesOverview = conn
				.prepareStatement("INSERT INTO TIME_SERIES_OVERVIEW"
						+ " SELECT ?, F.ID,F.PROJECT_ID,"
						+ "        CASE WHEN F.IS_READ = 'Y' THEN 'Yes' ELSE 'No' END,"
						+ "        F.LAST_CHANGED,"
						+ "        CASE"
						+ "             WHEN F.IMPORTANCE=0 THEN 'Irrelevant'"
						+ " 	        WHEN F.IMPORTANCE=1 THEN 'Low'"
						+ "             WHEN F.IMPORTANCE=2 THEN 'Medium'"
						+ "             WHEN F.IMPORTANCE=3 THEN 'High'"
						+ "             WHEN F.IMPORTANCE=4 THEN 'Critical'"
						+ "        END,"
						+ "        CASE WHEN COUNT.COUNT IS NULL THEN 0 ELSE COUNT.COUNT END,"
						+ "        ?,"
						+ "        LM.PACKAGE_NAME,"
						+ "        LM.CLASS_NAME,"
						+ "        FT.NAME,"
						+ "        F.SUMMARY"
						+ " FROM "
						+ tempTableName
						+ " TF"
						+ "    INNER JOIN FINDING F ON F.ID = TF.ID"
						+ "    LEFT OUTER JOIN ("
						+ "       SELECT"
						+ "          A.FINDING_ID \"ID\", COUNT(*) \"COUNT\""
						+ "       FROM SIERRA_AUDIT A"
						+ "       WHERE A.EVENT='COMMENT'"
						+ "       GROUP BY A.FINDING_ID) COUNT ON COUNT.ID = F.ID"
						+ "    INNER JOIN LOCATION_MATCH LM ON LM.FINDING_ID = F.ID"
						+ "    INNER JOIN FINDING_TYPE FT ON FT.ID = LM.FINDING_TYPE_ID");
		selectObsoletedTrails = conn
				.prepareStatement("SELECT OBS.UUID,OBS.OBSOLETED_BY_REVISION,F.UUID FROM FINDING OBS, FINDING F"
						+ "   WHERE"
						+ "   OBS.PROJECT_ID = ? AND"
						+ "   OBS.OBSOLETED_BY_ID IS NOT NULL AND"
						+ "   OBS.OBSOLETED_BY_REVISION > ? AND"
						+ "   F.ID = OBS.OBSOLETED_BY_ID");
		selectUpdatedMatches = conn
				.prepareStatement("SELECT F.UUID,LM.PACKAGE_NAME,LM.CLASS_NAME,LM.HASH,FT.UUID"
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
				}
				log.info("Populating scan overview for scan with uid "
						+ scan.getUid() + ".");
				populateScanOverview(scan.getId());
				for (QualifierRecord q : qualifierRecs) {
					int idx = 1;
					// Add the new findings to the time series overview
					populateTempIds.setLong(idx++, scan.getId());
					populateTempIds.setLong(idx++, q.getId());
					populateTempIds.setLong(idx++, projectRec.getId());
					populateTempIds.execute();
					idx = 1;
					// Look up previous scan in qualifier
					populateSeriesOverview.setLong(idx++, q.getId());
					populateSeriesOverview.setString(idx++, projectRec
							.getName());
					populateSeriesOverview.execute();
					deleteTempIds.execute();
				}
				log.info("Overview for qualifiers " + qualifiers
						+ " was generated with respect to scan with uid "
						+ scan.getUid() + ".");
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
	 * @param project
	 * @param revision
	 * @param merges
	 * @return
	 * @throws SQLException
	 */
	public List<String> mergeAuditTrails(String project, Long revision,
			List<Merge> merges) throws SQLException {
		ProjectRecord projectRecord = ProjectRecordFactory.getInstance(conn)
				.newProject();
		projectRecord.setName(project);
		if (!projectRecord.select()) {
			projectRecord.insert();
		}
		List<String> trails = new ArrayList<String>();
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
				// Try to look up any existing matches
				for (MatchRecord.PK matchId : matchIds) {
					matchRecord.setId(matchId);
					if (matchRecord.select()) {
						if (getFinding(matchRecord.getFindingId()).getUid() != null) {
							findings.add(matchRecord.getFindingId());
						}
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
						delete(oldFinding, findingId);
						matchRecord.setFindingId(findingId);
						matchRecord.setRevision(revision);
						matchRecord.update();
					} else {
						matchRecord.setFindingId(findingId);
						matchRecord.setRevision(revision);
						matchRecord.insert();
					}
				}
				trails.add(uuid);
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

	public static ServerFindingManager getInstance(Connection conn)
			throws SQLException {
		return new ServerFindingManager(conn);
	}

}
