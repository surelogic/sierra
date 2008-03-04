package com.surelogic.sierra.jdbc.timeseries;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.surelogic.sierra.jdbc.record.TimeseriesRecord;

public final class QualifierManager {

	public static final String ALL_SCANS = "__ALL_SCANS__";

	@SuppressWarnings("unused")
	private final Connection conn;

	private static final String FIND_ALL = "SELECT NAME FROM QUALIFIER";
	private final PreparedStatement findAllQualifierNames;

	private final TimeseriesRecordFactory qualifierFactory;

	private QualifierManager(Connection conn) throws SQLException {
		this.conn = conn;

		qualifierFactory = TimeseriesRecordFactory.getInstance(conn);

		findAllQualifierNames = conn.prepareStatement(FIND_ALL);
	}

	public List<String> getAllQualifierNames() throws SQLException {
		ResultSet rs = findAllQualifierNames.executeQuery();
		List<String> qualifierNames = new ArrayList<String>();
		try {
			while (rs.next()) {
				final String name = rs.getString(1);
				if (!ALL_SCANS.equals(name)) {
					qualifierNames.add(name);
				}
			}
		} finally {
			rs.close();
		}
		return qualifierNames;
	}

	public void deleteQualifier(String name) throws SQLException {
		final TimeseriesRecord qualifier = qualifierFactory.newQualifier();
		qualifier.setName(name);
		if (ALL_SCANS.equals(name) || !qualifier.select()) {
			throw new IllegalArgumentException("Qualifier with the name "
					+ name + " does not exist or could not be deleted.");
		}
		qualifier.delete();
	}

	public long newQualifier(String name) throws SQLException {
		final TimeseriesRecord qualifier = qualifierFactory.newQualifier();
		qualifier.setName(name);
		/** If this qualifier already exists, throw an error */
		if (ALL_SCANS.equals(name) || qualifier.select()) {
			throw new IllegalArgumentException(
					"Qualifier with this name already exists");
		}
		qualifier.insert();
		return qualifier.getId();
	}

	public void renameQualifier(String currName, String newName)
			throws SQLException {
		final TimeseriesRecord qualifier = qualifierFactory.newQualifier();
		qualifier.setName(currName);

		/** If this qualifier does not exist, throw an error */
		if (ALL_SCANS.equals(currName) || ALL_SCANS.equals(newName)
				|| !qualifier.select()) {
			throw new IllegalArgumentException("Qualifier with the name "
					+ currName + " does not exist or could not be renamed to "
					+ newName + ".");
		}
		qualifier.setName(newName);
		qualifier.update();
	}

	public static QualifierManager getInstance(Connection conn)
			throws SQLException {
		return new QualifierManager(conn);
	}
}
