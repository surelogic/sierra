package com.surelogic.sierra.jdbc.finding;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.surelogic.sierra.jdbc.qualifier.QualifierRecordFactory;
import com.surelogic.sierra.jdbc.record.QualifierRecord;

/**
 * Builds queries for accessing time series reporting data.
 * 
 * @author nathan
 * 
 */
public class TimeSeriesQueryBuilder {

	private final Connection conn;

	private final PreparedStatement getLatestScansByQualifierName;

	private List<ProductQueries> queries;

	private Long qualifierId;
	private final StringBuilder builder;

	private TimeSeriesQueryBuilder(Connection conn) {
		this.builder = new StringBuilder();
		this.conn = conn;
		try {
			this.getLatestScansByQualifierName = conn
					.prepareStatement("SELECT P.NAME,LS.PROJECT,LS.SCAN_ID"
							+ "   FROM LATEST_SCANS LS"
							+ "   LEFT OUTER JOIN PRODUCT_PROJECT_RELTN PPR ON PPR.PROJECT_NAME = LS.PROJECT"
							+ "   LEFT JOIN PRODUCT P ON P.ID = PPR.PRODUCT_ID"
							+ "   WHERE LS.QUALIFIER = ? ORDER BY PPR.PRODUCT_ID");
		} catch (SQLException e) {
			throw new IllegalStateException(e);
		}
	}

	public static TimeSeriesQueryBuilder getInstance(Connection conn)
			throws SQLException {
		return new TimeSeriesQueryBuilder(conn);
	}

	/**
	 * Returns a list of all products in this time series with data in them. If
	 * a project has data for this time series, but does not belong to a
	 * product, it implicitly belongs to a product with the same name as the
	 * project.
	 * 
	 * @return
	 */
	public List<ProductQueries> getProductQueries() {
		return queries;
	}

	/**
	 * Set the context that these queries build off of.
	 * 
	 * @param timeSeries
	 *            the name of a valid qualifer/timeSeries
	 */
	public void setTimeSeries(String timeSeries) {
		queries = new ArrayList<ProductQueries>();
		try {
			QualifierRecord q = QualifierRecordFactory.getInstance(conn)
					.newQualifier();
			q.setName(timeSeries);
			if (q.select()) {
				getLatestScansByQualifierName.setString(1, timeSeries);
				ResultSet set = getLatestScansByQualifierName.executeQuery();
				try {
					List<Long> scanIds = null;
					String product = null;
					while (set.next()) {
						int idx = 1;
						String newProduct = set.getString(idx++);
						String project = set.getString(idx++);
						Long scanId = set.getLong(idx++);
						if (newProduct == null) {
							queries.add(new ProductQueries(project, Collections
									.singletonList(scanId)));
						} else {
							if (!newProduct.equals(product)) {
								product = newProduct;
								scanIds = new ArrayList<Long>();
								queries
										.add(new ProductQueries(product,
												scanIds));
							}
							scanIds.add(scanId);
						}
					}
				} finally {
					set.close();
				}
				this.qualifierId = q.getId();
			} else {
				throw new IllegalArgumentException(timeSeries
						+ " is not a valid name for a time series/qualifier.");
			}
		} catch (SQLException e) {
			throw new IllegalStateException(e);
		}

	}

	public class ProductQueries {

		private final String product;
		private List<Long> scanIds;

		private ProductQueries(String product, List<Long> scanIds) {
			this.product = product;
			this.scanIds = scanIds;
		}

		/**
		 * Produces a query that gives a breakdown of the number of findings by
		 * importance for the latest set of scans in this time series.
		 * 
		 * @return
		 */
		public String queryLatestImportanceCounts() {
			builder.setLength(0);
			builder
					.append("SELECT TSO.IMPORTANCE, COUNT(TSO.FINDING_ID) \"Count\" FROM SCAN_OVERVIEW SO, TIME_SERIES_OVERVIEW TSO WHERE SO.SCAN_ID IN ");
			inClause(builder, scanIds);
			builder.append(" AND TSO.QUALIFIER_ID = ");
			builder.append(qualifierId);
			builder
					.append(" AND TSO.FINDING_ID = SO.FINDING_ID GROUP BY TSO.IMPORTANCE");
			builder.append(" ORDER BY CASE");
			builder.append(" WHEN TSO.IMPORTANCE='Irrelevant' THEN 1");
			builder.append(" WHEN TSO.IMPORTANCE='Low' THEN 2");
			builder.append(" WHEN TSO.IMPORTANCE='Medium' THEN 3");
			builder.append(" WHEN TSO.IMPORTANCE='High' THEN 4");
			builder.append(" WHEN TSO.IMPORTANCE='Critical' THEN 5");
			builder.append(" END");
			return builder.toString();
		}

		/**
		 * Produces a query that gives a breakdown of the number of findings for
		 * the latest set of scans in this time series based on whether or not
		 * they are relevant.
		 * 
		 * @return
		 */
		public String queryLatestRelevantOrIrrelevantCounts() {
			builder.setLength(0);
			builder.append("SELECT * FROM");
			builder
					.append("(SELECT COUNT(SO.FINDING_ID) \"Irrelevant\" FROM SCAN_OVERVIEW SO, TIME_SERIES_OVERVIEW TSO WHERE SO.SCAN_ID IN ");
			inClause(builder, scanIds);
			builder.append(" AND TSO.QUALIFIER_ID = ");
			builder.append(qualifierId);
			builder
					.append(" AND TSO.FINDING_ID = SO.FINDING_ID AND TSO.IMPORTANCE='Irrelevant') IRRELEVANT");
			builder.append(",");
			builder
					.append("(SELECT COUNT(SO.FINDING_ID) \"Relevant\" FROM SCAN_OVERVIEW SO, TIME_SERIES_OVERVIEW TSO WHERE SO.SCAN_ID IN ");
			inClause(builder, scanIds);
			builder.append(" AND TSO.QUALIFIER_ID = ");
			builder.append(qualifierId);
			builder
					.append(" AND TSO.FINDING_ID = SO.FINDING_ID AND TSO.IMPORTANCE!='Irrelevant') RELEVANT");
			return builder.toString();
		}

		/**
		 * Produces a query that lists the number of findings for each kind of
		 * finding type for the latest set of scans in this time series.
		 * 
		 * @return
		 */
		public String queryLatestFindingTypeCounts() {
			builder.setLength(0);
			builder
					.append("SELECT TSO.FINDING_TYPE \"Finding Type\", COUNT(TSO.FINDING_ID) \"Count\" FROM SCAN_OVERVIEW SO, TIME_SERIES_OVERVIEW TSO WHERE SO.SCAN_ID IN ");
			inClause(builder, scanIds);
			builder.append(" AND TSO.QUALIFIER_ID = ");
			builder.append(qualifierId);
			builder
					.append(" AND TSO.FINDING_ID = SO.FINDING_ID GROUP BY TSO.FINDING_TYPE");
			return builder.toString();
		}

		public String getProduct() {
			return product;
		}

	}

	private static void inClause(StringBuilder builder, List<?> values) {
		builder.append("(");
		for (Iterator<?> i = values.iterator(); i.hasNext();) {
			builder.append(i.next());
			if (i.hasNext()) {
				builder.append(", ");
			}
		}
		builder.append(")");
	}
}
