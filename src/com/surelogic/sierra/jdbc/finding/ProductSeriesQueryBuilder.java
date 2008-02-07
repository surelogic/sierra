package com.surelogic.sierra.jdbc.finding;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.jdbc.product.ProductRecordFactory;
import com.surelogic.sierra.jdbc.qualifier.QualifierRecordFactory;
import com.surelogic.sierra.jdbc.record.ProductRecord;
import com.surelogic.sierra.jdbc.record.QualifierRecord;

public final class ProductSeriesQueryBuilder {

	private static final Logger log = SLLogger
			.getLoggerFor(ProductSeriesQueryBuilder.class);

	private final Connection conn;

	private final StringBuilder builder = new StringBuilder();

	private List<Long> scanIds;

	private ProductSeriesQueryBuilder(Connection conn) {
		this.conn = conn;
	}

	public String queryRelevantOrIrrelevantCounts() {
		builder.setLength(0);

		return builder.toString();
	}

	public String queryImportanceCounts() {
		builder.setLength(0);

		return builder.toString();
	}

	public String queryTopFindings() {
		builder.setLength(0);

		return builder.toString();
	}

	public String queryFindingTypes() {
		builder.setLength(0);

		return builder.toString();
	}

	public void setContext(String product, String timeSeries) {
		try {
			ProductRecord p = ProductRecordFactory.getInstance(conn)
					.newProduct();
			p.setName(product);
			if (p.select()) {
				QualifierRecord q = QualifierRecordFactory.getInstance(conn)
						.newQualifier();
				q.setName(timeSeries);
				if (q.select()) {
					Statement st = conn.createStatement();
					try {
						ResultSet set = st
								.executeQuery("SELECT S.ID FROM QUALIFIER_SCAN_RELTN QSR, SCAN S WHERE QSR.QUALIFIER_ID = "
										+ q.getId()
										+ " AND S.ID = QSR.SCAN_ID AND S.PROJECT_ID = "
										+ p.getId());
						scanIds = new ArrayList<Long>();
						while (set.next()) {
							scanIds.add(set.getLong(1));
						}
					} finally {
						st.close();
					}
				} else {
					throw new IllegalArgumentException(timeSeries
							+ " is not a valid time series.");
				}
			} else {
				throw new IllegalArgumentException(product
						+ " is not a valid product name.");
			}
		} catch (SQLException e) {
			log.severe(e.getMessage());
		}

	}

	/** Unused */
	/*
	 * private static void inClause(StringBuilder builder, List<?> values) {
	 * builder.append("("); for (Iterator<?> i = values.iterator();
	 * i.hasNext();) { builder.append(i.next()); if (i.hasNext()) {
	 * builder.append(", "); } } builder.append(")"); }
	 */
}
