package com.surelogic.sierra.client.eclipse.views;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.ExpandItem;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.surelogic.adhoc.views.QueryUtility;
import com.surelogic.common.eclipse.SLImages;
import com.surelogic.sierra.client.eclipse.SLog;
import com.surelogic.sierra.client.eclipse.model.FindingsModel;
import com.surelogic.sierra.client.eclipse.model.FindingsOrganization;
import com.surelogic.sierra.db.Data;
import com.surelogic.sierra.tool.message.Importance;

public final class FindingsView extends ViewPart {

	private FindingsMediator f_mediator = null;

	private FindingsModel f_manager = new FindingsModel();

	private Composite results = null;

	@Override
	public void createPartControl(final Composite parent) {
		parent.setLayout(new GridLayout());

		final Composite projectSelector = new Composite(parent, SWT.NONE);
		projectSelector.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false));
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		projectSelector.setLayout(gridLayout);

		Label label = new Label(projectSelector, SWT.NONE);
		label.setAlignment(SWT.RIGHT);
		label.setImage(SLImages
				.getJDTImage(ISharedImages.IMG_OBJS_PACKFRAG_ROOT));
		label = new Label(projectSelector, SWT.NONE);
		label.setAlignment(SWT.RIGHT);
		label.setText("Project:");

		final Combo projectCombo = new Combo(projectSelector, SWT.DROP_DOWN);
		projectCombo.setItems(new String[] { "Test", "JEdit", "Project 3",
				"Project 4" });
		projectCombo.select(0);
		projectCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false));

		/*
		 * Findings for the project
		 */
		final Group findingsGroup = new Group(parent, SWT.NONE);
		findingsGroup.setText("Analysis Findings");
		findingsGroup
				.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		findingsGroup.setLayout(new FillLayout());

		SashForm sf = new SashForm(findingsGroup, SWT.VERTICAL | SWT.SMOOTH);

		final Composite topSash = new Composite(sf, SWT.NONE);
		topSash.setLayout(new GridLayout());

		final Composite findingsBar = new Composite(topSash, SWT.NONE);
		final GridLayout findingsBarLayout = new GridLayout();
		findingsBarLayout.numColumns = 4;
		findingsBar.setLayout(findingsBarLayout);
		findingsBar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false));

		final ToolBar groupByBar = new ToolBar(findingsBar, SWT.HORIZONTAL);
		final ToolItem groupings = new ToolItem(groupByBar, SWT.PUSH);
		groupings.setImage(SLImages.getImage(SLImages.IMG_CATEGORY));
		groupings
				.setToolTipText("Define how findings are organized within this view");

		final Label groupBy = new Label(findingsBar, SWT.NONE);
		groupBy.setText("by");
		final Combo groupByCombo = new Combo(findingsBar, SWT.DROP_DOWN
				| SWT.READ_ONLY);
		groupByCombo.setItems(f_manager.getKeys());
		groupByCombo.select(0);

		final Listener updateFindingsOverview = new Listener() {
			public void handleEvent(Event event) {
				String key = groupByCombo.getItem(groupByCombo
						.getSelectionIndex());
				if (key == null)
					return;
				FindingsOrganization org = f_manager.get(key);
				if (org == null) {
					SLog.logWarning("no FindingsOrganization for key " + key);
					return;
				}
				String project = projectCombo.getItem(projectCombo
						.getSelectionIndex());
				if (project == null) {
					SLog.logWarning("no project to qualify key " + key);
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
																	topSash,
																	columnLabels,
																	rows);
													results
															.setLayoutData(new GridData(
																	SWT.FILL,
																	SWT.FILL,
																	true, true));
													topSash.layout();
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
							SLog.logError("SQL problem in findings view", e);
						} finally {
							st.close();
						}
					} finally {
						c.close();
					}
				} catch (SQLException e) {
					SLog.logError("Could not work with the embedded database",
							e);
				}
			}
		};
		groupByCombo.addListener(SWT.Selection, updateFindingsOverview);

		/*
		 * Toolbar for analysis findings
		 */

		final ToolBar toolBar = new ToolBar(findingsBar, SWT.HORIZONTAL);
		toolBar.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));

		final ToolItem filter = new ToolItem(toolBar, SWT.DROP_DOWN);
		filter.setImage(SLImages.getImage(SLImages.IMG_FILTER));
		filter
				.setToolTipText("Configure the filters to be applied to this view");
		filter.setText("Filters");

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
					updateFindingsOverview.handleEvent(null);
				}
			}
		};
		final Menu filterMenu = new Menu(parent.getShell(), SWT.POP_UP);
		for (Importance i : Importance.values()) {
			MenuItem item = new MenuItem(filterMenu, SWT.CHECK);
			item.setText(i.toString());
			item.addListener(SWT.Selection, filterCheckListener);
		}
		filter.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				Rectangle r = filter.getBounds();
				Point p = new Point(r.x, r.y + r.height);
				p = filter.getDisplay().map(toolBar, null, p);
				filterMenu.setLocation(p);
				filterMenu.setVisible(true);
			}
		});

		new ToolItem(toolBar, SWT.SEPARATOR);

		final ToolItem export = new ToolItem(toolBar, SWT.PUSH);
		export.setImage(SLImages.getImage(SLImages.IMG_EXPORT));
		export.setToolTipText("Export findings to a file");
		export.setText("Export");

		final Menu toolBarMenu = new Menu(parent.getShell(), SWT.POP_UP);
		final MenuItem showText = new MenuItem(toolBarMenu, SWT.CHECK);
		showText.setText("Show Text");
		showText.setSelection(true);
		showText.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				if (showText.getSelection()) {
					filter.setText("Filters");
					export.setText("Export");
				} else {
					filter.setText("");
					export.setText("");
				}
				topSash.layout();
			}
		});
		findingsBar.setMenu(toolBarMenu);
		groupByBar.setMenu(toolBarMenu);
		toolBar.setMenu(toolBarMenu);
		groupBy.setMenu(toolBarMenu);

		/*
		 * Analysis findings
		 */

		Tree tree = new Tree(topSash, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.FULL_SELECTION);
		tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		results = tree;

		ExpandBar bar = new ExpandBar(sf, SWT.V_SCROLL);
		int barIndex = 0;
		// Second item
		final Composite detailsComp = new Composite(bar, SWT.NONE);
		final ExpandItem detailsItem = new ExpandItem(bar, SWT.NONE, barIndex++);
		detailsItem.setText("Details");
		detailsItem.setControl(detailsComp);
		detailsItem.setImage(SLImages.getImage(SLImages.IMG_DETAILS));

		// Second item
		final Composite logComp = new Composite(bar, SWT.NONE);
		final ExpandItem logItem = new ExpandItem(bar, SWT.NONE, barIndex++);
		logItem.setText("Log");
		logItem.setControl(logComp);
		logItem.setImage(SLImages.getImage(SLImages.IMG_COMMENT));

		sf.setWeights(new int[] { 3, 1 });
		bar.setSpacing(2);

		f_mediator = new FindingsMediator(projectCombo, topSash, detailsItem,
				detailsComp, logItem, logComp);
		updateFindingsOverview.handleEvent(null);
	}

	@Override
	public void setFocus() {
		if (f_mediator != null) {
			f_mediator.setFocus();
		}
	}
}
