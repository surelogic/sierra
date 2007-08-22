package com.surelogic.sierra.jdbc.product;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;

import com.surelogic.sierra.jdbc.project.ProjectRecordFactory;
import com.surelogic.sierra.jdbc.record.ProductRecord;
import com.surelogic.sierra.jdbc.record.ProjectRecord;

public class ProductManager {

	private final Connection conn;
	private final ProductRecordFactory productFactory;
	private final ProjectRecordFactory projectFactory;
	
	private ProductManager(Connection conn) throws SQLException {
		this.conn = conn;
		productFactory = ProductRecordFactory.getInstance(conn);
		projectFactory = ProjectRecordFactory.getInstance(conn);
	}

	public Collection<String> getProductNames() {
		// TODO
		return null;
	}

	public Long newProduct(String name, Collection<String> projects)
			throws SQLException {
		ProductRecord product = productFactory.newProduct();
		product.setName(name);
		
		if(!product.select())
			product.insert();
		
		for (String project : projects) {
			ProjectRecord proj = projectFactory.newProject();
			proj.setName(project);
			if(!proj.select()) {
				// XXX Throw error
			}
			
			
			
		}
		
		return product.getId();
	}

	public static ProductManager getInstance(Connection conn)
			throws SQLException {
		return new ProductManager(conn);
	}

}
