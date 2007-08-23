package com.surelogic.sierra.jdbc.run;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.surelogic.sierra.jdbc.record.RunRecord;
import com.surelogic.sierra.tool.analyzer.RunGenerator;

public class RunManager {

	@SuppressWarnings("unused")
	private final Connection conn;
	private final RunRecordFactory factory;

	private static final String DELETE_UNUSED_SOURCES = "DELETE FROM SOURCE_LOCATION WHERE ID IN ("
			+ " SELECT NO_PRIMARY.ID FROM ("
			+ " SELECT SL.ID \"ID\""
			+ " FROM SOURCE_LOCATION SL"
			+ " LEFT OUTER JOIN ARTIFACT A ON A.PRIMARY_SOURCE_LOCATION_ID = SL.ID"
			+ " WHERE A.PRIMARY_SOURCE_LOCATION_ID IS NULL"
			+ " ) AS NO_PRIMARY"
			+ " LEFT OUTER JOIN ARTIFACT_SOURCE_LOCATION_RELTN ASLR ON ASLR.SOURCE_LOCATION_ID = NO_PRIMARY.ID"
			+ " WHERE ASLR.SOURCE_LOCATION_ID IS NULL)";

	private static final String DELETE_UNUSED_COMPILATIONS = "DELETE FROM COMPILATION_UNIT WHERE ID IN ("
			+ " SELECT CU.ID"
			+ " FROM COMPILATION_UNIT CU"
			+ " LEFT OUTER JOIN SOURCE_LOCATION SL ON SL.COMPILATION_UNIT_ID = CU.ID"
			+ " WHERE SL.COMPILATION_UNIT_ID IS NULL)";

	private final PreparedStatement deleteSources;
	private final PreparedStatement deleteCompilations;

	private RunManager(Connection conn) throws SQLException {
		this.conn = conn;
		this.factory = RunRecordFactory.getInstance(conn);
		this.deleteCompilations = conn
				.prepareStatement(DELETE_UNUSED_COMPILATIONS);
		this.deleteSources = conn.prepareStatement(DELETE_UNUSED_SOURCES);
	}

	public RunGenerator getRunGenerator() {
		return JDBCRunGenerator.getInstance(conn);
	}

	public void deleteRun(String uid) throws SQLException {
		RunRecord rec = factory.newRun();
		rec.setUid(uid);
		if (!rec.select()) {
			throw new IllegalArgumentException("No run with uid" + uid
					+ " exists in the database");
		}
		rec.delete();
		deleteSources.execute();
		deleteCompilations.execute();
	}

	public static RunManager getInstance(Connection conn) throws SQLException {
		return new RunManager(conn);
	}
}
