package com.surelogic.sierra.jdbc.finding;

import java.sql.Connection;
import java.sql.SQLException;

import com.surelogic.sierra.jdbc.record.AuditRecord;
import com.surelogic.sierra.jdbc.record.BaseMapper;
import com.surelogic.sierra.jdbc.record.FindingRecord;
import com.surelogic.sierra.jdbc.record.LongRelationRecord;
import com.surelogic.sierra.jdbc.record.MatchRecord;
import com.surelogic.sierra.jdbc.record.RecordMapper;
import com.surelogic.sierra.jdbc.record.TrailRecord;
import com.surelogic.sierra.jdbc.record.UpdateBaseMapper;
import com.surelogic.sierra.jdbc.record.UpdateRecordMapper;

public class ClientFindingRecordFactory implements FindingRecordFactory {

	private static final String MATCH_SELECT = "SELECT FINDING_ID, TRAIL_ID FROM SIERRA_MATCH WHERE PROJECT_ID = ? AND HASH = ? AND CLASS_NAME = ? AND PACKAGE_NAME = ? AND FINDING_TYPE_ID = ?";
	private static final String MATCH_INSERT = "INSERT INTO SIERRA_MATCH (PROJECT_ID, HASH, CLASS_NAME, PACKAGE_NAME, FINDING_TYPE_ID, FINDING_ID, TRAIL_ID) VALUES (?,?,?,?,?,?,?)";
	private static final String MATCH_UPDATE = "UPDATE SIERRA_MATCH SET FINDING_ID = ?, TRAIL_ID = ? WHERE PROJECT_ID = ? AND HASH = ? AND CLASS_NAME = ? AND PACKAGE_NAME = ? AND FINDING_TYPE_ID = ?";
	private static final String FINDING_INSERT = "INSERT INTO FINDING (TRAIL_ID, IMPORTANCE) VALUES (?,?)";
	private static final String TRAIL_INSERT = "INSERT INTO TRAIL (PROJECT_ID,UID) VALUES (?,?)";
	private static final String INSERT_ARTIFACT_FINDING_RELATION = "INSERT INTO ARTIFACT_FINDING_RELTN (ARTIFACT_ID,FINDING_ID) VALUES (?,?)";
	private final UpdateRecordMapper matchMap;
	private final RecordMapper trailMap;
	private final RecordMapper findingMap;
	private final RecordMapper artifactFindingMap;

	private ClientFindingRecordFactory(Connection conn) throws SQLException {
		this.matchMap = new UpdateBaseMapper(conn, MATCH_INSERT, MATCH_SELECT,
				null, MATCH_UPDATE);
		this.trailMap = new BaseMapper(conn, TRAIL_INSERT, null, null);
		this.findingMap = new BaseMapper(conn, FINDING_INSERT, null, null);
		this.artifactFindingMap = new BaseMapper(conn,
				INSERT_ARTIFACT_FINDING_RELATION, null, null);
	}

	public AuditRecord newAudit() {
		// TODO
		return null;
	}

	public FindingRecord newFinding() {
		return new FindingRecord(findingMap);
	}

	public MatchRecord newMatch() {
		return new MatchRecord(matchMap);
	}

	public TrailRecord newTrail() {
		return new TrailRecord(trailMap);
	}

	public static ClientFindingRecordFactory getInstance(Connection conn)
			throws SQLException {
		return new ClientFindingRecordFactory(conn);
	}

	@Override
	public LongRelationRecord newArtifactFinding() {
		return new LongRelationRecord(artifactFindingMap);
	}
}
