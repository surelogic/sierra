package com.surelogic.sierra.jdbc.finding;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.surelogic.common.SLProgressMonitor;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.jdbc.project.ProjectRecordFactory;
import com.surelogic.sierra.jdbc.record.AuditRecord;
import com.surelogic.sierra.jdbc.record.FindingRecord;
import com.surelogic.sierra.jdbc.record.LongRelationRecord;
import com.surelogic.sierra.jdbc.record.MatchRecord;
import com.surelogic.sierra.jdbc.record.ProjectRecord;
import com.surelogic.sierra.jdbc.record.RelationRecord;
import com.surelogic.sierra.jdbc.record.ScanRecord;
import com.surelogic.sierra.jdbc.scan.ScanManager;
import com.surelogic.sierra.jdbc.scan.ScanRecordFactory;
import com.surelogic.sierra.jdbc.tool.FindingTypeManager;
import com.surelogic.sierra.jdbc.tool.MessageFilter;
import com.surelogic.sierra.jdbc.user.User;
import com.surelogic.sierra.tool.message.Audit;
import com.surelogic.sierra.tool.message.AuditEvent;
import com.surelogic.sierra.tool.message.AuditTrail;
import com.surelogic.sierra.tool.message.AuditTrailUpdate;
import com.surelogic.sierra.tool.message.Importance;
import com.surelogic.sierra.tool.message.Match;
import com.surelogic.sierra.tool.message.Merge;
import com.surelogic.sierra.tool.message.Priority;
import com.surelogic.sierra.tool.message.Severity;
import com.surelogic.sierra.tool.message.TrailObsoletion;

public abstract class FindingManager {

	protected static final Logger log = SLLogger
			.getLoggerFor(FindingManager.class);

	private static final int CHUNK_SIZE = 1000;
	private static final int CHECK_SIZE = 10;

	protected final Connection conn;

	private final FindingTypeManager ftManager;

	private final FindingRecordFactory fact;

	private final PreparedStatement selectFinding;
	private final PreparedStatement markFindingAsRead;
	private final PreparedStatement updateFindingImportance;
	private final PreparedStatement updateFindingUid;
	private final PreparedStatement updateMatchRevision;
	private final PreparedStatement findLocalMerges;
	private final PreparedStatement findLocalAudits;
	private final PreparedStatement obsoleteAudits;
	private final PreparedStatement obsoleteMatches;
	private final PreparedStatement obsoleteFinding;
	private final PreparedStatement latestAuditRevision;
	private final PreparedStatement deleteMatches;
	private final PreparedStatement deleteFindings;
	private final PreparedStatement deleteLocalAudits;

	FindingManager(Connection conn) throws SQLException {
		this.conn = conn;
		this.fact = ClientFindingRecordFactory.getInstance(conn);
		ftManager = FindingTypeManager.getInstance(conn);
		selectFinding = conn
				.prepareStatement("SELECT ID,IMPORTANCE FROM FINDING WHERE ID = ?");
		markFindingAsRead = conn
				.prepareStatement("UPDATE FINDING SET IS_READ = 'Y' WHERE ID = ?");
		updateFindingImportance = conn
				.prepareStatement("UPDATE FINDING SET IMPORTANCE = ? WHERE ID = ?");
		updateFindingUid = conn
				.prepareStatement("UPDATE FINDING SET UID = ? WHERE ID = ?");
		updateMatchRevision = conn
				.prepareStatement("UPDATE LOCATION_MATCH SET REVISION = ? WHERE FINDING_ID = ? AND REVISION IS NULL");
		findLocalMerges = conn
				.prepareStatement("SELECT M.FINDING_ID,M.PACKAGE_NAME,M.CLASS_NAME,M.HASH,FT.NAME"
						+ " FROM LOCATION_MATCH NEW_M, FINDING F, LOCATION_MATCH M, FINDING_TYPE FT"
						+ " WHERE NEW_M.PROJECT_ID = ? AND"
						+ " NEW_M.REVISION IS NULL AND"
						+ " F.ID = NEW_M.FINDING_ID AND"
						+ " F.IS_READ = 'Y' AND"
						+ " M.FINDING_ID = F.ID AND"
						+ " FT.ID = M.FINDING_TYPE_ID" + " ORDER BY FINDING_ID");
		findLocalAudits = conn
				.prepareStatement("SELECT F.UID,A.DATE_TIME,A.EVENT,A.VALUE"
						+ " FROM AUDIT A, FINDING F"
						+ " WHERE A.REVISION IS NULL AND"
						+ " F.ID = A.FINDING_ID AND F.PROJECT_ID = ?"
						+ " AND F.UID IS NOT NULL ORDER BY A.FINDING_ID");
		obsoleteAudits = conn
				.prepareStatement("UPDATE AUDIT SET FINDING_ID = ? WHERE FINDING_ID = ?");
		obsoleteMatches = conn
				.prepareStatement("UPDATE LOCATION_MATCH SET FINDING_ID = ? WHERE FINDING_ID = ?");
		obsoleteFinding = conn
				.prepareStatement("UPDATE FINDING SET OBSOLETED_BY_ID = ?, OBSOLETED_BY_REVISION = ? WHERE ID = ?");
		latestAuditRevision = conn
				.prepareStatement("SELECT MAX(A.REVISION) FROM PROJECT P, FINDING F, AUDIT A WHERE P.NAME = ? AND F.PROJECT_ID = P.ID AND A.FINDING_ID = F.ID");
		deleteMatches = conn
				.prepareStatement("DELETE FROM LOCATION_MATCH WHERE PROJECT_ID = (SELECT P.ID FROM PROJECT P WHERE P.NAME = ?)");
		deleteFindings = conn
				.prepareStatement("DELETE FROM FINDING WHERE PROJECT_ID = (SELECT P.ID FROM PROJECT P WHERE P.NAME = ?)");
		deleteLocalAudits = conn
				.prepareStatement("DELETE FROM AUDIT WHERE FINDING_ID IN (SELECT F.ID FROM FINDING F WHERE F.PROJECT_ID = (SELECT P.ID FROM PROJECT P WHERE P.NAME = ?)) AND USER_ID IS NULL");
	}

	protected abstract ResultSet getUnassignedArtifacts(ScanRecord scan)
			throws SQLException;

	protected abstract FindingRecordFactory getFactory();

	public void comment(Long findingId, String comment) throws SQLException {
		checkIsRead(findingId);
		comment(null, findingId, comment, new Date(), null);
	}

	public void setImportance(Long findingId, Importance importance)
			throws SQLException {
		checkIsRead(findingId);
		setImportance(null, findingId, importance, new Date(), null);
	}

	public void markAsRead(Long findingId) throws SQLException {
		checkIsRead(findingId);
		markAsRead(null, findingId, new Date(), null);
	}

	/**
	 * Generate findings for the scan with the given uid
	 * 
	 * @param uid
	 */
	public void generateFindings(String projectName, String uid,
			MessageFilter filter, SLProgressMonitor monitor) {
		try {

			FindingRecordFactory factory = getFactory();
			ScanRecord scan = ScanRecordFactory.getInstance(conn).newScan();
			scan.setUid(uid);
			if (!scan.select()) {
				throw new IllegalArgumentException("No scan with uid " + uid
						+ " exists in the database");
			}
			Long projectId = scan.getProjectId();

			ResultSet result = getUnassignedArtifacts(scan);

			int counter = 0;
			while (result.next()) {
				ArtifactResult art = new ArtifactResult();
				int idx = 1;
				art.id = result.getLong(idx++);
				art.p = Priority.values()[result.getInt(idx++)];
				art.s = Severity.values()[result.getInt(idx++)];
				art.m = factory.newMatch();
				// R.PROJECT_ID,S.HASH,CU.CLASS_NAME,CU.PACKAGE_NAME,A.FINDING_TYPE_ID
				MatchRecord.PK pk = new MatchRecord.PK();
				pk.setProjectId(result.getLong(idx++));
				pk.setHash(result.getLong(idx++));
				pk.setClassName(result.getString(idx++));
				pk.setPackageName(result.getString(idx++));
				pk.setFindingTypeId(result.getLong(idx++));
				art.m.setId(pk);
				Long findingId;
				if (!art.m.select()) {
					// We don't have a match, so we need to produce an entirely
					// new finding
					MatchRecord m = art.m;
					FindingRecord f = factory.newFinding();
					f.setProjectId(projectId);
					f.setImportance(filter.calculateImportance(art.m.getId()
							.getFindingTypeId(), art.p, art.s));
					f.insert();
					m.setFindingId(f.getId());
					m.insert();
					findingId = f.getId();
				} else {
					findingId = art.m.getFindingId();
				}
				LongRelationRecord afr = factory.newArtifactFinding();
				afr.setId(new RelationRecord.PK<Long, Long>(art.id, findingId));
				afr.insert();
				if ((++counter % CHUNK_SIZE) == 0) {
					conn.commit();
				}
				if ((counter % CHECK_SIZE) == 0) {
					if (monitor != null) {
						if (monitor.isCanceled()) {
							conn.rollback();
							ScanManager.getInstance(conn).deleteScan(uid, null);
							return;
						}
						monitor.worked(1);
					}
				}
			}
			conn.commit();
			result.close();
			log.info("All new findings persisted for scan " + uid
					+ " in project " + projectName + ".");
		} catch (SQLException e) {
			sqlError(e);
		}
	}

	public void deleteFindings(String projectName, SLProgressMonitor monitor)
			throws SQLException {
		if (monitor != null) {
			monitor.subTask("Deleting matches for project " + projectName);
		}
		deleteMatches.setString(1, projectName);
		deleteMatches.executeUpdate();
		if (monitor != null) {
			if (monitor.isCanceled())
				return;
			monitor.worked(1);
		}
		if (monitor != null) {
			monitor.subTask("Deleting findings for project " + projectName);
		}
		deleteFindings.setString(1, projectName);
		deleteFindings.executeUpdate();
		if (monitor != null) {
			if (monitor.isCanceled())
				return;
			monitor.worked(1);
		}
	}

	public void updateLocalFindings(String projectName,
			List<TrailObsoletion> obsoletions, List<AuditTrailUpdate> updates,
			SLProgressMonitor monitor) throws SQLException {
		ProjectRecord project = ProjectRecordFactory.getInstance(conn)
				.newProject();
		project.setName(projectName);
		if (project.select()) {
			if (obsoletions != null) {
				for (TrailObsoletion to : obsoletions) {
					FindingRecord obsoletedFinding = fact.newFinding();
					obsoletedFinding.setUid(to.getObsoletedTrail());
					if (obsoletedFinding.select()) {
						FindingRecord newFinding = fact.newFinding();
						newFinding.setUid(to.getTrail());
						if (!newFinding.select()) {
							newFinding.insert();
						}
						obsolete(obsoletedFinding.getId(), newFinding.getId(),
								to.getRevision());
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
					FindingRecord finding = fact.newFinding();
					finding.setUid(update.getTrail());
					finding.setProjectId(project.getId());
					if (!finding.select()) {
						finding.insert();
					}
					MatchRecord mRec = fact.newMatch();
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
									// This must be a local match, so delete it
									delete(mRec.getFindingId(), finding.getId());
								}
							} else {
								mRec.setFindingId(finding.getId());
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
							default:
								break;
							}
						}
					}
				}
			}
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
						.valueOf(set.getString(idx++)), set.getString(idx++)));
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
		Long oldFinding = null;
		List<Match> matches = null;
		while (set.next()) {
			int idx = 1;
			Long newFinding = set.getLong(idx++);
			if (!newFinding.equals(oldFinding)) {
				oldFinding = newFinding;
				Merge merge = new Merge();
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

		return merges;
	}

	public void updateLocalTrailUids(String projectName, Long revision,
			List<String> trails, List<Merge> merges, SLProgressMonitor monitor)
			throws SQLException {
		ProjectRecord projectRec = ProjectRecordFactory.getInstance(conn)
				.newProject();
		projectRec.setName(projectName);
		if (projectRec.select()) {
			MatchRecord match = fact.newMatch();
			MatchRecord.PK pk = new MatchRecord.PK();
			match.setId(pk);
			pk.setProjectId(projectRec.getId());
			Iterator<String> trailIter = trails.iterator();
			Iterator<Merge> mergeIter = merges.iterator();
			while (mergeIter.hasNext() && trailIter.hasNext()) {
				String trail = trailIter.next();
				Match m = mergeIter.next().getMatch().get(0);
				fillKey(pk, m);
				if (match.select()) {
					updateFindingUid.setString(1, trail);
					updateFindingUid.setLong(2, match.getFindingId());
					updateFindingUid.execute();
					updateMatchRevision.setLong(1, match.getFindingId());
					updateMatchRevision.setLong(2, revision);
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

	public Long getLatestAuditRevision(String projectName) throws SQLException {
		latestAuditRevision.setString(1, projectName);
		ResultSet set = latestAuditRevision.executeQuery();
		if (set.next()) {
			return set.getLong(1);
		}
		return 0L;
	}

	private void comment(Long userId, Long findingId, String comment,
			Date time, Long revision) throws SQLException {
		newAudit(userId, findingId, comment, AuditEvent.COMMENT, time, revision)
				.insert();
	}

	private void setImportance(Long userId, Long findingId,
			Importance importance, Date time, Long revision)
			throws SQLException {
		newAudit(userId, findingId, importance.toString(),
				AuditEvent.IMPORTANCE, time, revision).insert();
		updateFindingImportance.setInt(1, importance.ordinal());
		updateFindingImportance.setLong(2, findingId);
		updateFindingImportance.execute();
	}

	private void markAsRead(Long userId, Long findingId, Date time,
			Long revision) throws SQLException {
		newAudit(userId, findingId, null, AuditEvent.READ, time, revision)
				.insert();
		markFindingAsRead.setLong(1, findingId);
		markFindingAsRead.execute();
	}

	private AuditRecord newAudit(Long userId, Long findingId, String value,
			AuditEvent event, Date time, Long revision) throws SQLException {
		AuditRecord record = fact.newAudit();
		record.setUserId(userId);
		record.setTimestamp(time);
		record.setEvent(event);
		record.setValue(value);
		record.setFindingId(findingId);
		record.setRevision(revision);
		return record;
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
	private void obsolete(Long obsolete, Long finding, Long revision)
			throws SQLException {
		obsoleteMatches.setLong(1, finding);
		obsoleteMatches.setLong(2, obsolete);
		obsoleteMatches.execute();
		obsoleteAudits.setLong(1, finding);
		obsoleteAudits.setLong(2, obsolete);
		obsoleteAudits.execute();
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
	private void delete(Long deleted, Long finding) {
		// TODO
	}

	private void sqlError(SQLException e) {
		throw new FindingGenerationException(e);
	}

	/**
	 * For use in the client. Checks to see if a finding has been read. If it
	 * has not, it marks it as read.
	 * 
	 * @param findingId
	 * @throws SQLException
	 */
	private void checkIsRead(Long findingId) throws SQLException {
		FindingView f = getFinding(findingId);
		if (f == null)
			throw new IllegalArgumentException(findingId
					+ " is not a valid finding id.");
		if (!f.isRead()) {
			markAsRead(null, findingId, new Date(), null);
		}
	}

	private FindingView getFinding(Long findingId) throws SQLException {
		selectFinding.setLong(1, findingId);
		ResultSet set = selectFinding.executeQuery();
		if (set.next()) {
			FindingView f = new FindingView();
			f.read(set, 1);
			return f;
		} else {
			return null;
		}
	}

	private Long getUserId(String user) throws SQLException {
		return User.getUser(user, conn).getId();
	}

	private void fillKey(MatchRecord.PK pk, Match match) throws SQLException {
		pk.setClassName(match.getClassName());
		pk.setPackageName(match.getPackageName());
		pk.setHash(match.getHash());
		String ft = match.getFindingType();
		pk.setFindingTypeId(ftManager.getFindingTypeId(ft));
	}

	private static class ArtifactResult {
		Long id;
		Priority p;
		Severity s;
		MatchRecord m;
	}

	// TODO we only have one finding manager
	public static FindingManager getInstance(Connection conn)
			throws SQLException {
		return new ClientFindingManager(conn);
	}

	public void deleteLocalAudits(String projectName, SLProgressMonitor monitor)
			throws SQLException {
		deleteLocalAudits.setString(1, projectName);
		deleteLocalAudits.executeUpdate();
	}

}
