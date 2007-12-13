package com.surelogic.sierra.tool.message;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType
@XmlRootElement
public class GlobalSettings {

	private List<FindingTypeFilter> filter;

	public List<FindingTypeFilter> getFilter() {
		if (filter == null) {
			filter = new ArrayList<FindingTypeFilter>();
		}
		return filter;
	}

	public void setFilter(List<FindingTypeFilter> filter) {
		this.filter = filter;
	}

}
