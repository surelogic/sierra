package com.surelogic.sierra.jdbc.finding;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.surelogic.sierra.tool.message.IdentifierType;

public class ArtifactDetail {
	private final String tool;
	private final String message;
	private final SourceDetail primary;
	private final List<SourceDetail> additionalSources;

	ArtifactDetail(ResultSet set, ResultSet sources) throws SQLException {
		int idx = 1;
		this.tool = set.getString(idx++);
		this.message = set.getString(idx++);
		this.primary = new SourceDetail(set, idx);
		additionalSources = new ArrayList<SourceDetail>();
		while (sources.next()) {
			additionalSources.add(new SourceDetail(sources));
		}
	}

	public String getTool() {
		return tool;
	}

	public String getMessage() {
		return message;
	}

	public SourceDetail getPrimarySource() {
	  return primary;
	}
	
	public String getClassName() {
		return primary.getClassName();
	}

	public int getEndLineOfCode() {
		return primary.getEndLineOfCode();
	}

	public String getIdentifier() {
		return primary.getIdentifier();
	}

	public IdentifierType getIdentifierType() {
		return primary.getIdentifierType();
	}

	public int getLineOfCode() {
		return primary.getLineOfCode();
	}

	public String getPackageName() {
		return primary.getPackageName();
	}

	public List<SourceDetail> getAdditionalSources() {
		return additionalSources;
	}

	public static ArtifactDetail getArtifact(Connection conn, long artifactId)
			throws SQLException {
		Statement artSt = conn.createStatement();
		try {
			Statement sourceSt = conn.createStatement();
			try {
				ResultSet artSet = artSt
						.executeQuery("SELECT T.NAME, A.MESSAGE, CU.PACKAGE_NAME,SL.CLASS_NAME,SL.LINE_OF_CODE,SL.END_LINE_OF_CODE,SL.LOCATION_TYPE,SL.IDENTIFIER"
								+ "   FROM ARTIFACT A, ARTIFACT_TYPE ART, TOOL T, SOURCE_LOCATION SL, COMPILATION_UNIT CU"
								+ "   WHERE A.ID = "
								+ artifactId
								+ " AND ART.ID = A.ARTIFACT_TYPE_ID AND T.ID = ART.TOOL_ID"
								+ " AND SL.ID = A.PRIMARY_SOURCE_LOCATION_ID AND CU.ID = SL.COMPILATION_UNIT_ID");
				artSet.next();
				ResultSet sourceSet = sourceSt
						.executeQuery("SELECT CU.PACKAGE_NAME,SL.CLASS_NAME,SL.LINE_OF_CODE,SL.END_LINE_OF_CODE,SL.LOCATION_TYPE,SL.IDENTIFIER"
								+ "   FROM ARTIFACT_SOURCE_LOCATION_RELTN ASLR, SOURCE_LOCATION SL, COMPILATION_UNIT CU"
								+ "   WHERE ASLR.ARTIFACT_ID = "
								+ artifactId
								+ " AND SL.ID = ASLR.SOURCE_LOCATION_ID AND CU.ID = SL.COMPILATION_UNIT_ID");
				return new ArtifactDetail(artSet, sourceSet);
			} finally {
				sourceSt.close();
			}
		} finally {
			artSt.close();
		}
	}

}
