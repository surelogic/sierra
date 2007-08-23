package com.surelogic.sierra.jdbc.product;

import java.sql.Connection;
import java.sql.SQLException;

import com.surelogic.sierra.jdbc.record.BaseMapper;
import com.surelogic.sierra.jdbc.record.ProductProjectRecord;

public class ProductProjectRecordFactory {

	private final Connection conn;

	private final BaseMapper pprMapper;

	private ProductProjectRecordFactory(Connection conn) throws SQLException {
		this.conn = conn;

		pprMapper = new BaseMapper(conn, null, null, null);
	}

	public static ProductProjectRecordFactory getInstance(Connection conn)
			throws SQLException {
		return new ProductProjectRecordFactory(conn);
	}

	public ProductProjectRecord newProductProject() {
		return new ProductProjectRecord(pprMapper);
	}

}
