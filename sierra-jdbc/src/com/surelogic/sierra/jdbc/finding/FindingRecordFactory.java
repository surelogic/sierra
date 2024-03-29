package com.surelogic.sierra.jdbc.finding;

import java.sql.Connection;
import java.sql.SQLException;

import com.surelogic.sierra.jdbc.record.AuditRecord;
import com.surelogic.sierra.jdbc.record.BaseMapper;
import com.surelogic.sierra.jdbc.record.FindingRecord;
import com.surelogic.sierra.jdbc.record.LongRelationRecord;
import com.surelogic.sierra.jdbc.record.MatchRecord;
import com.surelogic.sierra.jdbc.record.RecordMapper;
import com.surelogic.sierra.jdbc.record.UpdateBaseMapper;
import com.surelogic.sierra.jdbc.record.UpdateRecordMapper;

public final class FindingRecordFactory {

	private static final String MATCH_SELECT = "SELECT FINDING_ID,REVISION FROM LOCATION_MATCH WHERE PROJECT_ID = ? AND HASH = ? AND CLASS_NAME = ? AND PACKAGE_NAME = ? AND FINDING_TYPE_ID = ?";
	private static final String MATCH_INSERT = "INSERT INTO LOCATION_MATCH (PROJECT_ID,HASH,CLASS_NAME,PACKAGE_NAME,FINDING_TYPE_ID,FINDING_ID,REVISION) VALUES (?,?,?,?,?,?,?)";
	private static final String MATCH_UPDATE = "UPDATE LOCATION_MATCH SET FINDING_ID = ?, REVISION = ? WHERE PROJECT_ID = ? AND HASH = ? AND CLASS_NAME = ? AND PACKAGE_NAME = ? AND FINDING_TYPE_ID = ?";
	private static final String FINDING_INSERT = "INSERT INTO FINDING (PROJECT_ID,UUID,IMPORTANCE,SUMMARY,OBSOLETED_BY_ID,OBSOLETED_BY_REVISION) VALUES (?,?,?,?,?,?)";
	private static final String FINDING_SELECT = "SELECT ID,PROJECT_ID,IMPORTANCE,SUMMARY,IS_READ,OBSOLETED_BY_ID,OBSOLETED_BY_REVISION FROM FINDING WHERE UUID = ?";
	private static final String FINDING_DELETE = "DELETE FROM FINDING WHERE ID = ?";
	private static final String FINDING_UPDATE = "UPDATE FINDING SET UUID = ?, IMPORTANCE = ?, SUMMARY = ? WHERE ID = ?";
	private static final String INSERT_ARTIFACT_FINDING_RELATION = "INSERT INTO ARTIFACT_FINDING_RELTN (ARTIFACT_ID,FINDING_ID) VALUES (?,?)";
	private static final String AUDIT_INSERT = "INSERT INTO SIERRA_AUDIT (UUID,FINDING_ID,PROJECT_ID,EVENT,USER_ID,DATE_TIME,VALUE,REVISION) VALUES (?,?,(SELECT PROJECT_ID FROM FINDING WHERE ID = ?),?,?,?,?,?)";
	private static final String AUDIT_SELECT = "SELECT FINDING_ID,EVENT,USER_ID,DATE_TIME,VALUE,REVISION FROM SIERRA_AUDIT WHERE UUID = ?";
	private final UpdateRecordMapper matchMap;
	private final UpdateRecordMapper findingMap;
	private final RecordMapper artifactFindingMap;
	private final RecordMapper auditMap;

	private FindingRecordFactory(final Connection conn) throws SQLException {
		this.matchMap = new UpdateBaseMapper(conn, MATCH_INSERT, MATCH_SELECT,
				null, MATCH_UPDATE, false);
		this.findingMap = new UpdateBaseMapper(conn, FINDING_INSERT,
				FINDING_SELECT, FINDING_DELETE, FINDING_UPDATE);
		this.artifactFindingMap = new BaseMapper(conn,
				INSERT_ARTIFACT_FINDING_RELATION, null, null, false);
		this.auditMap = new BaseMapper(conn, AUDIT_INSERT, AUDIT_SELECT, null);
	}

	public AuditRecord newAudit() {
		return new AuditRecord(auditMap);
	}

	public FindingRecord newFinding() {
		return new FindingRecord(findingMap);
	}

	public MatchRecord newMatch() {
		return new MatchRecord(matchMap);
	}

	public static FindingRecordFactory getInstance(final Connection conn)
			throws SQLException {
		return new FindingRecordFactory(conn);
	}

	public LongRelationRecord newArtifactFinding() {
		return new LongRelationRecord(artifactFindingMap);
	}
}
