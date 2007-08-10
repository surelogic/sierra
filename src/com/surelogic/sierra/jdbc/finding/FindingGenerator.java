package com.surelogic.sierra.jdbc.finding;

import static com.surelogic.sierra.jdbc.JDBCUtils.insert;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.surelogic.sierra.tool.message.Importance;
import com.surelogic.sierra.tool.message.Priority;
import com.surelogic.sierra.tool.message.Severity;

public class FindingGenerator {

	private static final int CHUNK_SIZE = 1000;

	private static final String UPDATE_ARTIFACTS_WITH_EXISTING_MATCH = "UPDATE ARTIFACT A"
			+ " SET A.FINDING_ID =("
			+ " SELECT SM.FINDING_ID"
			+ " FROM SOURCE_LOCATION S, COMPILATION_UNIT CU, SIERRA_MATCH SM"
			+ " WHERE S.ID = A.PRIMARY_SOURCE_LOCATION_ID AND"
			+ " CU.ID = S.COMPILATION_UNIT_ID AND"
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

	public static final String MATCH_SELECT = "SELECT FINDING_ID, TRAIL_ID FROM SIERRA_MATCH WHERE PROJECT_ID = ? AND HASH = ? AND CLASS_NAME = ? AND PACKAGE_NAME = ? AND FINDING_TYPE_ID = ?";
	public static final String MATCH_INSERT = "INSERT INTO SIERRA_MATCH (PROJECT_ID, HASH, CLASS_NAME, PACKAGE_NAME, FINDING_TYPE_ID, FINDING_ID, TRAIL_ID) VALUES (?,?,?,?,?,?,?)";
	public static final String MATCH_UPDATE_FINDING = "UPDATE SIERRA_MATCH SET FINDING_ID = ? WHERE PROJECT_ID = ? AND HASH = ? AND CLASS_NAME = ? AND PACKAGE_NAME = ? AND FINDING_TYPE_ID = ?";
	public static final String FINDING_INSERT = "INSERT INTO FINDING (TRAIL_ID, IMPORTANCE) VALUES (?,?)";
	public static final String TRAIL_INSERT = "INSERT INTO TRAIL (UID) VALUES (?)";
	public static final String ARTIFACT_FINDING_SET = "UPDATE ARTIFACT SET FINDING_ID = ? WHERE ID = ?";

	private final PreparedStatement updateArtifactsWithExistingMatch;
	private final PreparedStatement unassignedArtifacts;
	private final PreparedStatement insertMatch;
	private final PreparedStatement updateMatchFinding;
	private final PreparedStatement selectMatch;
	private final PreparedStatement insertFinding;
	private final PreparedStatement insertTrail;
	private final Connection conn;

	public FindingGenerator(Connection conn) {
		this.conn = conn;
		try {
			insertMatch = conn.prepareStatement(MATCH_INSERT,
					Statement.RETURN_GENERATED_KEYS);
			updateMatchFinding = conn.prepareStatement(MATCH_UPDATE_FINDING);
			selectMatch = conn.prepareStatement(MATCH_SELECT,
					Statement.RETURN_GENERATED_KEYS);
			insertTrail = conn.prepareStatement(TRAIL_INSERT,
					Statement.RETURN_GENERATED_KEYS);
			insertFinding = conn.prepareStatement(FINDING_INSERT,
					Statement.RETURN_GENERATED_KEYS);
			updateArtifactsWithExistingMatch = conn
					.prepareStatement(UPDATE_ARTIFACTS_WITH_EXISTING_MATCH);
			unassignedArtifacts = conn
					.prepareStatement(UNASSIGNED_ARTIFACTS_SELECT);
		} catch (SQLException e) {
			throw new FindingGenerationException(e);
		}
	}

	public void generate(Long runId) {
		try {
			updateArtifactsWithExistingMatch.setLong(1, runId);
			updateArtifactsWithExistingMatch.executeUpdate();
			conn.commit();
			unassignedArtifacts.setLong(1, runId);
			ResultSet result = unassignedArtifacts.executeQuery();
			int counter = 0;
			while (result.next()) {
				ArtifactResult art = new ArtifactResult();
				int idx = 1;
				art.p = Priority.values()[result.getInt(idx++)];
				art.s = Severity.values()[result.getInt(idx++)];
				art.m = new MatchRecord();
				idx = art.m.readPk(result, idx);
				art.m.fillWithPk(selectMatch, 1);
				ResultSet match = selectMatch.executeQuery();
				if (!match.next()) {
					// We don't have a match, so we need to produce an entirely
					// new finding and trail.
					counter++;
					MatchRecord m = art.m;
					TrailRecord t = new TrailRecord();
					FindingRecord f = new FindingRecord();
					f.setTrail(t);
					f.setImportance(calculateImportance(art.s, art.p));
					m.setFinding(f);
					m.setTrail(t);
					insert(insertTrail, t);
					insert(insertFinding, f);
					m.fill(insertMatch, 1);
					insertMatch.executeUpdate();
				} else {
					// If we have a match with a trail, but no finding, we
					// generate a finding and give it that trail.
					match.getLong(1);
					if (match.wasNull()) {
						MatchRecord m = art.m;
						FindingRecord f = new FindingRecord();
						TrailRecord t = new TrailRecord();
						t.setId(match.getLong(2));
						f.setTrail(t);
						f.setImportance(calculateImportance(art.s, art.p));
						insert(insertFinding, f);
						updateMatchFinding.setLong(1, f.getId());
						m.fill(updateMatchFinding, 2);
						updateMatchFinding.executeUpdate();
					}
				}
				if (counter == CHUNK_SIZE) {
					conn.commit();
					counter = 0;
				}
			}
			conn.commit();
			updateArtifactsWithExistingMatch.executeUpdate();
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