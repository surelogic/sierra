package com.surelogic.sierra.jdbc.finding;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.jdbc.record.FindingRecord;
import com.surelogic.sierra.jdbc.record.LongRelationRecord;
import com.surelogic.sierra.jdbc.record.MatchRecord;
import com.surelogic.sierra.jdbc.record.RelationRecord;
import com.surelogic.sierra.jdbc.record.RunRecord;
import com.surelogic.sierra.jdbc.record.TrailRecord;
import com.surelogic.sierra.jdbc.run.RunRecordFactory;
import com.surelogic.sierra.tool.message.Importance;
import com.surelogic.sierra.tool.message.Priority;
import com.surelogic.sierra.tool.message.Severity;

public abstract class FindingManager {

	protected static final Logger log = SLLogger
			.getLoggerFor(FindingManager.class);

	private static final int CHUNK_SIZE = 1000;

	protected final Connection conn;

	FindingManager(Connection conn) throws SQLException {
		this.conn = conn;
	}

	protected abstract ResultSet getUnassignedArtifacts(RunRecord run)
			throws SQLException;

	protected abstract FindingRecordFactory getFactory();

	/**
	 * Generate findings for the run with the given uid
	 * 
	 * @param uid
	 */
	public void generateFindings(String uid) {
		try {

			FindingRecordFactory factory = getFactory();
			RunRecord run = RunRecordFactory.getInstance(conn).newRun();
			run.setUid(uid);
			if (!run.select()) {
				throw new IllegalArgumentException("No run with uid " + uid
						+ " exists in the database");
			}
			Long projectId = run.getProjectId();

			ResultSet result = getUnassignedArtifacts(run);

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
					// new finding and trail.
					MatchRecord m = art.m;
					TrailRecord t = factory.newTrail();
					t.setProjectId(projectId);
					FindingRecord f = factory.newFinding();
					f.setTrail(t);
					f.setImportance(calculateImportance(art.s, art.p));
					t.insert();
					f.insert();
					m.setFindingId(f.getId());
					m.setTrailId(t.getId());
					m.insert();
					findingId = f.getId();
				} else if (art.m.getFindingId() == null) {
					// If we have a match with a trail, but no finding, we
					// generate a finding and give it that trail.
					MatchRecord m = art.m;
					FindingRecord f = factory.newFinding();
					TrailRecord t = factory.newTrail();
					t.setId(m.getTrailId());
					f.setTrail(t);
					f.setImportance(calculateImportance(art.s, art.p));
					f.insert();
					findingId = f.getId();
					m.setFindingId(findingId);
					m.update();
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
			log.info("All new findings persisted.");
		} catch (SQLException e) {
			sqlError(e);
		}
	}

	private static Importance calculateImportance(Severity severity,
			Priority priority) {
		return Importance.values()[((int) (((float) (severity.ordinal() + priority
				.ordinal())) / 2))];
	}

	private void sqlError(SQLException e) {
		throw new FindingGenerationException(e);
	}

	private static class ArtifactResult {
		Long id;
		Priority p;
		Severity s;
		MatchRecord m;
	}

	public static FindingManager getInstance(Connection conn)
			throws SQLException {
		return new ClientFindingManager(conn);
	}

	public static FindingManager getInstance(Connection conn, String qualifier)
			throws SQLException {
		return new QualifiedFindingManager(conn, qualifier);
	}
}
