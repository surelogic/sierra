package com.surelogic.sierra.client.eclipse.views;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;

import org.eclipse.core.runtime.IPath;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.ExpandItem;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.PageBook;

import com.surelogic.adhoc.views.QueryUtility;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.client.eclipse.Activator;
import com.surelogic.sierra.client.eclipse.Data;
import com.surelogic.sierra.client.eclipse.model.FindingsViewModel;
import com.surelogic.sierra.client.eclipse.model.FindingsViewOrganization;
import com.surelogic.sierra.client.eclipse.model.IFindingsViewModelObserver;
import com.surelogic.sierra.client.eclipse.model.Projects;
import com.surelogic.sierra.tool.message.Importance;

public final class FindingsMediator implements IFindingsViewModelObserver {

	private FindingsViewModel f_model;

	private final PageBook f_pages;

	private final Control f_noFindingsPage;

	private final Composite f_findingsPage;

	private final Combo f_projectCombo;

	private final ToolItem f_organizations;

	private final Combo f_organizeByCombo;

	private final ToolItem f_filters;

	private final Composite f_topSash;

	private final ExpandItem f_detailsItem;

	private final Composite f_detailsComp;

	private final ExpandItem f_logItem;

	private final Composite f_logComp;

	private Composite f_results = null;

	FindingsMediator(PageBook pages, Control noFindingsPage,
			Composite findingsPage, Combo projectCombo, ToolItem organizations,
			Combo organizeByCombo, ToolItem filters, Composite topSash,
			ExpandItem detailsItem, Composite detailsComp, ExpandItem logItem,
			Composite logComp) {
		final IPath pluginState = Activator.getDefault().getStateLocation();
		final File modelSaveFile = new File(pluginState.toOSString()
				+ System.getProperty("file.separator") + "findings-view.xml");
		f_model = new FindingsViewModel(modelSaveFile);
		f_pages = pages;
		f_noFindingsPage = noFindingsPage;
		f_findingsPage = findingsPage;
		f_projectCombo = projectCombo;
		f_organizations = organizations;
		f_organizeByCombo = organizeByCombo;
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
						f_model.getFilter().add(i);
						System.out.println("add:" + i);
					} else {
						f_model.getFilter().remove(i);
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
	}

	public void setFocus() {
		// TODO something reasonable
	}

	public void init() {
		f_projectCombo.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				/*
				 * The user has selected a project within the UI.
				 */
				final String projectName = f_projectCombo.getText();
				f_model.setProjectFocus(projectName);
			}
		});

		f_model.addObserver(this);
		f_model.init();

		f_organizeByCombo.setItems(f_model.getViewOrganizationKeys());
		setOrganizationInCombo();
		f_organizeByCombo.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				/*
				 * The user has selected a view organization within the UI.
				 */
				final String organizationName = f_organizeByCombo.getText();
				f_model.setViewOrganizationFocus(organizationName);
			}
		});
	}

	public void dispose() {
		f_model.dispose();
	}

	public void findingsFilterChanged(FindingsViewModel model) {
		// TODO Auto-generated method stub

	}

	public void findingsOrganizationChanged(FindingsViewModel model) {
		// TODO Auto-generated method stub

	}

	public void findingsOrganizationFocusChanged(final FindingsViewModel model) {
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			public void run() {
				setProjectInCombo();
				System.out.println("Changed project to"
						+ model.getProjectFocus());
				updateFindingsTreeTable();
			}
		});
	}

	public void noProjects(FindingsViewModel model) {
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			public void run() {
				/*
				 * Since there are no projects in the database we have nothing
				 * to display.
				 */
				f_pages.showPage(f_noFindingsPage);
			}
		});
	}

	public void projectListChanged(FindingsViewModel model) {
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			public void run() {
				f_pages.showPage(f_findingsPage);
				f_projectCombo.setItems(Projects.getInstance()
						.getProjectNamesArray());
				setProjectInCombo();
			}
		});
	}

	public void projectFocusChanged(final FindingsViewModel model) {
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			public void run() {
				setProjectInCombo();
				updateFindingsTreeTable();
			}
		});
	}

	private void setProjectInCombo() {
		final String projectNameFocus = f_model.getProjectFocus();
		String[] items = f_projectCombo.getItems();
		for (int i = 0; i < items.length; i++) {
			if (items[i].equals(projectNameFocus)) {
				if (f_projectCombo.getSelectionIndex() != i)
					f_projectCombo.select(i);
				return;
			}
		}
	}

	private void setOrganizationInCombo() {
		final String organizationFocus = f_model.getViewOrganizationFocus();
		String[] items = f_organizeByCombo.getItems();
		for (int i = 0; i < items.length; i++) {
			if (items[i].equals(organizationFocus)) {
				if (f_organizeByCombo.getSelectionIndex() != i)
					f_organizeByCombo.select(i);
				return;
			}
		}
	}

	private void updateFindingsTreeTable() {
		final FindingsViewOrganization org = f_model.getViewOrganization();

		final String project = f_model.getProjectFocus();
		if (project == null) {
			SLLogger.getLogger().log(Level.WARNING,
					"No project is in focus on the findings view (file a bug)");
			return;
		}

		String query = org.getQuery(project, f_model.getFilter());
		System.out.println("query \"" + query + "\"");

		try {
			final Connection c = Data.getConnection();
			try {
				final Statement st = c.createStatement();
				try {
					// TODO: fix query
					if (true)
						return;
					final ResultSet rs = st.executeQuery(query);
					final String[] columnLabels = QueryUtility
							.getColumnLabels(rs);
					final String[][] rows = QueryUtility.getRows(rs, 20000);
					PlatformUI.getWorkbench().getDisplay().asyncExec(
							new Runnable() {
								public void run() {
									if (f_results != null) {
										f_results.dispose();
									}
									System.out.println("got results");
									f_results = QueryUtility.construct(
											f_topSash, columnLabels, rows);
									f_results.setLayoutData(new GridData(
											SWT.FILL, SWT.FILL, true, true));
									f_topSash.layout();
								}
							});
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
}
