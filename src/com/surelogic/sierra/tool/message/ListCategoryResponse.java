package com.surelogic.sierra.tool.message;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ListCategoryResponse {

	private List<FilterSet> filterSets;

	public List<FilterSet> getFilterSets() {
		if (filterSets == null) {
			filterSets = new ArrayList<FilterSet>();
		}
		return filterSets;
	}

}
