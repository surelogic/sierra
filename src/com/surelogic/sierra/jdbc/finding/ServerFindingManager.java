package com.surelogic.sierra.jdbc.finding;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.surelogic.sierra.jdbc.DBType;
import com.surelogic.sierra.jdbc.JDBCUtils;
import com.surelogic.sierra.jdbc.project.ProjectRecordFactory;
import com.surelogic.sierra.jdbc.qualifier.QualifierRecordFactory;
import com.surelogic.sierra.jdbc.record.AuditRecord;
import com.surelogic.sierra.jdbc.record.FindingRecord;
import com.surelogic.sierra.jdbc.record.ProjectRecord;
import com.surelogic.sierra.jdbc.record.QualifierRecord;
import com.surelogic.sierra.jdbc.record.ScanRecord;
import com.surelogic.sierra.jdbc.scan.ScanRecordFactory;
import com.surelogic.sierra.tool.message.Audit;
import com.surelogic.sierra.tool.message.AuditTrail;
import com.surelogic.sierra.tool.message.Merge;

public final class ServerFindingManager extends FindingManager {

	// Queries/Views for Sierra Server
	//	
	// These are all for the MOST recent scan
	// 1. Number of total findings
	// 2. Number of total findings by importance
	// 3. Number of useful findings versus irrelevant (this is a variation of
	// #2).
	// 4. Number of findings by mneumonic type
	// 5. Number of new findings since the last scan
	// 6. Number of closed findings since the last scan
	//	
	//	
	// These are needed for a "time series"
	// 1. Number of findings for the last 'n' # of scans (by default i think n
	// should be a number that they can configure, but for now, let's just pull
	// ALL scans).
	// 2. Number of findings by importance
	// 3. number of new findings
	// 4. Number of closed findings

	private final PreparedStatement populateSeriesOverview;
	private final PreparedStatement populateTempIds;
	private final PreparedStatement deleteTempIds;

	private ServerFindingManager(Connection conn) throws SQLException {
		super(conn);
		Statement st = conn.createStatement();
		String tempTableName;
		try {
			if (DBType.ORACLE == JDBCUtils.getDb(conn)) {
				try {
					st
							.execute("CREATE GLOBAL TEMPORARY TABLE TEMP_FINDING_IDS (ID NUMBER NOT NULL) ON COMMIT DELETE ROWS");
				} catch (SQLException e) {
					// Do nothing, the table is probably already there.
				}
				tempTableName = "TEMP_FINDING_IDS";
				populateTempIds = conn
						.prepareStatement("INSERT INTO TEMP_FINDING_IDS"
								+ "  SELECT SO.FINDING_ID FROM SCAN_OVERVIEW SO WHERE SO.SCAN_ID = ?"
								+ "  MINUS "
								+ "  SELECT TSO.FINDING_ID FROM TIME_SERIES_OVERVIEW TSO WHERE QUALIFIER_ID = ? AND PROJECT_ID = ?");
			} else {
				try {
					st
							.execute("DECLARE GLOBAL TEMPORARY TABLE TEMP_FINDING_IDS (ID BIGINT NOT NULL) NOT LOGGED");
				} catch (SQLException e) {
					// Do nothing, the table is probably already there.
				}
				tempTableName = "SESSION.TEMP_FINDING_IDS";
				populateTempIds = conn
						.prepareStatement("INSERT INTO SESSION.TEMP_FINDING_IDS "
								+ "  SELECT SO.FINDING_ID FROM SCAN_OVERVIEW SO WHERE SO.SCAN_ID = ?"
								+ "  EXCEPT"
								+ "  SELECT TSO.FINDING_ID FROM TIME_SERIES_OVERVIEW TSO WHERE QUALIFIER_ID = ? AND PROJECT_ID = ?");
			}
		} finally {
			st.close();
		}
		deleteTempIds = conn.prepareStatement("DELETE FROM " + tempTableName);
		populateSeriesOverview = conn
				.prepareStatement("INSERT INTO TIME_SERIES_OVERVIEW"
						+ " SELECT ?, F.ID,F.PROJECT_ID,"
						+ "        CASE WHEN F.IS_READ = 'Y' THEN 'Yes' ELSE 'No' END,"
						+ "        F.LAST_CHANGED,"
						+ "        CASE"
						+ "             WHEN F.IMPORTANCE=0 THEN 'Irrelevant'"
						+ " 	        WHEN F.IMPORTANCE=1 THEN 'Low'"
						+ "             WHEN F.IMPORTANCE=2 THEN 'Medium'"
						+ "             WHEN F.IMPORTANCE=3 THEN 'High'"
						+ "             WHEN F.IMPORTANCE=4 THEN 'Critical'"
						+ "        END,"
						+ "        CASE WHEN COUNT.COUNT IS NULL THEN 0 ELSE COUNT.COUNT END,"
						+ "        ?,"
						+ "        LM.PACKAGE_NAME,"
						+ "        LM.CLASS_NAME,"
						+ "        FT.NAME,"
						+ "        F.SUMMARY"
						+ " FROM "
						+ tempTableName
						+ " TF"
						+ "    INNER JOIN FINDING F ON F.ID = TF.ID"
						+ "    LEFT OUTER JOIN ("
						+ "       SELECT"
						+ "          A.FINDING_ID \"ID\", COUNT(*) \"COUNT\""
						+ "       FROM SIERRA_AUDIT A"
						+ "       WHERE A.EVENT='COMMENT'"
						+ "       GROUP BY A.FINDING_ID) COUNT ON COUNT.ID = F.ID"
						+ "    INNER JOIN LOCATION_MATCH LM ON LM.FINDING_ID = F.ID"
						+ "    INNER JOIN FINDING_TYPE FT ON FT.ID = LM.FINDING_TYPE_ID");

	}

	public void generateOverview(String projectName, String scanUid,
			Set<String> qualifiers) throws SQLException {
		ProjectRecord projectRec = ProjectRecordFactory.getInstance(conn)
				.newProject();
		projectRec.setName(projectName);
		if (projectRec.select()) {
			ScanRecord scan = ScanRecordFactory.getInstance(conn).newScan();
			scan.setUid(scanUid);
			if (scan.select()) {
				List<QualifierRecord> qualifierRecs = new ArrayList<QualifierRecord>(
						qualifiers.size());
				QualifierRecordFactory qFac = QualifierRecordFactory
						.getInstance(conn);
				for (String q : qualifiers) {
					QualifierRecord qRec = qFac.newQualifier();
					qRec.setName(q);
					if (qRec.select()) {
						qualifierRecs.add(qRec);
					} else {
						throw new IllegalArgumentException(
								"No qualifier exists with name " + q);
					}
				}
				log.info("Populating scan overview for scan with uid "
						+ scan.getUid() + ".");
				populateScanOverview(scan.getId());
				for (QualifierRecord q : qualifierRecs) {
					int idx = 1;
					// Add the new findings to the time series overview
					populateTempIds.setLong(idx++, scan.getId());
					populateTempIds.setLong(idx++, q.getId());
					populateTempIds.setLong(idx++, projectRec.getId());
					populateTempIds.execute();
					idx = 1;
					// Look up previous scan in qualifier
					populateSeriesOverview.setLong(idx++, q.getId());
					populateSeriesOverview.setString(idx++, projectRec
							.getName());
					populateSeriesOverview.execute();
					deleteTempIds.execute();
				}
				log.info("Overview for qualifiers " + qualifiers
						+ " was generated with respect to scan with uid "
						+ scan.getUid() + ".");
			} else {
				throw new IllegalArgumentException("No scan exists with uid "
						+ scanUid);
			}
		} else {
			throw new IllegalArgumentException("No project exists with name"
					+ projectName);
		}
	}

	/**
	 * Commit the given audit trails.
	 * 
	 * @param userId
	 * @param revision
	 * @param trails
	 * @return the in-order list of uids that the audits were applied to.
	 * @throws SQLException
	 */
	public List<String> commitAuditTrails(Long userId, Long revision,
			List<AuditTrail> trails) throws SQLException {
		List<String> uids = new ArrayList<String>(trails.size());
		for (AuditTrail trail : trails) {
			FindingRecord findingRecord = factory.newFinding();
			String finding = trail.getFinding();
			findingRecord.setUid(finding);
			if (findingRecord.select()) {
				Long findingId = findingRecord.getId();
				Long obsoletedById = findingRecord.getObsoletedById();
				while ((obsoletedById = findingRecord.getObsoletedById()) != null) {
					findingRecord.setId(obsoletedById);
					findingRecord.select();
					findingId = findingRecord.getId();
				}
				for (Audit audit : trail.getAudits()) {
					AuditRecord auditRecord = factory.newAudit();
					auditRecord.setEvent(audit.getEvent());
					auditRecord.setFindingId(findingId);
					auditRecord.setRevision(revision);
					auditRecord.setTimestamp(audit.getTimestamp());
					auditRecord.setUserId(userId);
					auditRecord.setValue(audit.getValue());
					auditRecord.insert();
				}
				uids.add(findingRecord.getUid());
			}
		}
		return uids;
	}

	public void mergeAuditTrails(String project, Long revision, List<Merge> merges)
			throws SQLException {
		ProjectRecord projectRecord = ProjectRecordFactory.getInstance(conn)
				.newProject();
		projectRecord.setName(project);
		if (!projectRecord.select()) {
			projectRecord.insert();
		}
		
	}

	public static ServerFindingManager getInstance(Connection conn)
			throws SQLException {
		return new ServerFindingManager(conn);
	}

}
