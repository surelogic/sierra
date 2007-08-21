package com.surelogic.sierra.jdbc.finding;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;

import com.surelogic.sierra.jdbc.JDBCUtils;
import com.surelogic.sierra.jdbc.run.ProjectRecord;

public class ProductManager {

	private final Connection conn;
	private final PreparedStatement insert;
	private final PreparedStatement findProject;

	private ProductManager(Connection conn) throws SQLException {
		this.conn = conn;
		insert = conn.prepareStatement(ProductRecord.getInsertSql(),
				Statement.RETURN_GENERATED_KEYS);
		findProject = conn.prepareStatement(ProjectRecord.getFindSql(),
				Statement.RETURN_GENERATED_KEYS);
	}

	public Collection<String> getProductNames() {
		// TODO
		return null;
	}

	public Long newProduct(String name, Collection<String> projects)
			throws SQLException {
		ProductRecord product = new ProductRecord();
		product.setName(name);
		JDBCUtils.insert(insert, product);
		for (String project : projects) {
			ProjectRecord proj = new ProjectRecord();
		}
		return product.getId();
	}

	public static ProductManager getInstance(Connection conn)
			throws SQLException {
		return new ProductManager(conn);
	}

}
