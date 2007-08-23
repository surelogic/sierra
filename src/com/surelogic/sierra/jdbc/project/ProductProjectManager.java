package com.surelogic.sierra.jdbc.project;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

public class ProductProjectManager {

	@SuppressWarnings("unused")
	private final Connection conn;

	private static final String GET_PROJECT_NAMES = "SELECT PR.NAME FROM PRODUCT PD, PRODUCT_PROJECT_RELTN PPR, PROJECT PR WHERE PD.NAME = ? AND PPR.PRODUCT_ID = PD.ID AND PD.ID = PPR.PROJECT_ID";
	private final PreparedStatement ps;

	private ProductProjectManager(Connection conn) throws SQLException {
		this.conn = conn;
	
//		ProductProjectRecordFactory.getInstance(conn);
		
		ps = conn.prepareStatement(GET_PROJECT_NAMES);
	}

	/**
	 * 
	 * @param product
	 * @return all the project names related to the given product name
	 * @throws SQLException
	 */
	public Collection<String> getProjectNames(String product)
			throws SQLException {

		ps.setString(1, product);
		ResultSet rs = ps.executeQuery();

		Collection<String> projectNames = new ArrayList<String>();
		while (rs.next()) {
			projectNames.add(rs.getString(1));
		}

		return projectNames;
	}

	public static ProductProjectManager getInstance(Connection conn)
			throws SQLException {
		return new ProductProjectManager(conn);
	}

}
