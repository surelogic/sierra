package com.surelogic.sierra.jdbc.product;

import java.sql.Connection;
import java.sql.SQLException;

import com.surelogic.sierra.jdbc.record.ProductRecord;
import com.surelogic.sierra.jdbc.record.UpdateBaseMapper;

/**
 * @author Spencer.Whitman
 * 
 */
public class ProductRecordFactory {

	private static final String PRODUCT_INSERT = "INSERT INTO PRODUCT (NAME) VALUES (?)";
	private static final String PRODUCT_DELETE = "DELETE FROM PRODUCT WHERE ID = ?";
	private static final String PRODUCT_SELECT = "SELECT ID FROM PRODUCT WHERE NAME = ?";
	private static final String PRODUCT_UPDATE = "UPDATE PRODUCT SET NAME = ? WHERE ID = ?";
	
	@SuppressWarnings("unused")
	private final Connection conn;

	private final UpdateBaseMapper productMapper;

	private ProductRecordFactory(Connection conn) throws SQLException {
		this.conn = conn;

		productMapper = new UpdateBaseMapper(conn, PRODUCT_INSERT, PRODUCT_SELECT,
				PRODUCT_DELETE, PRODUCT_UPDATE);
	}

	public static ProductRecordFactory getInstance(Connection conn)
			throws SQLException {
		return new ProductRecordFactory(conn);
	}

	public ProductRecord newProduct() {
		return new ProductRecord(productMapper);
	}

}
