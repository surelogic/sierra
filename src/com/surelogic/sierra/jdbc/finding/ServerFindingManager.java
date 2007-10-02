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

	private ServerFindingManager(Connection conn) throws SQLException {
		super(conn);
		deleteSeriesOverview = conn
				.prepareStatement("DELETE FROM TIME_SERIES_OVERVIEW WHERE PROJECT_ID = ? AND QUALIFIER_ID = ?");
		populateSeriesOverview = conn.prepareStatement("SELECT * FROM TIME_SERIES_OVERVIEW");
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
