package com.surelogic.sierra.jdbc.finding;

import java.sql.Connection;
import java.sql.SQLException;

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
	
	
	
	private ServerFindingManager(Connection conn) throws SQLException {
		super(conn);
	}

	
	
	
	
	public static ServerFindingManager getInstance(Connection conn)
			throws SQLException {
		return new ServerFindingManager(conn);
	}

}
