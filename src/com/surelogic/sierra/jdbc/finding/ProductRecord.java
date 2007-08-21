package com.surelogic.sierra.jdbc.finding;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.surelogic.sierra.jdbc.LongRecord;

public class ProductRecord extends LongRecord {

	private static final String INSERT = "INSERT INTO PRODUCT (NAME) VALUES (?)";
	private static final String DELETE = "DELETE FROM PRODUCT WHERE ID = ?";
	private static final String FIND = "SELECT NAME FROM PRODUCT WHERE ID = ?";
	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int fill(PreparedStatement st, int idx) throws SQLException {
		st.setString(idx++, name);
		return idx;
	}

	static String getInsertSql() {
		return INSERT;
	}

	static String getDeleteSql() {
		return DELETE;
	}

	static String getSelectSql() {
		return FIND;
	}
}
