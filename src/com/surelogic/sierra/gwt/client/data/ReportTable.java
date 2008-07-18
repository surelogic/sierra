package com.surelogic.sierra.gwt.client.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ReportTable implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 9035444817120365740L;

	private List<String> headers;
	private List<ColumnData> columns;
	private List<List<String>> data;
	private List<String> links;

	private Report report;

	public ReportTable() {
		// Do nothing
	}

	public ReportTable(final Report report) {
		this.report = report;
	}

	/**
	 * A list of the column data types, in order.
	 * 
	 * @return
	 */
	public List<ColumnData> getColumns() {
		if (columns == null) {
			columns = new ArrayList<ColumnData>();
		}
		return columns;
	}

	/**
	 * A list of the column headers, in order.
	 * 
	 * @return
	 */
	public List<String> getHeaders() {
		if (headers == null) {
			headers = new ArrayList<String>();
		}
		return headers;
	}

	/**
	 * A list of table rows, with each entry in the list being a list of the
	 * column data, in order.
	 * 
	 * @return
	 */
	public List<List<String>> getData() {
		if (data == null) {
			data = new ArrayList<List<String>>();
		}
		return data;
	}

	/**
	 * A list of links, one for each row. This list may be empty if no column
	 * has a data type of {@link ColumnData.LINK}
	 * 
	 * @return
	 */
	public List<String> getLinks() {
		if (links == null) {
			links = new ArrayList<String>();
		}
		return links;
	}

	/**
	 * The report used to generate this report table
	 * 
	 * @return
	 */
	public Report getReport() {
		return report;
	}

}
