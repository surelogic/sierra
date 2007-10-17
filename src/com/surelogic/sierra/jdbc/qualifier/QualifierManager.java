package com.surelogic.sierra.jdbc.qualifier;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.surelogic.sierra.jdbc.record.QualifierRecord;

public class QualifierManager {

	@SuppressWarnings("unused")
	private final Connection conn;

	private static final String FIND_ALL = "SELECT NAME FROM QUALIFIER";
	private final PreparedStatement findAllQualifierNames;

	private final QualifierRecordFactory qualifierFactory;

	private QualifierManager(Connection conn) throws SQLException {
		this.conn = conn;

		qualifierFactory = QualifierRecordFactory.getInstance(conn);

		findAllQualifierNames = conn.prepareStatement(FIND_ALL);
	}

	
	
	public List<String> getAllQualifierNames() throws SQLException {
		ResultSet rs = findAllQualifierNames.executeQuery();
		List<String> qualifierNames = new ArrayList<String>();
		try {
			while (rs.next()) {
				qualifierNames.add(rs.getString(1));
			}
		} finally {
			rs.close();
		}
		return qualifierNames;
	}

	public void deleteQualifier(String name) throws SQLException {
		QualifierRecord qualifier = qualifierFactory.newQualifier();
		qualifier.setName(name);

		/** If this qualifier does not exist, throw an error */
		if (!qualifier.select()) {
			// XXX Throw error
			throw new SQLException();
		}

		qualifier.delete();
	}

	public Long newQualifier(String name) throws SQLException {
		QualifierRecord qualifier = qualifierFactory.newQualifier();
		qualifier.setName(name);

		/** If this qualifier already exists, throw an error */
		if (qualifier.select()) {
			// XXX Throw error
			throw new SQLException();
		}

		qualifier.insert();

		return qualifier.getId();
	}

	public void renameQualifier(String currName, String newName)
			throws SQLException {
		QualifierRecord qualifier = qualifierFactory.newQualifier();

		qualifier.setName(currName);

		/** If this qualifier does not exist, throw an error */
		if (!qualifier.select()) {
			// XXX Throw error
			throw new SQLException();
		}

		qualifier.setName(newName);
		qualifier.update();
	}

	public static QualifierManager getInstance(Connection conn)
			throws SQLException {
		return new QualifierManager(conn);
	}
}
