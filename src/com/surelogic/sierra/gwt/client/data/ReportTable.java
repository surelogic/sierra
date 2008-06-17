package com.surelogic.sierra.gwt.client.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ReportTable implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 9035444817120365740L;

	public enum ColumnData {
		TEXT("cell-text"), DATE("cell-date"), NUMBER("cell-number");

		private final String css;

		ColumnData(String css) {
			this.css = css;
		}

		public String getCSS() {
			return css;
		}

	}

	private List<String> headers;
	private List<ColumnData> columns;
	private List<List<String>> data;

	private Report report;

	public ReportTable() {

	}

	public ReportTable(Report report) {
		this.report = report;
	}

	public List<ColumnData> getColumns() {
		if (columns == null) {
			columns = new ArrayList<ColumnData>();
		}
		return columns;
	}

	public List<String> getHeaders() {
		if (headers == null) {
			headers = new ArrayList<String>();
		}
		return headers;
	}

	public List<List<String>> getData() {
		if (data == null) {
			data = new ArrayList<List<String>>();
		}
		return data;
	}

	public Report getReport() {
		return report;
	}

}
