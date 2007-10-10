package com.surelogic.sierra.client.eclipse.views;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.eclipse.swt.widgets.Composite;

import com.surelogic.adhoc.views.QueryUtility;
import com.surelogic.common.eclipse.CascadingList.IColumn;
import com.surelogic.sierra.client.eclipse.Data;
import com.surelogic.sierra.client.eclipse.model.selection.ISelectionObserver;
import com.surelogic.sierra.client.eclipse.model.selection.Selection;

public final class FindingsSelectionReport implements ISelectionObserver {

	private final Selection f_selection;

	FindingsSelectionReport(Selection selection) {
		f_selection = selection;
	}

	public void init() {
		f_selection.addObserver(this);
	}

	public void selectionChanged(Selection selecton) {
		// update the query
	}

	public void selectionStructureChanged(Selection selection) {
		// nothing to do
	}

	public void refresh() {
		final String query = f_selection.getQuery();
		try {
			final Connection c = Data.getConnection();
			try {
				final Statement st = c.createStatement();
				try {
					System.out.println(query);
					final ResultSet rs = st.executeQuery(query);
//					f_finder.addColumnAfter(new IColumn() {
//						public void createContents(Composite panel) {
//							try {
//								QueryUtility.construct(panel, QueryUtility
//										.getColumnLabels(rs), QueryUtility
//										.getRows(rs, 1000));
//							} catch (SQLException e) {
//								// TODO Auto-generated catch block
//								e.printStackTrace();
//							}
//						}
//					}, column, false);

				} finally {
					st.close();
				}
			} finally {
				c.close();
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
