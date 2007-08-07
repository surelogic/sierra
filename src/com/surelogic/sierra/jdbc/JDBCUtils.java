package com.surelogic.sierra.jdbc;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

public class JDBCUtils {
	public static void setNullableString(int idx, PreparedStatement st,
			String string) throws SQLException {
		if (string == null) {
			st.setNull(idx, Types.VARCHAR);
		} else {
			st.setString(idx, string);
		}
	}

	public static void setNullableLong(int idx, PreparedStatement st,
			Long longValue) throws SQLException {
		if (longValue == null) {
			st.setNull(idx, Types.VARCHAR);
		} else {
			st.setLong(idx, longValue);
		}
	}

	public static void setNullableInt(int idx, PreparedStatement st,
			Integer intValue) throws SQLException {
		if (intValue == null) {
			st.setNull(idx, Types.VARCHAR);
		} else {
			st.setLong(idx, intValue);
		}
	}

	/**
	 * Insert a record, and read it's generated id.
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
	 * Find a record by it's full set of values.
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
