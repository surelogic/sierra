package com.surelogic.sierra.jdbc.record;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class ProductRecord extends LongRecord {

	private static final String INSERT = "INSERT INTO PRODUCT (NAME) VALUES (?)";
	private static final String DELETE = "DELETE FROM PRODUCT WHERE ID = ?";
	private static final String FIND = "SELECT NAME FROM PRODUCT WHERE ID = ?";
	private String name;

	public ProductRecord(RecordMapper mapper) {
		super(mapper);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	protected int fill(PreparedStatement st, int idx) throws SQLException {
		st.setString(idx++, name);
		return idx;
	}

	@Override
	protected int fillWithNk(PreparedStatement st, int idx) throws SQLException {
		st.setString(idx++, name);
		return idx;
	}

	@Override
	protected int readAttributes(ResultSet set, int idx) throws SQLException {
		return idx;
	}

	public static String getInsertSql() {
		return INSERT;
	}

	public static String getDeleteSql() {
		return DELETE;
	}

	public static String getFindSql() {
		return FIND;
	}
}
