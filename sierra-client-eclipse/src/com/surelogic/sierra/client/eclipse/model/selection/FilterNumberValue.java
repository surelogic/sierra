package com.surelogic.sierra.client.eclipse.model.selection;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public abstract class FilterNumberValue extends Filter {

	FilterNumberValue(Selection selection, Filter previous) {
		super(selection, previous);
	}

	@Override
	protected void deriveAllValues() {
		/*
		 * We need this sorted by the number not by the string version of the
		 * number.
		 */
		f_allValues.clear();
		List<Integer> f_values = new LinkedList<Integer>();
		for (String s : f_counts.keySet()) {
			f_values.add(Integer.parseInt(s));
		}
		Collections.sort(f_values);
		for (Integer i : f_values) {
			f_allValues.add(i.toString());
		}
	}
}
