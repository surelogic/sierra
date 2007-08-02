package com.surelogic.sierra.client.eclipse.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This class defines one organization of the findings for the findings view.
 */
public final class FindingsOrganization {

	private final List<FindingsColumn> f_treePart = new ArrayList<FindingsColumn>();

	private final List<FindingsColumn> f_tablePart = new ArrayList<FindingsColumn>();

	public List<FindingsColumn> getMutableTreePart() {
		return f_treePart;
	}

	public List<FindingsColumn> getMutableTablePart() {
		return f_tablePart;
	}

	private void addColumnList(StringBuilder b) {
		boolean first = true;
		Iterator<FindingsColumn> treePartIterator = f_treePart.iterator();
		boolean moreColumns = treePartIterator.hasNext();
		while (moreColumns) {
			if (first) {
				first = false;
			} else {
				b.append(", ");
			}
			FindingsColumn column = treePartIterator.next();
			moreColumns = treePartIterator.hasNext();
			if (moreColumns) {
				b.append(column.getColumnWithTitle());
			} else {
				b.append(column.getColumnWithTitleAndTreeDivider());
			}
		}

		for (FindingsColumn column : f_tablePart) {
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
		for (FindingsColumn column : f_treePart) {
			if (first) {
				first = false;
			} else {
				b.append(", ");
			}
			b.append(column.getOrderBy());
		}
		for (FindingsColumn column : f_tablePart) {
			if (first) {
				first = false;
			} else {
				b.append(", ");
			}
			b.append(column.getOrderBy());
		}
	}

	public String getQuery(final String projectName) {
		StringBuilder b = new StringBuilder();
		b.append("select ");
		addColumnList(b);
		b.append(" from FINDINGS_OVERVIEW");
		b.append(" where PROJECT='" + projectName + "'");
		addOrderBy(b);
		return b.toString();
	}
}
