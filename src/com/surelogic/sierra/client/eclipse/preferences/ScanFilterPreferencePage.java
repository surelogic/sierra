package com.surelogic.sierra.client.eclipse.preferences;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.UIJob;

import com.surelogic.common.eclipse.HTMLPrinter;
import com.surelogic.common.eclipse.dialogs.ErrorDialogUtility;
import com.surelogic.common.eclipse.jobs.DatabaseJob;
import com.surelogic.common.eclipse.jobs.SLUIJob;
import com.surelogic.common.eclipse.logging.SLEclipseStatusUtility;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.jdbc.NullDBQuery;
import com.surelogic.common.jdbc.QB;
import com.surelogic.common.jdbc.Query;
import com.surelogic.common.jdbc.Row;
import com.surelogic.common.jdbc.RowHandler;
import com.surelogic.common.jdbc.TransactionException;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.client.eclipse.Activator;
import com.surelogic.sierra.client.eclipse.Data;
import com.surelogic.sierra.client.eclipse.StyleSheetHelper;
import com.surelogic.sierra.jdbc.settings.SettingQueries;

public class ScanFilterPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage {

	private static class FindingTypeRow {
		private String findingTypeId;
		private String findingTypeName;
		private String findingTypeDescription;

		@Override
		public String toString() {
			return "[Finding type: id=" + findingTypeId + " name="
					+ findingTypeName + " description="
					+ findingTypeDescription + "]";
		}
	}

	private static class ArtifactTypeRow {
		private String toolName;
		private String mnemonic;
		private String link;
		private String categoryName;

		@Override
		public String toString() {
			return "[Artifact type: tool=" + toolName + " mnemonic=" + mnemonic
					+ " link=" + link + " category=" + categoryName + "]";
		}
	}

	@Override
	protected void performApply() {
		applyChanges();
		super.performApply();
	}

	@Override
	protected void performDefaults() {
		final Set<String> filter = SettingQueries
				.getSureLogicDefaultScanFilters();
		for (final TreeItem item : f_findingTypes.getItems()) {
			item.setChecked(!filter.contains(item.getData()));
		}
		super.performDefaults();
	}

	@Override
	public boolean performOk() {
		applyChanges();
		return super.performOk();
	}

	private void applyChanges() {
		final List<String> filterIdExcludes = new ArrayList<String>();
		final List<String> filterIdIncludes = new ArrayList<String>();
		for (final TreeItem item : f_findingTypes.getItems()) {
			/*
			 * All of these should have the finding type id string as data.
			 */
			final String id = (String) item.getData();
			if (item.getChecked()) {
				filterIdIncludes.add(id);
			} else {
				filterIdExcludes.add(id);
			}
		}
		if (SLLogger.getLogger().isLoggable(Level.FINEST)) {
			/*
			 * Here we output a file in the users home directory recording the
			 * settings. This is useful if we want to overwrite the
			 * SureLogicDefaultFilterSet.txt file in the sierra-jdbc project.
			 * 
			 * This will only occur if the logger is set to a level of FINEST.
			 */
			final File output = new File(System.getProperty("user.home")
					+ System.getProperty("file.separator")
					+ "SierraFilterSet.txt");
			try {
				final PrintWriter p = new PrintWriter(output);
				try {
					for (final String id : filterIdIncludes) {
						p.println(id);
					}
				} finally {
					p.close();
				}
			} catch (final FileNotFoundException e) {
				SLLogger.getLogger().log(Level.SEVERE,
						"IO failure writing " + output + " filter set file", e);
			}
		}
		final Job job = new DatabaseJob("Updating Global Sierra Settings",
				Job.INTERACTIVE) {
			@Override
			protected IStatus run(final IProgressMonitor monitor) {
				monitor.beginTask("Updating Global Sierra Settings",
						IProgressMonitor.UNKNOWN);
				return updateSettings(filterIdIncludes);
			}
		};
		job.schedule();
	}

	private Composite f_panel = null;
	private Tree f_findingTypes = null;
	private Browser f_detailsText = null;
	private String f_selectedFindingTypeId = null;

	@Override
	protected Control createContents(final Composite parent) {
		f_panel = new Composite(parent, SWT.NONE);
		f_panel.setLayout(new GridLayout());
		f_panel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		GridData layoutData; // used to configure the layout
		f_findingTypes = new Tree(f_panel, SWT.CHECK | SWT.FULL_SELECTION);
		f_findingTypes.setHeaderVisible(true);
		f_findingTypes.setLinesVisible(true);
		layoutData = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
		layoutData.heightHint = 250;
		f_findingTypes.setLayoutData(layoutData);
		f_findingTypes.addListener(SWT.Selection, new Listener() {
			public void handleEvent(final Event event) {
				boolean clearHTMLDescription = true;
				/*
				 * Selection of an item (not the same as checking it).
				 */
				final TreeItem[] items = f_findingTypes.getSelection();
				if (items.length > 0) {
					final Object rawData = items[0].getData();
					if (rawData instanceof String) {
						final String findingTypeId = (String) rawData;
						clearHTMLDescription = false;
						/*
						 * The below check avoids lots of extra queries since
						 * this event is invoked when the selection changes and
						 * when a check box is checked.
						 */
						if (!findingTypeId.equals(f_selectedFindingTypeId)) {
							f_selectedFindingTypeId = findingTypeId;
							final Job job = new DatabaseJob(
									"Querying Sierra Artifact Type Description",
									Job.INTERACTIVE) {
								@Override
								protected IStatus run(
										final IProgressMonitor monitor) {
									monitor
											.beginTask(
													"Querying Sierra Artifact Type Description",
													IProgressMonitor.UNKNOWN);
									return queryFindingTypeHTMLDescriptionOf(findingTypeId);
								}
							};
							job.schedule();
						}
					}
				}
				/*
				 * Check state of an item.
				 */
				if ((event.detail == SWT.CHECK)
						&& (event.item instanceof TreeItem)) {
					final TreeItem item = (TreeItem) event.item;
					/*
					 * Only categories have sub-items so pass along the change.
					 */
					for (final TreeItem sub : item.getItems()) {
						sub.setChecked(item.getChecked());
					}
				}
				if (clearHTMLDescription) {
					clearHTMLDescription();
				}
			}
		});

		final Menu contextMenu = new Menu(f_findingTypes.getShell(), SWT.POP_UP);
		createMenuItem(contextMenu, "Select All").addListener(SWT.Selection,
				new Listener() {
					public void handleEvent(Event event) {
						setTypes(true);
					}
				});
		createMenuItem(contextMenu, "Unselect All").addListener(SWT.Selection,
				new Listener() {
					public void handleEvent(Event event) {
						setTypes(false);
					}
				});
		f_findingTypes.setMenu(contextMenu);

		final TreeColumn treeColumn = new TreeColumn(f_findingTypes, SWT.NONE);
		treeColumn.setText("Finding Type");
		treeColumn.setWidth(300);

		final TreeColumn descriptionColumn = new TreeColumn(f_findingTypes,
				SWT.NONE);
		descriptionColumn.setText("Description");
		descriptionColumn.setWidth(400);

		final Group description = new Group(f_panel, SWT.NONE);
		description.setText("Finding Type Description");
		description.setLayout(new FillLayout());
		layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		description.setLayoutData(layoutData);

		try {
			f_detailsText = new Browser(description, SWT.NONE);
		} catch (final SWTError e) {
			final int errNo = 26;
			final String msg = I18N.err(errNo);
			final IStatus reason = SLEclipseStatusUtility.createErrorStatus(
					errNo, msg, e);
			ErrorDialogUtility.open(null, "Browser Failure", reason);
		}
		clearHTMLDescription();

		final Job job = new DatabaseJob("Querying Sierra Artifacts") {
			@Override
			protected IStatus run(final IProgressMonitor monitor) {
				monitor.beginTask("Querying Sierra Artifacts",
						IProgressMonitor.UNKNOWN);
				return queryTableContents();
			}
		};
		job.schedule();

		/*
		 * Allow access to help via the F1 key.
		 */
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent,
				"com.surelogic.sierra.client.eclipse.preferences-sierra");

		return f_panel;
	}

	private MenuItem createMenuItem(final Menu contextMenu, String text) {
		MenuItem item = new MenuItem(contextMenu, SWT.PUSH);
		item.setText(text);
		return item;
	}

	private final List<FindingTypeRow> f_findingTypeList = new ArrayList<FindingTypeRow>();

	// List of included finding types
	private final List<String> f_filterList = new ArrayList<String>();

	/**
	 * Must be called from a database job.
	 */
	private IStatus queryTableContents() {
		f_findingTypeList.clear();
		try {
			Data.getInstance().withReadOnly(new NullDBQuery() {
				@Override
				public void doPerform(final Query q) {
					f_findingTypeList.addAll(q.statement("query.00002",
							new RowHandler<FindingTypeRow>() {
								public FindingTypeRow handle(final Row r) {
									final FindingTypeRow row = new FindingTypeRow();
									row.findingTypeName = r.nextString();
									row.findingTypeDescription = r.nextString();
									row.findingTypeId = r.nextString();
									return row;
								}
							}).call());

					f_filterList.addAll(SettingQueries.scanFilterForUid(
							SettingQueries.LOCAL_UUID).perform(q)
							.getIncludedFindingTypes());

				}
			});
		} catch (final TransactionException e) {
			final int errNo = 54;
			final String msg = I18N.err(errNo);
			return SLEclipseStatusUtility.createErrorStatus(errNo, msg, e);
		}
		final UIJob job = new SLUIJob() {
			@Override
			public IStatus runInUIThread(final IProgressMonitor monitor) {
				fillTableContents();
				return Status.OK_STATUS;
			}
		};
		job.schedule();
		return Status.OK_STATUS;
	}

	/**
	 * Must be called from a database job.
	 */
	private IStatus queryFindingTypeHTMLDescriptionOf(
			final String findingTypeUUID) {
		try {
			final Connection c = Data.getInstance().readOnlyConnection();
			try {
				final Statement st = c.createStatement();
				try {
					final List<ArtifactTypeRow> artifactList = new ArrayList<ArtifactTypeRow>();
					final ResultSet ars = st.executeQuery(QB.get(3,
							findingTypeUUID));
					try {
						while (ars.next()) {
							final ArtifactTypeRow row = new ArtifactTypeRow();
							row.toolName = ars.getString(1);
							row.mnemonic = ars.getString(2);
							row.link = ars.getString(3);
							row.categoryName = ars.getString(4);
							artifactList.add(row);
						}
					} finally {
						ars.close();
					}
					final ResultSet rs = st.executeQuery(QB.get(4,
							findingTypeUUID));
					try {
						while (rs.next()) {
							final String description = rs.getString(1);
							final UIJob job = new SLUIJob() {
								@Override
								public IStatus runInUIThread(
										final IProgressMonitor monitor) {
									setHTMLDescription(description,
											artifactList);
									return Status.OK_STATUS;
								}
							};
							job.schedule();
						}
					} finally {
						rs.close();
					}
				} finally {
					st.close();
				}
			} finally {
				c.close();
			}
		} catch (final SQLException e) {
			final int errNo = 55;
			final String msg = I18N.err(errNo);
			return SLEclipseStatusUtility.createErrorStatus(errNo, msg, e);
		}
		return Status.OK_STATUS;
	}

	/**
	 * Must be called from the UI thread.
	 */
	private void fillTableContents() {
		/**
		 * Fix to bug 1355, we should have checked if the panel is still up.
		 */
		if (f_findingTypes.isDisposed()) {
			return;
		}
		for (final FindingTypeRow row : f_findingTypeList) {
			final TreeItem item = new TreeItem(f_findingTypes, SWT.NONE);
			item.setText(0, row.findingTypeName);
			item.setText(1, row.findingTypeDescription);
			item.setData(row.findingTypeId);
			item.setChecked(f_filterList.contains(row.findingTypeId));
		}
		getShell().layout();
	}

	private final Display f_display = PlatformUI.getWorkbench().getDisplay();

	private final RGB fBackgroundColorRGB = f_display.getSystemColor(
			SWT.COLOR_WIDGET_BACKGROUND).getRGB();

	/**
	 * Must be called from the UI thread.
	 */
	private void clearHTMLDescription() {
		setHTMLDescription("No finding type selected.", null);
	}

	/**
	 * Must be called from the UI thread.
	 */
	private void setHTMLDescription(final String description,
			final List<ArtifactTypeRow> artifactList) {
		final StringBuffer b = new StringBuffer();
		HTMLPrinter.insertPageProlog(b, 0, fBackgroundColorRGB,
				StyleSheetHelper.getInstance().getStyleSheet());
		b.append(description);
		if ((artifactList != null) && !artifactList.isEmpty()) {
			b.append("<br><br>");
			b.append("<center>Reporting Tool Information<table border=1>");
			b.append("<tr>");
			b.append("<th>Tool</th>");
			b.append("<th>Category</th>");
			b.append("<th>Rule</th>");
			b.append("</tr>");
			for (final ArtifactTypeRow row : artifactList) {
				b.append("<tr>");
				b.append("<td>").append(row.toolName).append("</td>");
				b.append("<td>").append(row.categoryName).append("</td>");
				b.append("<td>");
				if ((row.link != null) && !"".equals(row.link.trim())) {
					b.append("<A HREF=\"").append(row.link).append("\">");
					b.append(row.mnemonic);
					b.append("</A>");
				} else if (row.toolName.equalsIgnoreCase("FindBugs")) {
					b.append("<A HREF=\"http://findbugs.sourceforge.net/");
					b.append("bugDescriptions.html#");
					b.append(row.mnemonic).append("\">");
					b.append(row.mnemonic);
					b.append("</A>");
				} else {
					b.append(row.mnemonic);
				}
				b.append("</td>");
				b.append("</tr>");
			}
			b.append("</table></center>");
		}
		if (f_detailsText != null) {
			f_detailsText.setText(b.toString());
		}
	}

	/**
	 * Must be called from a database job.
	 */
	private IStatus updateSettings(final List<String> filterUUIDList) {
		try {
			Data.getInstance().withTransaction(
					SettingQueries.updateDefaultScanFilter(filterUUIDList));
		} catch (final TransactionException e) {
			final int errNo = 56;
			final String msg = I18N.err(errNo);
			return SLEclipseStatusUtility.createErrorStatus(errNo, msg, e);
		}
		return Status.OK_STATUS;
	}

	public void init(final IWorkbench workbench) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("Use this page to select rules to include from the scan.");
	}

	private void setTypes(boolean state) {
		for (TreeItem item : f_findingTypes.getItems()) {
			setItemChecked(item, state);
		}
	}

	private void setItemChecked(TreeItem item, boolean state) {
		if (item == null) {
			return;
		}
		item.setChecked(state);

		if (item.getItems() == null) {
			return;
		}
		for (TreeItem sub : item.getItems()) {
			setItemChecked(sub, state);
		}
	}
}
