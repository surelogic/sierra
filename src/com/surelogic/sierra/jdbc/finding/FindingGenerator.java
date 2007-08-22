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

	private static final String UPDATE_ARTIFACTS_WITH_EXISTING_MATCH = "UPDATE ARTIFACT A"
			+ " SET A.FINDING_ID =("
			+ " SELECT SM.FINDING_ID"
			+ " FROM RUN R, SOURCE_LOCATION S, COMPILATION_UNIT CU, SIERRA_MATCH SM"
			+ " WHERE R.ID = A.RUN_ID AND"
			+ " S.ID = A.PRIMARY_SOURCE_LOCATION_ID AND"
			+ " CU.ID = S.COMPILATION_UNIT_ID AND"
			+ " SM.PROJECT_ID = R.PROJECT_ID AND"
			+ " SM.HASH = S.HASH AND"
			+ " SM.CLASS_NAME = CU.CLASS_NAME AND"
			+ " SM.PACKAGE_NAME = CU.PACKAGE_NAME AND"
			+ " SM.FINDING_TYPE_ID = A.FINDING_TYPE_ID AND"
			+ " SM.FINDING_ID IS NOT NULL"
			+ " )"
			+ " WHERE A.RUN_ID = ? AND A.FINDING_ID IS NULL";

	private static final String UNASSIGNED_ARTIFACTS_SELECT = "SELECT A.PRIORITY,A.SEVERITY,R.PROJECT_ID,S.HASH,CU.CLASS_NAME,CU.PACKAGE_NAME,A.FINDING_TYPE_ID"
			+ " FROM ARTIFACT A, RUN R, SOURCE_LOCATION S, COMPILATION_UNIT CU"
			+ " WHERE"
			+ " A.FINDING_ID IS NULL AND A.RUN_ID = ? AND R.ID = A.RUN_ID AND S.ID = A.PRIMARY_SOURCE_LOCATION_ID AND CU.ID = S.COMPILATION_UNIT_ID";

	private final PreparedStatement updateArtifactsWithExistingMatch;
	private final PreparedStatement unassignedArtifacts;

	private final FindingRecordFactory factory;
	private final Connection conn;

	public FindingGenerator(Connection conn, FindingRecordFactory factory) {
		this.factory = factory;
		this.conn = conn;
		try {
			// THese queries stay
			updateArtifactsWithExistingMatch = conn
					.prepareStatement(UPDATE_ARTIFACTS_WITH_EXISTING_MATCH);
			unassignedArtifacts = conn
					.prepareStatement(UNASSIGNED_ARTIFACTS_SELECT);

		} catch (SQLException e) {
			throw new FindingGenerationException(e);
		}
	}

	public void generate(RunRecord run) {
		try {
			Long projectId = run.getProjectId();
			updateArtifactsWithExistingMatch.setLong(1, run.getId());
			updateArtifactsWithExistingMatch.executeUpdate();
			conn.commit();
			unassignedArtifacts.setLong(1, run.getId());
			ResultSet result = unassignedArtifacts.executeQuery();

			int counter = 0;
			while (result.next()) {
				ArtifactResult art = new ArtifactResult();
				int idx = 1;
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
				if (!art.m.select()) {
					// We don't have a match, so we need to produce an entirely
					// new finding and trail.
					counter++;
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
					m.setFindingId(f.getId());
					m.update();
				}
				if (counter == CHUNK_SIZE) {
					conn.commit();
					counter = 0;
				}
			}
			conn.commit();
			result.close();
			log.info("All new findings persisted.");
			updateArtifactsWithExistingMatch.executeUpdate();
			log.info("Finished finding generation");
			conn.commit();
		} catch (SQLException e) {
			sqlError(e);
		}
	}

	private static class ArtifactResult {
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