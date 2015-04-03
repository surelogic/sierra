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

	public ReportSetting(final String name, final List<String> values) {
		super();
		this.name = name;
		this.values = values;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public List<String> getValues() {
		if (values == null) {
			values = new ArrayList<String>();
		}
		return values;
	}

	public void setValues(final List<String> values) {
		this.values = values;
	}

	public ReportSetting copy() {
		final ReportSetting copy = new ReportSetting();
		copy.name = name;
		copy.values = LangUtil.copy(values, new ArrayList<String>());
		return copy;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((values == null) ? 0 : values.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final ReportSetting other = (ReportSetting) obj;
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		if (values == null) {
			if (other.values != null) {
				return false;
			}
		} else if (!values.equals(other.values)) {
			return false;
		}
		return true;
	}

}
