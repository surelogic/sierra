package com.surelogic.sierra.jdbc.finding;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.surelogic.sierra.jdbc.record.ScanRecord;

class ClientFindingManager extends FindingManager {

	private FindingRecordFactory factory;

	private static final String UNASSIGNED_ARTIFACTS_SELECT = "SELECT A.ID,A.PRIORITY,A.SEVERITY,A.MESSAGE,R.PROJECT_ID,S.HASH,CU.CLASS_NAME,CU.PACKAGE_NAME,ATFTR.FINDING_TYPE_ID"
			+ " FROM (SELECT U.ID FROM ARTIFACT U LEFT OUTER JOIN ARTIFACT_FINDING_RELTN AFR ON AFR.ARTIFACT_ID = U.ID WHERE U.SCAN_ID = ? AND AFR.ARTIFACT_ID IS NULL) AS UNASSIGNED, "
			+ " ARTIFACT A,ARTIFACT_TYPE_FINDING_TYPE_RELTN ATFTR, SCAN R, SOURCE_LOCATION S, COMPILATION_UNIT CU"
			+ " WHERE"
			+ " A.ID = UNASSIGNED.ID AND R.ID = A.SCAN_ID AND S.ID = A.PRIMARY_SOURCE_LOCATION_ID AND CU.ID = S.COMPILATION_UNIT_ID AND ATFTR.ARTIFACT_TYPE_ID = A.ARTIFACT_TYPE_ID";

	private final PreparedStatement unassignedArtifacts;

	ClientFindingManager(Connection conn) throws SQLException {
		super(conn);
		factory = ClientFindingRecordFactory.getInstance(conn);
		unassignedArtifacts = conn
				.prepareStatement(UNASSIGNED_ARTIFACTS_SELECT);
	}

	@Override
	protected FindingRecordFactory getFactory() {
		return factory;
	}

	@Override
	protected ResultSet getUnassignedArtifacts(ScanRecord scan)
			throws SQLException {
		unassignedArtifacts.setLong(1, scan.getId());
		return unassignedArtifacts.executeQuery();
	}

}
