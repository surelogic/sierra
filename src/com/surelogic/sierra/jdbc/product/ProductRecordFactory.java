package com.surelogic.sierra.jdbc.product;

import java.sql.Connection;
import java.sql.SQLException;

import com.surelogic.sierra.jdbc.record.BaseMapper;
import com.surelogic.sierra.jdbc.record.ProductRecord;

/** 
 * @author Spencer.Whitman
 *
 */
public class ProductRecordFactory {
	
	private static final String PRODUCT_INSERT = "INSERT INTO PRODUCT (NAME) VALUES (?)";
	private static final String PRODUCT_DELETE = "DELETE FROM PRODUCT WHERE ID = ?";
	private static final String PRODUCT_SELECT = "SELECT NAME FROM PRODUCT WHERE ID = ?";
	
	private final Connection conn;

	private final BaseMapper productMapper;
	
	private ProductRecordFactory(Connection conn) throws SQLException {
		this.conn = conn;
		
		productMapper = new BaseMapper(conn, PRODUCT_INSERT, PRODUCT_SELECT,
				PRODUCT_DELETE);
	}

	public static ProductRecordFactory getInstance(Connection conn) throws SQLException {
		return new ProductRecordFactory(conn);
	}
	
	ProductRecord newProduct() {
		return new ProductRecord(productMapper);
	}
	
}
