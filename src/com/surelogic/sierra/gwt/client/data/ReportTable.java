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

	private List<List<String>> data;

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

}
