package com.surelogic.sierra.jdbc.product;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import com.surelogic.sierra.jdbc.project.ProjectRecordFactory;
import com.surelogic.sierra.jdbc.record.ProductProjectRecord;
import com.surelogic.sierra.jdbc.record.ProductRecord;
import com.surelogic.sierra.jdbc.record.ProjectRecord;
import com.surelogic.sierra.jdbc.record.RecordRelationRecord;

public class ProductManager {

	@SuppressWarnings("unused")
	private final Connection conn;

	private static final String FIND_ALL = "SELECT NAME FROM PRODUCT";
	private final PreparedStatement findAllStatement;

	private final ProductRecordFactory productFactory;
	private final ProjectRecordFactory projectFactory;
	private final ProductProjectRecordFactory pprFactory;
	private final ProductProjectManager ppManager;

	private ProductManager(Connection conn) throws SQLException {
		this.conn = conn;

		productFactory = ProductRecordFactory.getInstance(conn);
		projectFactory = ProjectRecordFactory.getInstance(conn);
		pprFactory = ProductProjectRecordFactory.getInstance(conn);
		ppManager = ProductProjectManager.getInstance(conn);

		findAllStatement = conn.prepareStatement(FIND_ALL);
	}

	public Collection<String> getAllProductNames() throws SQLException {

		ResultSet rs = findAllStatement.executeQuery();

		Collection<String> productNames = new ArrayList<String>();

		while (rs.next()) {
			productNames.add(rs.getString(1));
		}

		return productNames;
	}

	public Collection<String> getProjectNames(String product)
			throws SQLException {
		// XXX Check to ensure product exists
		return ppManager.getProjectNames(product);
	}

	public Long newProduct(String name, Collection<String> projects)
			throws SQLException {

		if (name == null)
			throw new SQLException();

		ProductRecord product = productFactory.newProduct();
		product.setName(name);

		/** If this product does not exist, add it to the DB */
		if (!product.select())
			product.insert();

		if (projects != null) {
			for (String projectName : projects) {
				ProjectRecord project = projectFactory.newProject();
				project.setName(projectName);
				if (!project.select()) {
					// XXX Throw error
				}

				/** Add a relation between this project and product to the DB */
				ProductProjectRecord rec = pprFactory.newProductProject();
				rec
						.setId(new RecordRelationRecord.PK<ProductRecord, ProjectRecord>(
								product, project));
				rec.insert();
			}
		}
		return product.getId();
	}

	public void deleteProduct(String name) throws SQLException {
		ProductRecord product = productFactory.newProduct();
		product.setName(name);

		/** If this qualifier does not exist, throw an error */
		if (!product.select()) {
			// XXX Throw error
		}

		product.delete();
	}

	public static ProductManager getInstance(Connection conn)
			throws SQLException {
		return new ProductManager(conn);
	}

}
