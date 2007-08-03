package com.surelogic.sierra.jdbc.finding;

import static com.surelogic.sierra.jdbc.JDBCUtils.insert;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
			+ " SM.FINDING_TYPE_ID = A.FINDING_TYPE_ID"
			+ " )"
			+ " WHERE A.RUN_ID = ? AND A.FINDING_ID IS NULL";

	private static final String UPDATE_ARTIFACT_FINDING = "UPDATE ARTIFACT A SET A.FINDING_ID = ? WHERE A.ID = ?";

	private static final String UNASSIGNED_ARTIFACTS_SELECT = "SELECT A.ID,A.PRIORITY,A.SEVERITY,S.HASH,CU.CLASS_NAME,CU.PACKAGE_NAME,A.FINDING_TYPE_ID"
			+ " FROM ARTIFACT A, SOURCE_LOCATION S, COMPILATION_UNIT CU"
			+ " WHERE"
			+ " A.FINDING_ID IS NULL AND A.RUN_ID = ? AND S.ID = A.PRIMARY_SOURCE_LOCATION_ID AND CU.ID = S.COMPILATION_UNIT_ID";

	public static final String MATCH_INSERT = "INSERT INTO SIERRA_MATCH (HASH, CLASS_NAME, PACKAGE_NAME, FINDING_TYPE_ID, FINDING_ID, TRAIL_ID) VALUES (?,?,?,?,?,?)";
	public static final String MATCH_SELECT_FINDING = "SELECT FINDING_ID FROM SIERRA_MATCH WHERE HASH = ? AND CLASS_NAME = ? AND PACKAGE_NAME = ? AND FINDING_TYPE_ID = ?";
	public static final String FINDING_INSERT = "INSERT INTO FINDING (TRAIL_ID, IMPORTANCE) VALUES (?,?)";
	public static final String TRAIL_INSERT = "INSERT INTO TRAIL (UID) VALUES (?)";
	public static final String ARTIFACT_FINDING_SET = "UPDATE ARTIFACT SET FINDING_ID = ? WHERE ID = ?";

	private final PreparedStatement updateArtifactsWithExistingMatch;
	private final PreparedStatement unassignedArtifacts;
	private final PreparedStatement insertMatch;
	private final PreparedStatement selectMatchFinding;
	private final PreparedStatement insertFinding;
	private final PreparedStatement insertTrail;
	private final PreparedStatement updateArtifact;
	private final Connection conn;

	public FindingGenerator(Connection conn) {
		this.conn = conn;
		try {
			insertMatch = conn.prepareStatement(MATCH_INSERT,
					Statement.RETURN_GENERATED_KEYS);
			selectMatchFinding = conn.prepareStatement(MATCH_SELECT_FINDING);
			insertTrail = conn.prepareStatement(TRAIL_INSERT,
					Statement.RETURN_GENERATED_KEYS);
			insertFinding = conn.prepareStatement(FINDING_INSERT,
					Statement.RETURN_GENERATED_KEYS);
			updateArtifact = conn.prepareStatement(UPDATE_ARTIFACT_FINDING);
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

			unassignedArtifacts.setLong(1, runId);
			unassignedArtifacts.setMaxRows(CHUNK_SIZE);
			List<ArtifactResult> results = new ArrayList<ArtifactResult>(
					CHUNK_SIZE);
			Map<MatchRecord.PK, Long> findingMap = new HashMap<MatchRecord.PK, Long>(
					CHUNK_SIZE);
			do {
				results.clear();
				findingMap.clear();
				updateArtifactsWithExistingMatch.setLong(1, runId);
				updateArtifactsWithExistingMatch.executeUpdate();
				conn.commit();
				ResultSet result = unassignedArtifacts.executeQuery();
				while (result.next()) {
					ArtifactResult art = new ArtifactResult();
					int idx = 1;
					art.id = result.getLong(idx++);
					art.p = Priority.values()[result.getInt(idx++)];
					art.s = Severity.values()[result.getInt(idx++)];
					art.m = new MatchRecord();
					idx = art.m.readPK(result, idx);
					results.add(art);
				}
				result.close();
				for (ArtifactResult art : results) {
					Long findingId = findingMap.get(art.m.getId());
					if (findingId == null) {
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
						findingId = f.getId();
						findingMap.put(art.m.getId(), findingId);
					}
					updateArtifact.setLong(1, findingId);
					updateArtifact.setLong(2, art.id);
					updateArtifact.executeUpdate();
				}
				conn.commit();
			} while (results.size() > 0);

			conn.commit();
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