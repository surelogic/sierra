package com.surelogic.sierra.jdbc.finding;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class FindingDetail {

	private final String packageName;
	private final String className;
	private final String summary;
	private final String findingType;
	private final String findingTypeDetail;
	private List<CommentDetail> comments;
	private List<ArtifactDetail> artifacts;

	private FindingDetail(Connection conn, Long findingId) throws SQLException {
		Statement st = conn.createStatement();
		try {
			ResultSet set = st
					.executeQuery("SELECT FO.PACKAGE,FO.CLASS,FO.SUMMARY,FT.NAME,FT.INFO"
							+ "   FROM FINDINGS_OVERVIEW FO, LOCATION_MATCH LM, FINDING_TYPE FT"
							+ "   WHERE FO.ID = "
							+ findingId
							+ " AND LM.FINDING_ID = FO.ID AND FT.ID = LM.FINDING_TYPE_ID");
			if (set.next()) {
				int idx = 1;
				packageName = set.getString(idx++);
				className = set.getString(idx++);
				summary = set.getString(idx++);
				findingType = set.getString(idx++);
				findingTypeDetail = set.getString(idx++);
				set = st
						.executeQuery("SELECT U.USER_NAME, A.VALUE, A.DATE_TIME"
								+ "   FROM AUDIT A LEFT OUTER JOIN SIERRA_USER SU ON SU.ID = A.USER_ID"
								+ "   WHERE FINDING_ID = "
								+ findingId
								+ " AND EVENT = 'COMMENT'");
				comments = new ArrayList<CommentDetail>();
				while (set.next()) {
					idx = 1;
					comments.add(new CommentDetail(set.getString(idx++), set
							.getString(idx++), set.getTimestamp(idx++)));
				}
				set = st
						.executeQuery("SELECT T.NAME, A.MESSAGE"
								+ "   FROM ARTIFACT_FINDING_RELTN AFR, ARTIFACT A, ARTIFACT_TYPE ART, TOOL T, LATEST_SCANS LS"
								+ "   WHERE AFR.FINDING_ID = "
								+ findingId
								+ "   AND A.ID = AFR.ARTIFACT_ID AND ART.ID = A.ARTIFACT_TYPE_ID AND T.ID = ART.TOOL_ID"
								+ "   AND LS.SCAN_ID = A.SCAN_ID");
				artifacts = new ArrayList<ArtifactDetail>();
				while (set.next()) {
					idx = 1;
					artifacts.add(new ArtifactDetail(set.getString(idx++), set
							.getString(idx++)));
				}
				artifacts = new ArrayList<ArtifactDetail>();

			} else {
				throw new IllegalArgumentException(findingId
						+ " is not a valid finding id.");
			}
		} finally {
			st.close();
		}
	}

	public String getPackageName() {
		return packageName;
	}

	public String getClassName() {
		return className;
	}

	public String getSummary() {
		return summary;
	}

	public String getFindingType() {
		return findingType;
	}

	public String getFindingTypeDetail() {
		return findingTypeDetail;
	}

	public List<CommentDetail> getComments() {
		return comments;
	}

	public List<ArtifactDetail> getArtifacts() {
		return artifacts;
	}

	public static FindingDetail getDetail(Connection conn, Long findingId)
			throws SQLException {
		return new FindingDetail(conn, findingId);
	}
}
