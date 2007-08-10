package com.surelogic.sierra.jdbc;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

/**
 * A collection of utility methods specific to SIERRA to help w/ using JDBC.
 * 
 * @author nathan
 * 
 */
public class JDBCUtils {

	/**
	 * Set a paramter to the specified String, or to null if none is supplied.
	 * 
	 * @param idx
	 * @param st
	 * @param string
	 * @throws SQLException
	 */
	public static void setNullableString(int idx, PreparedStatement st,
			String string) throws SQLException {
		if (string == null) {
			st.setNull(idx, Types.VARCHAR);
		} else {
			st.setString(idx, string);
		}
	}

	/**
	 * Set a paramter to the specified Long, or to null if none is supplied.
	 * 
	 * @param idx
	 * @param st
	 * @param longValue
	 * @throws SQLException
	 */
	public static void setNullableLong(int idx, PreparedStatement st,
			Long longValue) throws SQLException {
		if (longValue == null) {
			st.setNull(idx, Types.VARCHAR);
		} else {
			st.setLong(idx, longValue);
		}
	}

	/**
	 * Set a paramter to the specified Integer, or to null if none is supplied.
	 * 
	 * @param idx
	 * @param st
	 * @param intValue
	 * @throws SQLException
	 */
	public static void setNullableInt(int idx, PreparedStatement st,
			Integer intValue) throws SQLException {
		if (intValue == null) {
			st.setNull(idx, Types.VARCHAR);
		} else {
			st.setLong(idx, intValue);
		}
	}

	/**
	 * Insert a record, and read it's generated id. This method is only for use
	 * with statements that return a generated id.
	 * 
	 * @param st
	 * @param record
	 * @throws SQLException
	 */
	public static void insert(PreparedStatement st, Record<?> record)
			throws SQLException {
		ResultSet keys;
		record.fill(st, 1);
		st.executeUpdate();
		keys = st.getGeneratedKeys();
		keys.next();
		record.readPk(keys, 1);
	}

	/**
	 * Find a record by it's full set of values (the same set of values it was
	 * inserted by).
	 * 
	 * @param st
	 * @param record
	 * @return
	 * @throws SQLException
	 */
	public static boolean find(PreparedStatement st, Record<?> record)
			throws SQLException {
		record.fill(st, 1);
		ResultSet set = st.executeQuery();
		boolean found = set.next();
		if (found) {
			record.readPk(set, 1);
		}
		return found;
	}

}
