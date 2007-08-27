package com.surelogic.sierra.jdbc.finding;

import java.sql.Connection;
import java.sql.SQLException;

import com.surelogic.sierra.jdbc.record.AuditRecord;
import com.surelogic.sierra.jdbc.record.FindingRecord;
import com.surelogic.sierra.jdbc.record.LongRelationRecord;
import com.surelogic.sierra.jdbc.record.MatchRecord;
import com.surelogic.sierra.jdbc.record.QualifiedMapper;
import com.surelogic.sierra.jdbc.record.QualifiedUpdateRecordMapper;
import com.surelogic.sierra.jdbc.record.RecordMapper;
import com.surelogic.sierra.jdbc.record.TrailRecord;
import com.surelogic.sierra.jdbc.record.UpdateRecordMapper;

public class QualifiedFindingRecordFactory implements FindingRecordFactory {

	private static final String MATCH_SELECT = "SELECT FINDING_ID, TRAIL_ID FROM SIERRA_MATCH WHERE QUALIFIER_ID = ? AND PROJECT_ID = ? AND HASH = ? AND CLASS_NAME = ? AND PACKAGE_NAME = ? AND FINDING_TYPE_ID = ?";
	private static final String MATCH_INSERT = "INSERT INTO SIERRA_MATCH (QUALIFIER_ID, PROJECT_ID, HASH, CLASS_NAME, PACKAGE_NAME, FINDING_TYPE_ID, FINDING_ID, TRAIL_ID) VALUES (?,?,?,?,?,?,?,?)";
	private static final String MATCH_UPDATE = "UPDATE SIERRA_MATCH SET FINDING_ID = ?, TRAIL_ID = ? WHERE QUALIFIER_ID = ? AND PROJECT_ID = ? AND HASH = ? AND CLASS_NAME = ? AND PACKAGE_NAME = ? AND FINDING_TYPE_ID = ?";
	private static final String FINDING_INSERT = "INSERT INTO FINDING (QUALIFIER_ID, TRAIL_ID, IMPORTANCE) VALUES (?,?,?)";
	private static final String TRAIL_INSERT = "INSERT INTO TRAIL (QUALIFIER_ID, PROJECT_ID,UID) VALUES (?,?,?)";
	private static final String ARTIFACT_FINDING_RELATION_INSERT = "INSERT INTO ARTIFACT_FINDING_RELTN (QUALIFIER_ID,ARTIFACT_ID,FINDING_ID) VALUES (?,?,?)";
	private final UpdateRecordMapper matchMap;
	private final RecordMapper trailMap;
	private final RecordMapper findingMap;
	private final RecordMapper artifactFindingMap;

	private QualifiedFindingRecordFactory(Connection conn, Long qualifier)
			throws SQLException {
		this.matchMap = new QualifiedUpdateRecordMapper(conn, MATCH_INSERT,
				MATCH_SELECT, null, MATCH_UPDATE, qualifier);
		this.trailMap = new QualifiedMapper(conn, TRAIL_INSERT, null, null,
				qualifier);
		this.findingMap = new QualifiedMapper(conn, FINDING_INSERT, null, null,
				qualifier);
		this.artifactFindingMap = new QualifiedMapper(conn,
				ARTIFACT_FINDING_RELATION_INSERT, null, null, qualifier);
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

	public LongRelationRecord newArtifactFinding() {
		return new LongRelationRecord(artifactFindingMap);
	}

	public static QualifiedFindingRecordFactory getInstance(Connection conn,
			Long qualifier) throws SQLException {
		return new QualifiedFindingRecordFactory(conn, qualifier);
	}

}
