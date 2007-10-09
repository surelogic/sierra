package com.surelogic.sierra.jdbc.product;

import java.sql.Connection;
import java.sql.SQLException;

import com.surelogic.sierra.jdbc.record.BaseMapper;
import com.surelogic.sierra.jdbc.record.ProductProjectRecord;

public class ProductProjectRecordFactory {

	@SuppressWarnings("unused")
	private final Connection conn;

	private final String INSERT = "INSERT INTO PRODUCT_PROJECT_RELTN (PRODUCT_ID, PROJECT_NAME) VALUES (?,?)";
	private final String DELETE = "DELETE FROM PRODUCT_PROJECT_RELTN WHERE PRODUCT_ID = ? AND PROJECT_NAME = ?";

	private final BaseMapper pprMapper;

	private ProductProjectRecordFactory(Connection conn) throws SQLException {
		this.conn = conn;

		pprMapper = new BaseMapper(conn, INSERT, null, DELETE, false);
	}

	public static ProductProjectRecordFactory getInstance(Connection conn)
			throws SQLException {
		return new ProductProjectRecordFactory(conn);
	}

	public ProductProjectRecord newProductProject() {
		return new ProductProjectRecord(pprMapper);
	}

}
