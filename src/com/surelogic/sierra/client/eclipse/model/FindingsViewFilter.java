package com.surelogic.sierra.client.eclipse.model;

import java.util.HashSet;
import java.util.Set;

import com.surelogic.sierra.tool.message.Importance;

/**
 * A model that reflects what findings the user wants to be displayed.
 */
public final class FindingsViewFilter {

	/**
	 * The model this filter is contained within. Each model has one and only
	 * one filter. This reference is used to allow notification that the filter
	 * has been mutated.
	 */
	private final FindingsViewModel f_model;

	public FindingsViewFilter(final FindingsViewModel model) {
		f_model = model;
	}

	private final Set<Importance> f_importanceFilter = new HashSet<Importance>();

	public void add(final Importance importance) {
		if (importance != null) {
			f_importanceFilter.add(importance);
			f_model.findingsFilterChanged();
		}
	}

	public void remove(final Importance importance) {
		if (importance != null) {
			f_importanceFilter.remove(importance);
			f_model.findingsFilterChanged();
		}
	}

	public boolean isFiltered(final Importance importance) {
		return f_importanceFilter.contains(importance);
	}

	/**
	 * Extends the <code>where</code> clause of an SQL query that is used to
	 * generate the contents of the findings view.
	 * <p>
	 * This method is only intended to be called from
	 * {@link FindingsViewOrganization#getQuery(String)}.
	 * <p>
	 * <i>Implementation Note:</i> This method depends upon all the elements in
	 * the {@link Importance} enumerations being upper-case (i.e., invoking
	 * <code>toString()</code> on an element of that enumeration returns all
	 * upper-case letters).
	 * 
	 * @param b
	 *            the mutable string to add the <code>where</code> clause to.
	 */
	void addFilter(StringBuilder b) {
		if (!f_importanceFilter.isEmpty()) {
			b.append(" and UPPER(IMPORTANCE) NOT IN (");

			boolean comma = false;
			for (Importance i : f_importanceFilter) {
				if (comma) {
					b.append(",");
				} else {
					comma = true;
				}
				b.append("'").append(i.toString()).append("'");
			}
			b.append(")");
		}
	}
}
