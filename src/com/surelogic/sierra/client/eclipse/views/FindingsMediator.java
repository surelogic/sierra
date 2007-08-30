package com.surelogic.sierra.client.eclipse.views;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.ExpandItem;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.PageBook;

import com.surelogic.adhoc.views.QueryUtility;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.client.eclipse.Data;
import com.surelogic.sierra.client.eclipse.model.FindingsModel;
import com.surelogic.sierra.client.eclipse.model.FindingsOrganization;
import com.surelogic.sierra.tool.message.Importance;

public final class FindingsMediator {

	private FindingsModel f_manager = new FindingsModel();

	private final Listener f_updateFindingsOverview;

	private final PageBook f_pages;

	private final Widget f_noFindingsPage;

	private final Composite f_findingsPage;

	private final Combo f_projectCombo;

	private final ToolItem f_groupings;

	private final Combo f_groupByCombo;

	private final ToolItem f_filters;

	private final Composite f_topSash;

	private final ExpandItem f_detailsItem;

	private final Composite f_detailsComp;

	private final ExpandItem f_logItem;

	private final Composite f_logComp;

	private Composite results = null;

	FindingsMediator(PageBook pages, Widget noFindingsPage,
			Composite findingsPage, Combo projectCombo, ToolItem groupings,
			Combo groupByCombo, ToolItem filters, Composite topSash,
			ExpandItem detailsItem, Composite detailsComp, ExpandItem logItem,
			Composite logComp) {
		f_pages = pages;
		f_noFindingsPage = noFindingsPage;
		f_findingsPage = findingsPage;
		f_projectCombo = projectCombo;
		f_groupings = groupings;
		f_groupByCombo = groupByCombo;
		f_filters = filters;
		f_topSash = topSash;
		f_detailsItem = detailsItem;
		f_detailsComp = detailsComp;
		f_logItem = logItem;
		f_logComp = logComp;

		final Listener filterCheckListener = new Listener() {
			public void handleEvent(Event event) {
				MenuItem item = (MenuItem) event.widget;
				Importance i = Importance.valueOf(item.getText());
				if (i != null) {
					if (item.getSelection()) {
						f_manager.getFilter().add(i);
						System.out.println("add:" + i);
					} else {
						f_manager.getFilter().remove(i);
						System.out.println("remove:" + i);

					}
					// TODO signal to update the results.
				}
			}
		};
		final Menu filterMenu = new Menu(f_findingsPage.getShell(), SWT.POP_UP);
		for (Importance i : Importance.values()) {
			MenuItem item = new MenuItem(filterMenu, SWT.CHECK);
			item.setText(i.toString());
			item.addListener(SWT.Selection, filterCheckListener);
		}
		f_filters.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				Rectangle r = f_filters.getBounds();
				Point p = new Point(r.x, r.y + r.height);
				p = f_filters.getDisplay().map(f_filters.getControl(), null, p);
				filterMenu.setLocation(p);
				filterMenu.setVisible(true);
			}
		});

		f_updateFindingsOverview = new Listener() {
			public void handleEvent(Event event) {
				String key = f_groupByCombo.getItem(f_groupByCombo
						.getSelectionIndex());
				if (key == null)
					return;
				FindingsOrganization org = f_manager.get(key);
				if (org == null) {
					SLLogger.getLogger().log(Level.WARNING,
							"No FindingsOrganization for key " + key);
					return;
				}
				String project = f_projectCombo.getText();
				if (project == null) {
					SLLogger.getLogger().log(Level.WARNING,
							"No project to qualify key " + key);
					return;
				}
				String query = org.getQuery(project, f_manager.getFilter());
				System.out.println(key + " selected");
				System.out.println("query \"" + query + "\"");
				try {
					final Connection c = Data.getConnection();
					try {
						final Statement st = c.createStatement();
						try {
							boolean hasResultSet = st.execute(query);
							if (hasResultSet) {
								// result set
								final ResultSet rs = st.getResultSet();
								final String[] columnLabels = QueryUtility
										.getColumnLabels(rs);
								final String[][] rows = QueryUtility.getRows(
										rs, 20000);
								PlatformUI.getWorkbench().getDisplay()
										.asyncExec(new Runnable() {
											public void run() {
												if (results != null) {
													results.dispose();
													results = QueryUtility
															.construct(
																	f_topSash,
																	columnLabels,
																	rows);
													results
															.setLayoutData(new GridData(
																	SWT.FILL,
																	SWT.FILL,
																	true, true));
													f_topSash.layout();
												}
											}
										});
								// do stuff with result set
								System.out.println("got a result set");

							} else {
								// update count or no results
								int updateCount = st.getUpdateCount();
							}
						} catch (SQLException e) {
							// an error occurred with the query
							SLLogger.getLogger().log(Level.SEVERE,
									"SQL problem in findings view", e);
						} finally {
							st.close();
						}
					} finally {
						c.close();
					}
				} catch (SQLException e) {
					SLLogger.getLogger().log(Level.SEVERE,
							"Could not work with the embedded database", e);
				}
			}
		};

	}

	public void setFocus() {
		// TODO Auto-generated method stub

	}
}
