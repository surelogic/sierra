package com.surelogic.sierra.client.eclipse.preferences;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.progress.UIJob;

import com.surelogic.common.eclipse.SLImages;
import com.surelogic.common.eclipse.job.SLUIJob;
import com.surelogic.sierra.client.eclipse.Activator;
import com.surelogic.sierra.client.eclipse.jobs.DeleteProjectDataJob;
import com.surelogic.sierra.client.eclipse.model.IProjectsObserver;
import com.surelogic.sierra.client.eclipse.model.Projects;
import com.surelogic.sierra.tool.message.Importance;

public class SierraPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage {

	BooleanFieldEditor f_balloonFlag;
	BooleanFieldEditor f_showMarkersInJavaEditorFlag;
	RadioGroupFieldEditor f_showAbove;
	BooleanFieldEditor f_saveResources;
	IntegerFieldEditor f_findingsListLimit;

	public void init(IWorkbench workbench) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("Use this page to customize Sierra.");
	}

	@Override
	protected Control createContents(Composite parent) {
		final Composite panel = new Composite(parent, SWT.NONE);
		GridLayout grid = new GridLayout();
		panel.setLayout(grid);

		final Group diGroup = new Group(panel, SWT.NONE);
		diGroup.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		diGroup.setText("Appearance");

		f_findingsListLimit = new IntegerFieldEditor(
				PreferenceConstants.P_FINDINGS_LIST_LIMIT,
				"Maximum number of findings shown in 'Findings Quick Search' results:",
				diGroup);
		f_findingsListLimit.setPage(this);
		f_findingsListLimit.setPreferenceStore(getPreferenceStore());
		f_findingsListLimit.load();

		f_balloonFlag = new BooleanFieldEditor(
				PreferenceConstants.P_SIERRA_BALLOON_FLAG,
				"Show 'balloon' notifications for scan start and completion.",
				diGroup);
		f_balloonFlag.setPage(this);
		f_balloonFlag.setPreferenceStore(getPreferenceStore());
		f_balloonFlag.load();

		f_showMarkersInJavaEditorFlag = new BooleanFieldEditor(
				PreferenceConstants.P_SIERRA_SHOW_MARKERS,
				"Show markers for findings in the Java editor.", diGroup);
		f_showMarkersInJavaEditorFlag.setPage(this);
		f_showMarkersInJavaEditorFlag.setPreferenceStore(getPreferenceStore());
		f_showMarkersInJavaEditorFlag.load();

		f_showAbove = new RadioGroupFieldEditor(
				PreferenceConstants.P_SIERRA_SHOW_MARKERS_AT_OR_ABOVE_IMPORTANCE,
				"Only show markers for findings in the Java editor at or above",
				1, new String[][] {
						{ Importance.CRITICAL.toStringSentenceCase(),
								Importance.CRITICAL.toString() },
						{ Importance.HIGH.toStringSentenceCase(),
								Importance.HIGH.toString() },
						{ Importance.MEDIUM.toStringSentenceCase(),
								Importance.MEDIUM.toString() },
						{ Importance.LOW.toStringSentenceCase(),
								Importance.LOW.toString() },
						{ Importance.IRRELEVANT.toStringSentenceCase(),
								Importance.IRRELEVANT.toString() } },

				diGroup);
		f_showAbove.setPage(this);
		f_showAbove.setPreferenceStore(getPreferenceStore());
		f_showAbove.load();

		f_saveResources = new BooleanFieldEditor(
				PreferenceConstants.P_SIERRA_ALWAYS_SAVE_RESOURCES,
				"Save all modified resources automatically prior to a scan.",
				diGroup);
		f_saveResources.setPage(this);
		f_saveResources.setPreferenceStore(getPreferenceStore());
		f_saveResources.load();

		final Group pGroup = new Group(panel, SWT.NONE);
		pGroup.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		pGroup.setText("Scanned Projects");
		grid = new GridLayout();
		grid.numColumns = 2;
		pGroup.setLayout(grid);

		final Label l = new Label(pGroup, SWT.WRAP);
		StringBuilder b = new StringBuilder();
		b.append("The following projects have stored Sierra data.");
		b.append(" You may want to delete Sierra data about any projects you");
		b.append(" no longer work on to conserve resources on your machine.");
		b.append(" Deleting all stored Sierra data requires Eclipse to");
		b.append(" be restarted. ");
		l.setText(b.toString());
		GridData data = new GridData();
		data.widthHint = 400;
		data.horizontalSpan = 2;
		l.setLayoutData(data);

		final Table t = new Table(pGroup, SWT.FULL_SELECTION | SWT.MULTI);
		data = new GridData(SWT.FILL, SWT.FILL, true, true);
		data.heightHint = 150;
		t.setLayoutData(data);

		final Composite c = new Composite(pGroup, SWT.NONE);
		RowLayout rl = new RowLayout(SWT.VERTICAL);
		rl.fill = true;
		c.setLayout(rl);
		c.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, false, false));

		final Button deleteSelectedProjectData = new Button(c, SWT.PUSH);
		deleteSelectedProjectData.setText("Delete Sierra Data");
		deleteSelectedProjectData.setEnabled(false);

		final Button deleteDatabase = new Button(c, SWT.PUSH);
		deleteDatabase.setText("Delete All Sierra Data");
		deleteDatabase.setEnabled(!PreferenceConstants
				.deleteDatabaseOnStartup());

		(new Mediator(t, deleteSelectedProjectData, deleteDatabase)).init();

		return panel;
	}

	@Override
	protected void performDefaults() {
		f_balloonFlag.loadDefault();
		f_showMarkersInJavaEditorFlag.loadDefault();
		f_showAbove.loadDefault();
		f_saveResources.loadDefault();
		f_findingsListLimit.loadDefault();
		super.performDefaults();
	}

	@Override
	public boolean performOk() {
		f_balloonFlag.store();
		f_showMarkersInJavaEditorFlag.store();
		f_showAbove.store();
		f_saveResources.store();
		f_findingsListLimit.store();
		return super.performOk();
	}

	private static class Mediator {

		private final Table f_projectTable;
		private final Button f_deleteSelectedProjects;
		private final Button f_deleteDatabase;

		Mediator(Table projectTable, Button deleteSelectedProjects,
				Button deleteDatabase) {
			f_projectTable = projectTable;
			f_deleteSelectedProjects = deleteSelectedProjects;
			f_deleteDatabase = deleteDatabase;
		}

		void init() {
			final IProjectsObserver obs = new IProjectsObserver() {
				public void notify(final Projects p) {
					// Get into a UI thread!
					final UIJob job = new SLUIJob() {
						@Override
						public IStatus runInUIThread(IProgressMonitor monitor) {
							setTableContents(p.getProjectNames());
							return Status.OK_STATUS;
						}
					};
					job.schedule();
				}
			};
			Projects.getInstance().addObserver(obs);
			// fill table of projects for the first time
			obs.notify(Projects.getInstance());

			f_projectTable.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
					f_deleteSelectedProjects.setEnabled(f_projectTable
							.getSelectionCount() != 0);
				}
			});

			f_deleteSelectedProjects.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
					TableItem[] selection = f_projectTable.getSelection();
					final List<String> projectNames = new ArrayList<String>();
					for (TableItem item : selection) {
						final String projectName = item.getText();
						projectNames.add(projectName);
					}
					if (projectNames.size() == 0)
						return;

					DeleteProjectDataJob.utility(projectNames, f_projectTable
							.getShell(), false);
				}
			});

			f_deleteDatabase.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
					final StringBuilder b = new StringBuilder();
					b.append("Are you sure you want to delete all ");
					b.append("Sierra data in your Eclipse workspace?\n\n");
					b.append("This action will not take effect until");
					b.append(" you restart Eclipse.\n");
					b.append("This action will not ");
					b.append("change or delete data on any Sierra server.");
					if (!MessageDialog.openConfirm(f_projectTable.getShell(),
							"Confirm Sierra Data Deletion", b.toString())) {
						return; // bail
					}
					f_deleteDatabase.setEnabled(false);
					PreferenceConstants.setDeleteDatabaseOnStartup(true);
				}
			});
		}

		void setTableContents(List<String> projectNames) {
			if (!f_projectTable.isDisposed()) {
				f_projectTable.removeAll();
				for (String projectName : projectNames) {
					TableItem item = new TableItem(f_projectTable, SWT.NULL);
					item.setText(projectName);
					/*
					 * TODO: Fix to use ILabelDecorator to look like a Java
					 * project.
					 */
					item
							.setImage(SLImages
									.getWorkbenchImage(IDE.SharedImages.IMG_OBJ_PROJECT));
				}
			}
		}
	}
}