package com.surelogic.sierra.gwt.client.data;

import java.util.Comparator;

public class FindingTypeComparator implements Comparator<FindingType> {

	public int compare(FindingType o1, FindingType o2) {
		return o1.getName().compareTo(o2.getName());
	}

}
