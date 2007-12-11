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
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;

import com.surelogic.adhoc.views.TableUtility;
import com.surelogic.common.eclipse.job.DatabaseJob;
import com.surelogic.common.eclipse.logging.SLStatus;
import com.surelogic.sierra.client.eclipse.Activator;
import com.surelogic.sierra.client.eclipse.Data;

public class ResultFilterPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage {

	private static final String QUERY = "select A.ID, T.NAME, A.MNEMONIC, A.CATEGORY from ARTIFACT_TYPE A, TOOL T order by 2, 3";

	private static class ArtifactRow {
		private long id;
		private String toolName;
		private String mnemonic;
		private String category;

		@Override
		public String toString() {
			return "[Artifact id=" + id + " toolName=" + toolName
					+ " mnemonic=" + mnemonic + " category=" + category + "]";
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
	private Table f_table = null;

	@Override
	protected Control createContents(Composite parent) {
		f_panel = new Composite(parent, SWT.NONE);
		f_panel.setLayout(new FillLayout());

		f_table = new Table(f_panel, SWT.CHECK);
		f_table.setHeaderVisible(true);
		f_table.setLinesVisible(true);

		final TableColumn toolColumn = new TableColumn(f_table, SWT.NONE);
		toolColumn.setText("Tool");
		toolColumn.addListener(SWT.Selection,
				TableUtility.SORT_COLUMN_ALPHABETICALLY);
		toolColumn.setMoveable(true);

		final TableColumn mnemonicColumn = new TableColumn(f_table, SWT.NONE);
		mnemonicColumn.setText("Artifact Mnemonic");
		mnemonicColumn.addListener(SWT.Selection,
				TableUtility.SORT_COLUMN_ALPHABETICALLY);
		mnemonicColumn.setMoveable(true);

		final TableColumn categoryColumn = new TableColumn(f_table, SWT.NONE);
		categoryColumn.setText("Category");
		categoryColumn.addListener(SWT.Selection,
				TableUtility.SORT_COLUMN_ALPHABETICALLY);
		categoryColumn.setMoveable(true);

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
						row.id = rs.getLong(1);
						row.toolName = rs.getString(2);
						row.mnemonic = rs.getString(3);
						row.category = rs.getString(4);
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
		for (ArtifactRow row : f_artifactList) {
			System.out.println(row);
			final TableItem item = new TableItem(f_table, SWT.NONE);
			item.setText(0, row.toolName);
			item.setText(1, row.mnemonic);
			item.setText(2, row.category);
			item.setData(row.id);
		}
		for (TableColumn c : f_table.getColumns()) {
			c.pack();
		}
		f_panel.getParent().layout();
	}

	public void init(IWorkbench workbench) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("Use this page to select rules to include/exclude from the scan.");
	}
}
