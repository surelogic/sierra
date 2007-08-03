package com.surelogic.sierra.client.eclipse.model;

import java.util.HashSet;
import java.util.Set;

import com.surelogic.sierra.tool.message.Importance;

public final class FindingsFilter {

	private final Set<Importance> f_importanceFilter = new HashSet<Importance>();

	public void add(final Importance importance) {
		if (importance != null) {
			f_importanceFilter.add(importance);
		}
	}

	public void remove(final Importance importance) {
		if (importance != null) {
			f_importanceFilter.remove(importance);
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
	 * {@link FindingsOrganization#getQuery(String)}.
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
