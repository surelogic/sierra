package com.surelogic.sierra.jdbc.product;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import com.surelogic.sierra.jdbc.record.ProductRecord;

public class ProductManager {

	@SuppressWarnings("unused")
	private final Connection conn;

	private static final String FIND_ALL = "SELECT NAME FROM PRODUCT";
	private final PreparedStatement findAllStatement;

	private final ProductRecordFactory productFactory;

	private final ProductProjectManager ppManager;

	private ProductManager(Connection conn) throws SQLException {
		this.conn = conn;

		productFactory = ProductRecordFactory.getInstance(conn);

		ppManager = ProductProjectManager.getInstance(conn);

		findAllStatement = conn.prepareStatement(FIND_ALL);
	}

	/**
	 * 
	 * @return a collection of all the product names
	 * @throws SQLException
	 */
	public Collection<String> getAllProductNames() throws SQLException {
		ResultSet rs = findAllStatement.executeQuery();
		Collection<String> productNames = new ArrayList<String>();
		try {
			while (rs.next()) {
				productNames.add(rs.getString(1));
			}
		} finally {
			rs.close();
		}
		return productNames;
	}

	/**
	 * 
	 * @param productName
	 * @return A collection of all the project names associated to this product
	 * @throws SQLException
	 */
	public Collection<String> getProjectNames(String productName)
			throws SQLException {

		if (productName == null)
			throw new SQLException();

		ProductRecord product = productFactory.newProduct();
		product.setName(productName);

		/** If this product does not exist, throw an error */
		if (!product.select()) {
			// XXX Throw error
			throw new SQLException();
		}
		return ppManager.getProjectNames(productName);
	}

	/**
	 * Product must already exist in the database
	 * 
	 * @param product
	 * @param projects
	 * @throws SQLException
	 */
	private void addProjects(ProductRecord product, Collection<String> projects)
			throws SQLException {
		if (projects != null) {
			for (String projectName : projects) {
				ppManager.addProjectRelation(product, projectName);
			}
		}
	}

	/**
	 * Add a list of projects to the pre-existing product
	 * 
	 * @param productName
	 * @param projects
	 * @throws SQLException
	 */
	public void addProjects(String productName, Collection<String> projects)
			throws SQLException {

		if (productName == null)
			throw new SQLException();

		ProductRecord product = productFactory.newProduct();
		product.setName(productName);

		/** If this product does not exist, throw an error */
		if (!product.select()) {
			// XXX Throw error
			throw new SQLException();
		}

		addProjects(product, projects);
	}

	/**
	 * Rename a product from currName to newName
	 * 
	 * @param currName
	 * @param newName
	 * @throws SQLException
	 */
	public void renameProduct(String currName, String newName)
			throws SQLException {
		ProductRecord product = productFactory.newProduct();

		product.setName(currName);

		/** If this product does not exist, throw an error */
		if (!product.select()) {
			// XXX Throw error
			throw new SQLException();
		}

		product.setName(newName);
		product.update();
	}

	/**
	 * Create a new product
	 * 
	 * @param name
	 *            name of the new product
	 * @param projects
	 *            associated projects of the new product
	 * @return The ID of the new product
	 * @throws SQLException
	 */
	public Long newProduct(String name, Collection<String> projects)
			throws SQLException {

		if (name == null)
			throw new SQLException();

		ProductRecord product = productFactory.newProduct();
		product.setName(name);

		/** If this product does not exist, add it to the DB */
		if (!product.select())
			product.insert();

		addProjects(product, projects);

		return product.getId();
	}

	/**
	 * Remove a product from the DB identified by the name of the product
	 * 
	 * @param name
	 * @throws SQLException
	 */
	public void deleteProduct(String name) throws SQLException {
		ProductRecord product = productFactory.newProduct();
		product.setName(name);

		/** If this product does not exist, throw an error */
		if (!product.select()) {
			// XXX Throw error
			throw new SQLException();
		}

		product.delete();
	}

	public static ProductManager getInstance(Connection conn)
			throws SQLException {
		return new ProductManager(conn);
	}

}
