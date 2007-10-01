package com.surelogic.sierra.jdbc.product;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import com.surelogic.sierra.jdbc.record.ProductProjectRecord;
import com.surelogic.sierra.jdbc.record.ProductRecord;
import com.surelogic.sierra.jdbc.record.RecordStringRelationRecord;

public class ProductProjectManager {

	@SuppressWarnings("unused")
	private final Connection conn;

	private static final String GET_PROJECT_NAMES = "SELECT PR.NAME FROM PRODUCT PD, PRODUCT_PROJECT_RELTN PPR, PROJECT PR WHERE PD.NAME = ? AND PPR.PRODUCT_ID = PD.ID AND PR.NAME = PPR.PROJECT_NAME";
	private final PreparedStatement getProjectNames;

	private ProductProjectRecordFactory pprFactory;
	private final ProductRecordFactory productFactory;

	private ProductProjectManager(Connection conn) throws SQLException {
		this.conn = conn;

		pprFactory = ProductProjectRecordFactory.getInstance(conn);
		productFactory = ProductRecordFactory.getInstance(conn);

		getProjectNames = conn.prepareStatement(GET_PROJECT_NAMES);
	}

	/**
	 * 
	 * @param product
	 * @return all the project names related to the given product name
	 * @throws SQLException
	 */
	public Collection<String> getProjectNames(String product)
			throws SQLException {
		getProjectNames.setString(1, product);
		ResultSet rs = getProjectNames.executeQuery();
		try {
			Collection<String> projectNames = new ArrayList<String>();
			while (rs.next()) {
				projectNames.add(rs.getString(1));
			}
			return projectNames;
		} finally {
			rs.close();
		}
	}

	public void addProjectRelation(ProductRecord product, String projectName)
			throws SQLException {

		if (product == null)
			throw new SQLException();

		ProductProjectRecord ppr = pprFactory.newProductProject();
		ppr.setId(new RecordStringRelationRecord.PK<ProductRecord, String>(
				product, projectName));
		ppr.insert();
	}

	public void addProjectRelation(String productName, String projectName)
			throws SQLException {
		// Add a relation between this product and this project (don't check the
		// project name)
		if (productName == null)
			throw new SQLException();

		ProductRecord product = productFactory.newProduct();

		product.setName(productName);

		if (!product.select()) {
			// TODO
			throw new SQLException();
		}

		addProjectRelation(product, projectName);
	}

	public void deleteProjectRelation(ProductRecord product, String projectName)
			throws SQLException {

		if (product == null)
			throw new SQLException();

		ProductProjectRecord ppr = pprFactory.newProductProject();
		ppr.setId(new RecordStringRelationRecord.PK<ProductRecord, String>(
				product, projectName));
		ppr.delete();
	}

	public void deleteProjectRelation(String productName, String projectName)
			throws SQLException {
		// Add a relation between this product and this project (don't check the
		// project name)
		if (productName == null)
			throw new SQLException();

		ProductRecord product = productFactory.newProduct();

		product.setName(productName);

		if (!product.select()) {
			// TODO
			throw new SQLException();
		}

		deleteProjectRelation(product, projectName);
	}

	public static ProductProjectManager getInstance(Connection conn)
			throws SQLException {
		return new ProductProjectManager(conn);
	}

}
