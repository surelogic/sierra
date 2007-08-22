package com.surelogic.sierra.jdbc.product;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;

import com.surelogic.sierra.jdbc.productProject.ProductProjectRecordFactory;
import com.surelogic.sierra.jdbc.project.ProjectRecordFactory;
import com.surelogic.sierra.jdbc.record.ProductProjectRecord;
import com.surelogic.sierra.jdbc.record.ProductRecord;
import com.surelogic.sierra.jdbc.record.ProjectRecord;
import com.surelogic.sierra.jdbc.record.RelationRecord;

public class ProductManager {

	private final Connection conn;

	private final ProductRecordFactory productFactory;
	private final ProjectRecordFactory projectFactory;
	private final ProductProjectRecordFactory pprFactory;

	private ProductManager(Connection conn) throws SQLException {
		this.conn = conn;
		productFactory = ProductRecordFactory.getInstance(conn);
		projectFactory = ProjectRecordFactory.getInstance(conn);
		pprFactory = ProductProjectRecordFactory.getInstance(conn);
	}

	public Collection<String> getProductNames() {
		// TODO
		return null;
	}

	public Long newProduct(String name, Collection<String> projects)
			throws SQLException {
		ProductRecord product = productFactory.newProduct();
		product.setName(name);

		if (!product.select())
			product.insert();

		for (String projectName : projects) {
			ProjectRecord project = projectFactory.newProject();
			project.setName(projectName);
			if (!project.select()) {
				// XXX Throw error
			}

			/** Add a relation between this project and product to the DB */
			ProductProjectRecord rec = pprFactory.newProductProject();
			rec.setId(new RelationRecord.PK<ProductRecord, ProjectRecord>(
					product, project));
			rec.insert();
		}

		return product.getId();
	}

	public static ProductManager getInstance(Connection conn)
			throws SQLException {
		return new ProductManager(conn);
	}

}
