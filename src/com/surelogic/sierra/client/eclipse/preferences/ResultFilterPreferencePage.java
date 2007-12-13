package com.surelogic.sierra.client.eclipse.preferences;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
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
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;

import com.surelogic.common.eclipse.HTMLPrinter;
import com.surelogic.common.eclipse.job.DatabaseJob;
import com.surelogic.common.eclipse.logging.SLStatus;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.client.eclipse.Activator;
import com.surelogic.sierra.client.eclipse.Data;
import com.surelogic.sierra.client.eclipse.StyleSheetHelper;
import com.surelogic.sierra.jdbc.settings.SettingsManager;
import com.surelogic.sierra.tool.message.FindingTypeFilter;

public class ResultFilterPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage {

	/**
	 * Returns the category name, the finding type name, a short message about
	 * the finding type, and the finding type identifier.
	 */
	private static final String QUERY = "select C.NAME, T.NAME, T.SHORT_MESSAGE, T.UUID from FINDING_TYPE T, FINDING_CATEGORY C where T.CATEGORY_ID = C.ID and T.ID in (select FINDING_TYPE_ID from ARTIFACT_TYPE where T.NAME != 'Checkstyle') order by 1,2,3";

	private static class FindingTypeRow {
		private String findingTypeUUID;
		private String categoryName;
		private String findingTypeName;
		private String findingTypeDescription;

		@Override
		public String toString() {
			return "[Finding type: id=" + findingTypeUUID + " name="
					+ findingTypeName + " description="
					+ findingTypeDescription + " category=" + categoryName
					+ "]";
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
	private String f_selectedFindingTypeUUID = null;

	@Override
	protected Control createContents(Composite parent) {
		f_panel = new Composite(parent, SWT.NONE);
		f_panel.setLayout(new GridLayout());

		GridData layoutData;
		f_findingTypes = new Tree(f_panel, SWT.CHECK | SWT.FULL_SELECTION);
		f_findingTypes.setHeaderVisible(true);
		f_findingTypes.setLinesVisible(true);
		layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		f_findingTypes.setLayoutData(layoutData);
		f_findingTypes.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				boolean clearHTMLDescription = true;
				/*
				 * Selection of an item (not the same as checking it).
				 */
				TreeItem[] items = f_findingTypes.getSelection();
				if (items.length > 0) {
					final Object rawData = items[0].getData();
					if (rawData instanceof String) {
						final String findingTypeUUID = (String) rawData;
						clearHTMLDescription = false;
						/*
						 * The below check avoids lots of extra queries since
						 * this event is invoked when the selection changes and
						 * when a check box is checked.
						 */
						if (!findingTypeUUID.equals(f_selectedFindingTypeUUID)) {
							f_selectedFindingTypeUUID = findingTypeUUID;
							Job job = new DatabaseJob(
									"Querying Sierra Artifact Type Description") {
								@Override
								protected IStatus run(IProgressMonitor monitor) {
									monitor
											.beginTask(
													"Querying Sierra Artifact Type Description",
													IProgressMonitor.UNKNOWN);
									return queryFindingTypeHTMLDescriptionOf(findingTypeUUID);
								}
							};
							job.schedule();
						}
					}
				}
				/*
				 * Check state of an item.
				 */
				if (event.item instanceof TreeItem) {
					TreeItem item = (TreeItem) event.item;
					/*
					 * Only categories have sub-items so pass along the change.
					 */
					for (TreeItem sub : item.getItems()) {
						sub.setChecked(item.getChecked());
					}
				}
				if (clearHTMLDescription)
					clearHTMLDescription();
				fixCategoryChecked();
			}
		});

		final TreeColumn treeColumn = new TreeColumn(f_findingTypes, SWT.NONE);
		treeColumn.setText("Category/Finding Type");
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
		} catch (SWTError e) {
			MessageDialog.openError(Display.getCurrent().getActiveShell(),
					"Browser Failure", "Browser cannot be initialized.");
		}
		clearHTMLDescription();

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

	private final List<FindingTypeRow> f_artifactList = new ArrayList<FindingTypeRow>();

	private final List<String> f_filterList = new ArrayList<String>();

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
						FindingTypeRow row = new FindingTypeRow();
						row.categoryName = rs.getString(1);
						row.findingTypeName = rs.getString(2);
						row.findingTypeDescription = rs.getString(3);
						row.findingTypeUUID = rs.getString(4);
						f_artifactList.add(row);
					}
					for (FindingTypeFilter filter : SettingsManager
							.getInstance(c).getGlobalSettings()) {
						/*
						 * The name is actually the UUID.
						 */
						f_filterList.add(filter.getName());
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
	 * Must be called from a database job.
	 */
	private IStatus queryFindingTypeHTMLDescriptionOf(
			final String findingTypeUUID) {
		try {
			final Connection c = Data.readOnlyConnection();
			try {
				final Statement st = c.createStatement();
				try {
					final List<ArtifactTypeRow> artifactList = new ArrayList<ArtifactTypeRow>();
					final String artifactQuery = "select distinct T.NAME, A.MNEMONIC, A.LINK, A.CATEGORY from FINDING_TYPE F, ARTIFACT_TYPE A, TOOL T where T.ID=A.TOOL_ID and F.ID=A.FINDING_TYPE_ID and F.UUID='"
							+ findingTypeUUID + "'";
					if (SLLogger.getLogger().isLoggable(Level.FINE)) {
						SLLogger.getLogger().fine(
								"Query of artifacts for findingType="
										+ findingTypeUUID + ": "
										+ artifactQuery);
					}
					final ResultSet ars = st.executeQuery(artifactQuery);
					while (ars.next()) {
						ArtifactTypeRow row = new ArtifactTypeRow();
						row.toolName = ars.getString(1);
						row.mnemonic = ars.getString(2);
						row.link = ars.getString(3);
						row.categoryName = ars.getString(4);
						artifactList.add(row);
					}
					final String query = "select INFO from FINDING_TYPE where UUID='"
							+ findingTypeUUID + "'";
					if (SLLogger.getLogger().isLoggable(Level.FINE)) {
						SLLogger.getLogger().fine(
								"Query of HTML info for findingType="
										+ findingTypeUUID + ": " + query);
					}
					final ResultSet rs = st.executeQuery(query);
					while (rs.next()) {
						final String description = rs.getString(1);
						PlatformUI.getWorkbench().getDisplay().asyncExec(
								new Runnable() {
									public void run() {
										setHTMLDescription(description,
												artifactList);
									}
								});
					}
				} finally {
					st.close();
				}
			} finally {
				c.close();
			}
		} catch (SQLException e) {
			return SLStatus.createErrorStatus(
					"Query of Sierra finding type description failed", e);
		}
		return Status.OK_STATUS;
	}

	/**
	 * Must be called from the UI thread.
	 */
	private void fillTableContents() {
		String currentCategoryName = null;
		TreeItem currentCategoryItem = null;
		for (FindingTypeRow row : f_artifactList) {
			if (!row.categoryName.equals(currentCategoryName)) {
				currentCategoryItem = new TreeItem(f_findingTypes, SWT.NONE);
				currentCategoryName = row.categoryName;
				currentCategoryItem.setText(0, row.categoryName);
			}
			final TreeItem item = new TreeItem(currentCategoryItem, SWT.NONE);
			item.setText(0, row.findingTypeName);
			item.setText(1, row.findingTypeDescription);
			item.setData(row.findingTypeUUID);
			item.setChecked(!f_filterList.contains(row.findingTypeUUID));
		}
		fixCategoryChecked();
		f_panel.getParent().layout();
	}

	/**
	 * Must be called from the UI thread.
	 */
	private void fixCategoryChecked() {
		for (TreeItem category : f_findingTypes.getItems()) {
			boolean noneSelected = true;
			boolean allSelected = true;
			for (TreeItem item : category.getItems()) {
				if (item.getChecked()) {
					noneSelected = false;
				} else {
					allSelected = false;
				}
			}
			category.setGrayed(!(allSelected || noneSelected));
			category.setChecked(!noneSelected);
		}
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
			List<ArtifactTypeRow> artifactList) {
		StringBuffer b = new StringBuffer();
		HTMLPrinter.insertPageProlog(b, 0, fBackgroundColorRGB,
				StyleSheetHelper.getInstance().getStyleSheet());
		b.append(description);
		if (artifactList != null && !artifactList.isEmpty()) {
			b.append("<br><br>");
			b.append("<center>Reporting Tool Information<table border=1>");
			b.append("<tr>");
			b.append("<th>Tool</th>");
			b.append("<th>Category</th>");
			b.append("<th>Rule</th>");
			b.append("</tr>");
			for (ArtifactTypeRow row : artifactList) {
				b.append("<tr>");
				b.append("<td>").append(row.toolName).append("</td>");
				b.append("<td>").append(row.categoryName).append("</td>");
				b.append("<td>");
				if (row.link != null && !"".equals(row.link.trim())) {
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
		f_detailsText.setText(b.toString());
	}

	public void init(IWorkbench workbench) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("Use this page to select rules to include/exclude from the scan.");
	}
}
