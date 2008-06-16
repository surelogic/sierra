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

	private final List<String> headers = new ArrayList<String>();
	private final List<ColumnData> columns = new ArrayList<ColumnData>();
	private final List<List<String>> data = new ArrayList<List<String>>();

	private Report report;

	public ReportTable() {

	}

	public ReportTable(Report report) {
		this.report = report;
	}

	public List<ColumnData> getColumns() {
		return columns;
	}

	public List<String> getHeaders() {
		return headers;
	}

	public List<List<String>> getData() {
		return data;
	}

	public Report getReport() {
		return report;
	}

}
