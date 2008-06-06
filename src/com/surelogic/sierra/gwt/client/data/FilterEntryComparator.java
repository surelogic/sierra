package com.surelogic.sierra.gwt.client.data;

import java.util.Comparator;

public class FilterEntryComparator implements Comparator<FilterEntry> {

	public int compare(FilterEntry o1, FilterEntry o2) {
		return o1.getName().compareTo(o2.getName());
	}

}
