package com.surelogic.sierra.jdbc.finding;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class SynchDetail {

	private final Date time;
	private final String project;
	private final List<SynchedFindingDetail> findings;

	private SynchDetail(Connection conn, String project, Date time)
			throws SQLException {
		PreparedStatement synchSt = conn
				.prepareStatement("SELECT COMMIT_REVISION,PRIOR_REVISION FROM SYNCH_DETAIL WHERE PROJECT_ID = (SELECT ID FROM PROJECT WHERE NAME = ?) AND DATE_TIME = ?");
		try {
			synchSt.setString(1, project);
			synchSt.setTimestamp(2, new Timestamp(time.getTime()));
			ResultSet set = synchSt.executeQuery();
			if (set.next()) {
				this.project = project;
				this.time = time;
				this.findings = new ArrayList<SynchedFindingDetail>();
				long commitRevision = set.getLong(1);
				long priorRevision = set.getLong(2);
				PreparedStatement auditSt = conn
						.prepareStatement("SELECT A.FINDING_ID,U.USER_NAME,A.EVENT,A.VALUE,A.DATE_TIME FROM SIERRA_AUDIT A, SIERRA_USER U WHERE A.REVISION = ? AND U.ID = A.USER_ID ORDER BY A.FINDING_ID");
				try {
					auditSt.setLong(1, commitRevision);
					Map<Long, List<AuditDetail>> commitMap = populateAuditMap(auditSt
							.executeQuery());
					auditSt.setLong(1, priorRevision);
					Map<Long, List<AuditDetail>> auditMap = populateAuditMap(auditSt
							.executeQuery());
					HashSet<Long> findingIds = new HashSet<Long>(auditMap
							.keySet());
					findingIds.addAll(commitMap.keySet());
					PreparedStatement overviewSt = conn
							.prepareStatement("SELECT FINDING_ID,AUDITED,LAST_CHANGED,IMPORTANCE,STATUS,LINE_OF_CODE,ARTIFACT_COUNT,AUDIT_COUNT,PROJECT,PACKAGE,CLASS,CU,FINDING_TYPE,CATEGORY,TOOL,SUMMARY"
									+ " FROM FINDINGS_OVERVIEW WHERE FINDING_ID = ?");
					try {
						for (long findingId : findingIds) {
							overviewSt.setLong(1, findingId);
							ResultSet overviewSet = overviewSt.executeQuery();
							if (overviewSet.next()) {
								FindingOverview overview = new FindingOverview(
										overviewSet);
								findings.add(new SynchedFindingDetail(overview,
										auditMap.get(findingId), commitMap
												.get(findingId)));
							}
						}
					} finally {
						overviewSt.close();
					}
				} finally {
					auditSt.close();
				}
			} else {
				throw new IllegalArgumentException("No scan occurred at "
						+ time + " for project " + project);
			}
		} finally {
			synchSt.close();
		}

	}

	/*
	 * Fill out the map with a list of audit details for each finding id. The
	 * set is ordered by finding id.
	 */
	private static Map<Long, List<AuditDetail>> populateAuditMap(ResultSet set)
			throws SQLException {
		Map<Long, List<AuditDetail>> auditMap = new HashMap<Long, List<AuditDetail>>();
		long findingId = -1;
		List<AuditDetail> audits = null;
		while (set.next()) {
			long nextFindingId = set.getLong(1);
			AuditDetail detail = new AuditDetail(set, 2);
			if (!(nextFindingId == findingId)) {
				findingId = nextFindingId;
				audits = new ArrayList<AuditDetail>();
				audits.add(detail);
				auditMap.put(findingId, audits);
			} else {
				audits.add(detail);
			}
		}
		return auditMap;
	}

	public static SynchDetail getDetail(Connection conn, String project,
			Date time) throws SQLException {
		return new SynchDetail(conn, project, time);
	}

	public Date getTime() {
		return time;
	}

	public String getProject() {
		return project;
	}

	public List<SynchedFindingDetail> getFindings() {
		return findings;
	}

}
