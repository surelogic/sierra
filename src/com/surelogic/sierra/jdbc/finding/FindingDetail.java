package com.surelogic.sierra.jdbc.finding;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.surelogic.sierra.tool.message.Importance;

public class FindingDetail {

	private final FindingOverview overview;
	private final String findingTypeDetail;
	private List<AuditDetail> audits;
	private List<ArtifactDetail> artifacts;

	private FindingDetail(Connection conn, Long findingId) throws SQLException {
		Statement st = conn.createStatement();
		try {
			ResultSet set = st
					.executeQuery("SELECT FT.INFO,FO.FINDING_ID,FO.AUDITED,FO.LAST_CHANGED,FO.IMPORTANCE,FO.STATUS,FO.LINE_OF_CODE,FO.ARTIFACT_COUNT,FO.AUDIT_COUNT,FO.PROJECT,FO.PACKAGE,FO.CLASS,FO.CU,FO.FINDING_TYPE,FO.CATEGORY,FO.TOOL,FO.SUMMARY"
							+ "   FROM FINDINGS_OVERVIEW FO, LOCATION_MATCH LM, FINDING_TYPE FT"
							+ "   WHERE FO.FINDING_ID = "
							+ findingId
							+ " AND LM.FINDING_ID = FO.FINDING_ID AND FT.ID = LM.FINDING_TYPE_ID");
			if (set.next()) {
				int idx = 1;
				findingTypeDetail = set.getString(idx++);
				overview = new FindingOverview(set, idx);
				set = st
						.executeQuery("SELECT A.FINDING_ID, SU.USER_NAME, A.EVENT, A.VALUE, A.DATE_TIME"
								+ "   FROM SIERRA_AUDIT A LEFT OUTER JOIN SIERRA_USER SU ON SU.ID = A.USER_ID"
								+ "   WHERE FINDING_ID = "
								+ findingId
								+ " ORDER BY A.DATE_TIME");
				audits = new ArrayList<AuditDetail>();
				while (set.next()) {
					idx = 1;
					audits.add(new AuditDetail(set));
				}
				artifacts = new ArrayList<ArtifactDetail>();
				set = st
						.executeQuery("SELECT ARTIFACT_ID FROM ARTIFACT A, ARTIFACT_FINDING_RELTN AFR WHERE AFR.FINDING_ID = "
								+ findingId
								+ " AND A.ID = AFR.ARTIFACT_ID AND A.SCAN_ID IN (SELECT SCAN_ID FROM LATEST_SCANS)");
				while (set.next()) {
					long artifactId = set.getLong(1);
					Statement artSt = conn.createStatement();
					try {
						ResultSet artSet = artSt
								.executeQuery("SELECT T.NAME, A.MESSAGE, CU.PACKAGE_NAME,SL.CLASS_NAME,SL.LINE_OF_CODE,SL.END_LINE_OF_CODE,SL.LOCATION_TYPE,SL.IDENTIFIER"
										+ "   FROM ARTIFACT A, ARTIFACT_TYPE ART, TOOL T, SOURCE_LOCATION SL, COMPILATION_UNIT CU"
										+ "   WHERE A.ID = "
										+ artifactId
										+ " AND ART.ID = A.ARTIFACT_TYPE_ID AND T.ID = ART.TOOL_ID"
										+ " AND SL.ID = A.PRIMARY_SOURCE_LOCATION_ID AND CU.ID = SL.COMPILATION_UNIT_ID");
						artSet.next();
						artifacts.add(new ArtifactDetail(artSet));
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

	public String getCategory() {
		return overview.getCategory();
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

	public int[] getLinesOfCode() {
		Set<Integer> lineSet = new TreeSet<Integer>();
		for (ArtifactDetail a : artifacts) {
			lineSet.add(a.getLineOfCode());
		}
		Iterator<Integer> iter = lineSet.iterator();
		int lines[] = new int[lineSet.size()];
		for (int i = 0; i < lines.length; i++) {
			lines[i] = iter.next();
		}
		Arrays.sort(lines);
		return lines;
	}

	public int getNumberOfArtifacts() {
		return overview.getNumberOfArtifacts();
	}

	public int getNumberOfAudits() {
		return overview.getNumberOfAudits();
	}

	public String getPackageName() {
		return overview.getPackageName();
	}

	public String getProjectName() {
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

	public List<AuditDetail> getAudits() {
		return audits;
	}

	public List<ArtifactDetail> getArtifacts() {
		return artifacts;
	}

	public static FindingDetail getDetail(Connection conn, Long findingId)
			throws SQLException {
		return new FindingDetail(conn, findingId);
	}
}
