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

	public String getColumnList() {
		Iterator<FindingsColumn> treePartIterator = f_treePart.iterator();
		boolean moreColumns = treePartIterator.hasNext();
		while (moreColumns) {
			FindingsColumn column = treePartIterator.next();
			moreColumns = treePartIterator.hasNext();
			if (moreColumns) {
				column.getColumnWithTitle();
			} else {
				column.getColumnWithTitleAndTreeDivider();
			}

		}
		return null;
	}
}
