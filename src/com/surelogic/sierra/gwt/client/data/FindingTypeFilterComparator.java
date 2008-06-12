package com.surelogic.sierra.gwt.client.data;

import java.util.Comparator;

public class FindingTypeFilterComparator implements Comparator<FindingTypeFilter> {

	public int compare(FindingTypeFilter o1, FindingTypeFilter o2) {
		return o1.getName().compareTo(o2.getName());
	}

}
