package com.surelogic.sierra.gwt.client.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.surelogic.sierra.gwt.client.util.LangUtil;

public class ReportSetting implements Serializable {
	private static final long serialVersionUID = -1849548465131070042L;
	private String name;
	private List<String> values;

	public ReportSetting() {
		super();
	}

	public ReportSetting(String name, List<String> values) {
		super();
		this.name = name;
		this.values = values;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<String> getValues() {
		if (values == null) {
			values = new ArrayList<String>();
		}
		return values;
	}

	public void setValues(List<String> values) {
		this.values = values;
	}

	public ReportSetting copy() {
		final ReportSetting copy = new ReportSetting();
		copy.name = name;
		copy.values = LangUtil.copy(values, new ArrayList<String>());
		return copy;
	}
}
