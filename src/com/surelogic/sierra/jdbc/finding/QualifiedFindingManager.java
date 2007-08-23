package com.surelogic.sierra.jdbc.finding;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.surelogic.sierra.jdbc.qualifier.QualifierRecordFactory;
import com.surelogic.sierra.jdbc.record.QualifierRecord;
import com.surelogic.sierra.jdbc.record.RunRecord;

public class QualifiedFindingManager extends FindingManager {

	private static final String UNASSIGNED_ARTIFACTS_SELECT = "SELECT A.ID,A.PRIORITY,A.SEVERITY,R.PROJECT_ID,S.HASH,CU.CLASS_NAME,CU.PACKAGE_NAME,A.FINDING_TYPE_ID"
			+ " FROM (SELECT U.ID FROM ARTIFACT U LEFT OUTER JOIN ARTIFACT_FINDING_RELTN AFR ON AFR.ARTIFACT_ID = U.ID AND AFR.QUALIFIER_ID = ? WHERE U.RUN_ID = ? AND AFR.ARTIFACT_ID IS NULL) AS UNASSIGNED, "
			+ " ARTIFACT A, RUN R, SOURCE_LOCATION S, COMPILATION_UNIT CU"
			+ " WHERE"
			+ " A.ID = UNASSIGNED.ID AND R.ID = A.RUN_ID AND S.ID = A.PRIMARY_SOURCE_LOCATION_ID AND CU.ID = S.COMPILATION_UNIT_ID";

	private final PreparedStatement unassignedArtifacts;

	private final Long qualifierId;
	private final FindingRecordFactory factory;

	QualifiedFindingManager(Connection conn, String qualifier)
			throws SQLException {
		super(conn);
		unassignedArtifacts = conn
				.prepareStatement(UNASSIGNED_ARTIFACTS_SELECT);
		QualifierRecord rec = QualifierRecordFactory.getInstance(conn)
				.newQualifier();
		rec.setName(qualifier);
		if (!rec.select()) {
			throw new IllegalArgumentException("Qualifier with name "
					+ qualifier + " is not a valid qualifier");
		}
		qualifierId = rec.getId();
		this.factory = QualifiedFindingRecordFactory.getInstance(conn,
				qualifierId);
	}

	@Override
	protected FindingRecordFactory getFactory() {
		return factory;
	}

	@Override
	protected ResultSet getUnassignedArtifacts(RunRecord run)
			throws SQLException {
		unassignedArtifacts.setLong(1, qualifierId);
		unassignedArtifacts.setLong(2, run.getId());
		return unassignedArtifacts.executeQuery();
	}

}
