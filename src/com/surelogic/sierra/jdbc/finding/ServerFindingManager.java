package com.surelogic.sierra.jdbc.finding;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.surelogic.sierra.jdbc.project.ProjectRecordFactory;
import com.surelogic.sierra.jdbc.qualifier.QualifierRecordFactory;
import com.surelogic.sierra.jdbc.record.ProjectRecord;
import com.surelogic.sierra.jdbc.record.QualifierRecord;
import com.surelogic.sierra.jdbc.record.ScanRecord;
import com.surelogic.sierra.jdbc.scan.ScanRecordFactory;

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

	private final PreparedStatement deleteSeriesOverview;
	private final PreparedStatement populateSeriesOverview;
	private final PreparedStatement populateTempIds;

	private ServerFindingManager(Connection conn) throws SQLException {
		super(conn);
		try {
			conn
					.createStatement()
					.execute(
							"DECLARE GLOBAL TEMPORARY TABLE TEMP_FINDING_IDS (ID BIGINT NOT NULL) NOT LOGGED");
		} catch (SQLException e) {
			// Do nothing, the table is probably already there.
		}
		deleteSeriesOverview = conn
				.prepareStatement("DELETE FROM TIME_SERIES_OVERVIEW WHERE PROJECT_ID = ? AND QUALIFIER_ID = ?");
		populateTempIds = conn
				.prepareStatement("INSERT INTO SESSION.TEMP_FINDING_IDS "
						+ "SELECT SO.FINDING_ID FROM SCAN_OVERVIEW SO WHERE SO.SCAN_ID = ?");
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
						+ "        CASE"
						+ "             WHEN SO.FINDING_ID IS NULL THEN 'Fixed'"
						+ "             WHEN PREV.FINDING_ID IS NULL THEN 'New'"
						+ "             ELSE 'Unchanged'"
						+ "        END,"
						+ "        SO.LINE_OF_CODE,"
						+ "        CASE WHEN SO.ARTIFACT_COUNT IS NULL THEN 0 ELSE SO.ARTIFACT_COUNT END,"
						+ "        CASE WHEN COUNT.COUNT IS NULL THEN 0 ELSE COUNT.COUNT END,"
						+ "        ?,"
						+ "        LM.PACKAGE_NAME,"
						+ "        LM.CLASS_NAME,"
						+ "        FT.NAME,"
						+ "        SO.TOOL,"
						+ "        F.SUMMARY"
						+ " FROM"
						+ "    SESSION.TEMP_FINDING_IDS TF"
						+ "    INNER JOIN FINDING F ON F.ID = TF.ID"
						+ "    LEFT OUTER JOIN SCAN_OVERVIEW SO ON SO.FINDING_ID = F.ID AND SO.SCAN_ID = ?"
						+ "    LEFT OUTER JOIN SCAN_OVERVIEW PREV ON PREV.FINDING_ID = F.ID AND PREV.SCAN_ID = ?"
						+ "    LEFT OUTER JOIN ("
						+ "       SELECT"
						+ "          A.FINDING_ID \"ID\", COUNT(*) \"COUNT\""
						+ "       FROM AUDIT A"
						+ "       WHERE A.EVENT='COMMENT'"
						+ "       GROUP BY A.FINDING_ID) AS COUNT ON COUNT.ID = F.ID"
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
					deleteSeriesOverview.setLong(idx++, projectRec.getId());
					deleteSeriesOverview.setLong(idx++, q.getId());
					deleteSeriesOverview.execute();
					idx = 1;
					// Fill out parameters
					// populateSeriesOverview.execute();
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

	public static ServerFindingManager getInstance(Connection conn)
			throws SQLException {
		return new ServerFindingManager(conn);
	}

}
