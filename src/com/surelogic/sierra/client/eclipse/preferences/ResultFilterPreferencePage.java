package com.surelogic.sierra.client.eclipse.preferences;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;

import com.surelogic.common.eclipse.job.DatabaseJob;
import com.surelogic.common.eclipse.logging.SLStatus;
import com.surelogic.sierra.client.eclipse.Activator;
import com.surelogic.sierra.client.eclipse.Data;

public class ResultFilterPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage {

	/**
	 * Returns the category name, the finding type name, a short message about
	 * the finding type, and the finding type identifier.
	 */
	private static final String QUERY = "select C.NAME, T.NAME, T.SHORT_MESSAGE, T.ID from FINDING_TYPE T, FINDING_CATEGORY C where T.CATEGORY_ID = C.ID and T.ID in (select FINDING_TYPE_ID from ARTIFACT_TYPE where T.NAME != 'Checkstyle') order by 1,2,3";

	private static class ArtifactRow {
		private long id;
		private String categoryName;
		private String findingTypeName;
		private String findingTypeDescription;

		@Override
		public String toString() {
			return "[Finding type: id=" + id + " name=" + findingTypeName
					+ " description=" + findingTypeDescription + " category="
					+ categoryName + "]";
		}
	}

	@Override
	protected void performApply() {
		// TODO Auto-generated method stub
		super.performApply();
	}

	@Override
	protected void performDefaults() {
		// TODO Auto-generated method stub
		super.performDefaults();
	}

	@Override
	public boolean performOk() {
		// TODO Auto-generated method stub
		return super.performOk();
	}

	private Composite f_panel = null;
	private Tree f_findingTypes = null;
	private Browser f_detailsText = null;
	

	@Override
	protected Control createContents(Composite parent) {
		f_panel = new Composite(parent, SWT.NONE);
		f_panel.setLayout(new FillLayout());

		GridData layoutData;
		f_findingTypes = new Tree(f_panel, SWT.CHECK | SWT.FULL_SELECTION);
		f_findingTypes.setHeaderVisible(true);
		f_findingTypes.setLinesVisible(true);
		layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		f_findingTypes.setLayoutData(layoutData);

		final TreeColumn treeColumn = new TreeColumn(f_findingTypes, SWT.NONE);
		treeColumn.setText("Category/Finding Type");
		treeColumn.setWidth(300);

		final TreeColumn descriptionColumn = new TreeColumn(f_findingTypes, SWT.NONE);
		descriptionColumn.setText("Description");
		descriptionColumn.setWidth(400);
		
		final Group description = new Group(f_panel, SWT.NONE);
		description.setText("Finding Type Description");
		description.setLayout(new FillLayout());
		layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		description.setLayoutData(layoutData);

		try {
			f_detailsText = new Browser(description, SWT.NONE);
		} catch (SWTError e) {
			MessageDialog.openError(Display.getCurrent().getActiveShell(),
					"Browser Failure", "Browser cannot be initialized.");
		}

		Job job = new DatabaseJob("Querying Sierra Artifacts") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				monitor.beginTask("Querying Sierra Artifacts",
						IProgressMonitor.UNKNOWN);
				return queryTableContents();
			}
		};
		job.schedule();
		return f_panel;
	}

	private final List<ArtifactRow> f_artifactList = new ArrayList<ArtifactRow>();

	/**
	 * Must be called from a database job.
	 */
	private IStatus queryTableContents() {
		f_artifactList.clear();
		try {
			final Connection c = Data.readOnlyConnection();
			try {
				final Statement st = c.createStatement();
				try {
					final String query = QUERY;
					final ResultSet rs = st.executeQuery(query);
					while (rs.next()) {
						ArtifactRow row = new ArtifactRow();
						row.categoryName = rs.getString(1);
						row.findingTypeName = rs.getString(2);
						row.findingTypeDescription = rs.getString(3);
						row.id = rs.getLong(4);
						f_artifactList.add(row);
					}
				} finally {
					st.close();
				}
			} finally {
				c.close();
			}
		} catch (SQLException e) {
			return SLStatus.createErrorStatus(
					"Query of Sierra tool artifacts failed", e);
		}
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			public void run() {
				fillTableContents();
			}
		});
		return Status.OK_STATUS;
	}

	/**
	 * Must be called from the UI thread.
	 */
	private void fillTableContents() {
		String currentCategoryName = null;
		TreeItem currentCategoryItem = null;
		for (ArtifactRow row : f_artifactList) {
			if (!row.categoryName.equals(currentCategoryName)) {
				currentCategoryItem = new TreeItem(f_findingTypes, SWT.NONE);
				currentCategoryName = row.categoryName;
				currentCategoryItem.setText(0, row.categoryName);
			}
			final TreeItem item = new TreeItem(currentCategoryItem, SWT.NONE);
			item.setText(0, row.findingTypeName);
			item.setText(1, row.findingTypeDescription);
			item.setData(row.id);
		}
//		for (TreeColumn c : f_table.getColumns()) {
//			c.pack();
//		}
		f_panel.getParent().layout();
	}

	public void init(IWorkbench workbench) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("Use this page to select rules to include/exclude from the scan.");
	}
}
