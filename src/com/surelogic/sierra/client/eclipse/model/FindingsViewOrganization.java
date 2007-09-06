package com.surelogic.sierra.client.eclipse.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A model that reflects how the user wants findings organized when they are
 * displayed.
 */
public final class FindingsViewOrganization {

	/**
	 * The model this is contained within. Each model has one and only one
	 * filter. This reference is used to allow notification that the filter has
	 * been mutated.
	 */
	private final FindingsViewModel f_model;

	public FindingsViewOrganization(final FindingsViewModel model) {
		f_model = model;
	}

	private final List<FindingsViewColumn> f_treePart = new ArrayList<FindingsViewColumn>();

	private final List<FindingsViewColumn> f_tablePart = new ArrayList<FindingsViewColumn>();

	public List<FindingsViewColumn> getMutableTreePart() {
		return f_treePart;
	}

	public List<FindingsViewColumn> getMutableTablePart() {
		return f_tablePart;
	}
	
	public void mutated() {
		f_model.findingsOrganizationChanged();
	}

	private void addColumnList(StringBuilder b) {
		boolean first = true;
		Iterator<FindingsViewColumn> treePartIterator = f_treePart.iterator();
		boolean moreColumns = treePartIterator.hasNext();
		while (moreColumns) {
			if (first) {
				first = false;
			} else {
				b.append(", ");
			}
			FindingsViewColumn column = treePartIterator.next();
			moreColumns = treePartIterator.hasNext();
			if (moreColumns) {
				b.append(column.getColumnWithTitle());
			} else {
				b.append(column.getColumnWithTitleAndTreeDivider());
			}
		}

		for (FindingsViewColumn column : f_tablePart) {
			if (first) {
				first = false;
			} else {
				b.append(", ");
			}
			b.append(column.getColumnWithTitle());
		}
	}

	private void addOrderBy(StringBuilder b) {
		b.append(" order by ");
		boolean first = true;
		for (FindingsViewColumn column : f_treePart) {
			if (first) {
				first = false;
			} else {
				b.append(", ");
			}
			b.append(column.getOrderBy());
		}
		for (FindingsViewColumn column : f_tablePart) {
			if (first) {
				first = false;
			} else {
				b.append(", ");
			}
			b.append(column.getOrderBy());
		}
	}

	/**
	 * 
	 * @param projectName
	 * @param filter
	 *            a filter for the query, may be <code>null</code>.
	 * @return
	 */
	public String getQuery(final String projectName,
			final FindingsViewFilter filter) {
		if (f_treePart.isEmpty() && f_tablePart.isEmpty())
			throw new IllegalStateException(
					"Cannot generate a query from an empty organization");
		StringBuilder b = new StringBuilder();
		b.append("select ");
		addColumnList(b);
		b.append(" from FINDINGS_OVERVIEW");
		b.append(" where PROJECT='" + projectName + "'");
		if (filter != null)
			filter.addFilter(b);
		addOrderBy(b);
		return b.toString();
	}
}
