package com.surelogic.sierra.jdbc.finding;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.surelogic.sierra.tool.message.Importance;

public class FindingDetail {

	private final FindingOverview overview;
	private final String findingTypeDetail;
	private List<CommentDetail> comments;
	private List<ArtifactDetail> artifacts;

	private FindingDetail(Connection conn, Long findingId) throws SQLException {
		Statement st = conn.createStatement();
		try {
			ResultSet set = st
					.executeQuery("SELECT FINDING_ID,EXAMINED,LAST_CHANGED,IMPORTANCE,STATUS,LINE_OF_CODE,ARTIFACT_COUNT,COMMENT_COUNT,PROJECT,PACKAGE,CLASS,FINDING_TYPE,TOOL,SUMMARY"
							+ " FROM FINDINGS_OVERVIEW WHERE FINDING_ID = "
							+ findingId);
			set.next();
			overview = new FindingOverview(set);
			set = st
					.executeQuery("SELECT FT.INFO"
							+ "   FROM FINDINGS_OVERVIEW FO, LOCATION_MATCH LM, FINDING_TYPE FT"
							+ "   WHERE FO.FINDING_ID = "
							+ findingId
							+ " AND LM.FINDING_ID = FO.FINDING_ID AND FT.ID = LM.FINDING_TYPE_ID");
			if (set.next()) {
				int idx = 1;
				findingTypeDetail = set.getString(idx++);
				set = st
						.executeQuery("SELECT SU.USER_NAME, A.VALUE, A.DATE_TIME"
								+ "   FROM SIERRA_AUDIT A LEFT OUTER JOIN SIERRA_USER SU ON SU.ID = A.USER_ID"
								+ "   WHERE FINDING_ID = "
								+ findingId
								+ " AND EVENT = 'COMMENT'");
				comments = new ArrayList<CommentDetail>();
				while (set.next()) {
					idx = 1;
					comments.add(new CommentDetail(set.getString(idx++), set
							.getString(idx++), set.getTimestamp(idx++)));
				}
				artifacts = new ArrayList<ArtifactDetail>();
				set = st
						.executeQuery("SELECT ARTIFACT_ID FROM ARTIFACT_FINDING_RELTN WHERE FINDING_ID = "
								+ findingId);
				while (set.next()) {
					long artifactId = set.getLong(1);
					Statement artSt = conn.createStatement();
					try {
						ResultSet artSet = artSt
								.executeQuery("SELECT CU.PACKAGE_NAME,CU.CLASS_NAME,SL.LINE_OF_CODE,SL.END_LINE_OF_CODE,SL.LOCATION_TYPE,SL.IDENTIFIER"
										+ "   FROM ARTIFACT A, SOURCE_LOCATION SL, COMPILATION_UNIT CU"
										+ "   WHERE A.ID = "
										+ artifactId
										+ " AND SL.ID = A.PRIMARY_SOURCE_LOCATION_ID AND CU.ID = SL.COMPILATION_UNIT_ID");
						artSet.next();
						SourceDetail primary = new SourceDetail(artSet);
						artSet = artSt
								.executeQuery("SELECT CU.PACKAGE_NAME,CU.CLASS_NAME,SL.LINE_OF_CODE,SL.END_LINE_OF_CODE,SL.LOCATION_TYPE,SL.IDENTIFIER"
										+ "   FROM ARTIFACT_SOURCE_LOCATION_RELTN A, SOURCE_LOCATION SL, COMPILATION_UNIT CU"
										+ "   WHERE A.ARTIFACT_ID = "
										+ artifactId
										+ " AND SL.ID = A.SOURCE_LOCATION_ID AND CU.ID = SL.COMPILATION_UNIT_ID");
						List<SourceDetail> additionalSources = new LinkedList<SourceDetail>();
						while (artSet.next()) {
							additionalSources.add(new SourceDetail(artSet));
						}
						artSet = artSt
								.executeQuery("SELECT T.NAME, A.MESSAGE"
										+ "   FROM ARTIFACT A, ARTIFACT_TYPE ART, TOOL T"
										+ "   WHERE A.ID = " + artifactId);
						artSet.next();
						artifacts.add(new ArtifactDetail(artSet, primary,
								additionalSources));
					} finally {
						artSt.close();
					}

				}

			} else {
				throw new IllegalArgumentException(findingId
						+ " is not a valid finding id.");
			}
		} finally {
			st.close();
		}
	}

	public String getClassName() {
		return overview.getClassName();
	}

	public long getFindingId() {
		return overview.getFindingId();
	}

	public String getFindingType() {
		return overview.getFindingType();
	}

	public Importance getImportance() {
		return overview.getImportance();
	}

	public Date getLastChanged() {
		return overview.getLastChanged();
	}

	public int getLineOfCode() {
		return overview.getLineOfCode();
	}

	public Integer[] getLinesOfCode() {
		Set<Integer> lineSet = new TreeSet<Integer>();
		for (ArtifactDetail a : artifacts) {
			lineSet.add(a.getLineOfCode());
		}
		Integer[] lines = lineSet.toArray(new Integer[lineSet.size()]);	
		Arrays.sort(lines);
		return lines;

	}

	public int getNumberOfArtifacts() {
		return overview.getNumberOfArtifacts();
	}

	public int getNumberOfComments() {
		return overview.getNumberOfComments();
	}

	public String getPackageName() {
		return overview.getPackageName();
	}

	public String getProject() {
		return overview.getProject();
	}

	public FindingStatus getStatus() {
		return overview.getStatus();
	}

	public String getSummary() {
		return overview.getSummary();
	}

	public String getTool() {
		return overview.getTool();
	}

	public boolean isExamined() {
		return overview.isExamined();
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
