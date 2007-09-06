package com.surelogic.sierra.jdbc.finding;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import com.surelogic.common.SLProgressMonitor;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.jdbc.record.AuditRecord;
import com.surelogic.sierra.jdbc.record.FindingRecord;
import com.surelogic.sierra.jdbc.record.LongRelationRecord;
import com.surelogic.sierra.jdbc.record.MatchRecord;
import com.surelogic.sierra.jdbc.record.RelationRecord;
import com.surelogic.sierra.jdbc.record.ScanRecord;
import com.surelogic.sierra.jdbc.scan.ScanRecordFactory;
import com.surelogic.sierra.jdbc.tool.MessageFilter;
import com.surelogic.sierra.jdbc.user.User;
import com.surelogic.sierra.tool.message.AuditEvent;
import com.surelogic.sierra.tool.message.AuditTrailUpdate;
import com.surelogic.sierra.tool.message.AuditTrails;
import com.surelogic.sierra.tool.message.Importance;
import com.surelogic.sierra.tool.message.Merge;
import com.surelogic.sierra.tool.message.Priority;
import com.surelogic.sierra.tool.message.Severity;
import com.surelogic.sierra.tool.message.TrailObsoletion;

public abstract class FindingManager {

	protected static final Logger log = SLLogger
			.getLoggerFor(FindingManager.class);

	private static final int CHUNK_SIZE = 1000;

	protected final Connection conn;
	private final FindingRecordFactory fact;

	private final PreparedStatement selectFinding;
	private final PreparedStatement markFindingAsRead;
	private final PreparedStatement updateFindingImportance;

	// private final PreparedStatement getLocalMerges;

	FindingManager(Connection conn) throws SQLException {
		this.conn = conn;
		this.fact = ClientFindingRecordFactory.getInstance(conn);
		selectFinding = conn
				.prepareStatement("SELECT ID,IMPORTANCE FROM FINDING WHERE ID = ?");
		markFindingAsRead = conn
				.prepareStatement("UPDATE FINDING SET IS_READ = 'Y' WHERE ID = ?");
		updateFindingImportance = conn
				.prepareStatement("UPDATE FINDING SET IMPORTANCE = ? WHERE ID = ?");
		// getLocalMerges = conn.prepareStatement("");
	}

	protected abstract ResultSet getUnassignedArtifacts(ScanRecord scan)
			throws SQLException;

	protected abstract FindingRecordFactory getFactory();

	public void comment(Long findingId, String comment) throws SQLException {
		FindingView f = getFinding(findingId);
		if (f == null)
			throw new IllegalArgumentException(findingId
					+ " is not a valid finding id.");
		newAudit(findingId, comment, AuditEvent.COMMENT).insert();
	}

	public void setImportance(Long findingId, Importance importance)
			throws SQLException {
		FindingView f = getFinding(findingId);
		if (f == null)
			throw new IllegalArgumentException(findingId
					+ " is not a valid finding id.");
		newAudit(findingId, importance.toString(), AuditEvent.IMPORTANCE)
				.insert();
		updateFindingImportance.setInt(1, importance.ordinal());
		updateFindingImportance.setLong(2, findingId);
		updateFindingImportance.execute();
	}

	public void markAsRead(Long findingId) throws SQLException {
		FindingView f = getFinding(findingId);
		if (f == null)
			throw new IllegalArgumentException(findingId
					+ " is not a valid finding id.");
		newAudit(findingId, null, AuditEvent.READ).insert();
		markFindingAsRead.setLong(1, findingId);
		markFindingAsRead.execute();
	}

	/**
	 * Generate findings for the scan with the given uid
	 * 
	 * @param uid
	 */
	public void generateFindings(String projectName, String uid,
			MessageFilter filter) {
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
				if (++counter == CHUNK_SIZE) {
					conn.commit();
					counter = 0;
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

	public void updateLocalFindings(Long revison,
			List<TrailObsoletion> obsoletions, List<AuditTrailUpdate> updates,
			SLProgressMonitor monitor) {

	}

	public List<Merge> getNewLocalMerges(String project,
			SLProgressMonitor monitor) {
		return null;
	}

	public void updateLocalTrailUids(String project, Long revision,
			List<String> trails, List<Merge> merges) {

	}

	public AuditTrails getNewLocalAudits(String project,
			SLProgressMonitor monitor) {
		return null;
	}

	private AuditRecord newAudit(Long findingId, String value, AuditEvent event)
			throws SQLException {
		AuditRecord record = fact.newAudit();
		record.setUserId(User.getUser(conn).getId());
		record.setTimestamp(new Date());
		record.setEvent(event);
		record.setValue(value);
		record.setFindingId(findingId);
		return record;
	}

	private void sqlError(SQLException e) {
		throw new FindingGenerationException(e);
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

	public Long getLatestAuditRevision(String projectName) {
		// TODO Auto-generated method stub
		return null;
	}

}
