package com.surelogic.sierra.jdbc.finding;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

import com.surelogic.sierra.jdbc.record.FindingRecord;
import com.surelogic.sierra.jdbc.record.MatchRecord;
import com.surelogic.sierra.jdbc.record.RunRecord;
import com.surelogic.sierra.jdbc.record.TrailRecord;
import com.surelogic.sierra.tool.SierraLogger;
import com.surelogic.sierra.tool.message.Importance;
import com.surelogic.sierra.tool.message.Priority;
import com.surelogic.sierra.tool.message.Severity;

public class FindingGenerator {

	private static final Logger log = SierraLogger
			.getLogger(FindingGenerator.class.getName());

	private static final int CHUNK_SIZE = 1000;

	private static final String INSERT_ARTIFACT_FINDING_RELATION = "INSERT INTO ARTIFACT_FINDING_RELTN (ARTIFACT_ID,FINDING_ID) VALUES (?,?)";
	private static final String UNASSIGNED_ARTIFACTS_SELECT = "SELECT A.ID,A.PRIORITY,A.SEVERITY,R.PROJECT_ID,S.HASH,CU.CLASS_NAME,CU.PACKAGE_NAME,A.FINDING_TYPE_ID"
			+ " FROM (SELECT U.ID FROM ARTIFACT U LEFT OUTER JOIN ARTIFACT_FINDING_RELTN AFR ON AFR.ARTIFACT_ID = U.ID WHERE U.RUN_ID = ? AND AFR.ARTIFACT_ID IS NULL) AS UNASSIGNED, "
			+ " ARTIFACT A, RUN R, SOURCE_LOCATION S, COMPILATION_UNIT CU"
			+ " WHERE"
			+ " A.ID = UNASSIGNED.ID AND R.ID = A.RUN_ID AND S.ID = A.PRIMARY_SOURCE_LOCATION_ID AND CU.ID = S.COMPILATION_UNIT_ID";

	// private final PreparedStatement updateArtifactsWithExistingMatch;
	private final PreparedStatement unassignedArtifacts;
	private final PreparedStatement insertArtifactFindingRelation;
	private final FindingRecordFactory factory;
	private final Connection conn;

	public FindingGenerator(Connection conn) {
		this.conn = conn;
		try {
			this.factory = ClientFindingRecordFactory.getInstance(conn);
			// THese queries stay
			unassignedArtifacts = conn
					.prepareStatement(UNASSIGNED_ARTIFACTS_SELECT);
			insertArtifactFindingRelation = conn
					.prepareStatement(INSERT_ARTIFACT_FINDING_RELATION);
		} catch (SQLException e) {
			throw new FindingGenerationException(e);
		}
	}

	public void generate(RunRecord run) {
		try {
			Long projectId = run.getProjectId();
			unassignedArtifacts.setLong(1, run.getId());
			ResultSet result = unassignedArtifacts.executeQuery();

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
				insertArtifactFindingRelation.setLong(1, art.id);
				insertArtifactFindingRelation.setLong(2, findingId);
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

	private static class ArtifactResult {
		Long id;
		Priority p;
		Severity s;
		MatchRecord m;
	}

	private static Importance calculateImportance(Severity severity,
			Priority priority) {
		return Importance.values()[((int) (((float) (severity.ordinal() + priority
				.ordinal())) / 2))];
	}

	private void sqlError(SQLException e) {
		throw new FindingGenerationException(e);
	}
}