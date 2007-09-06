package com.surelogic.sierra.client.eclipse.preferences;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

import com.surelogic.common.eclipse.SLImages;
import com.surelogic.sierra.client.eclipse.Activator;
import com.surelogic.sierra.client.eclipse.jobs.DeleteProjectDataJob;
import com.surelogic.sierra.client.eclipse.model.IProjectsObserver;
import com.surelogic.sierra.client.eclipse.model.Projects;

public class SierraPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage {

	DirectoryFieldEditor f_path;
	BooleanFieldEditor f_balloonFlag;

	public void init(IWorkbench workbench) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("Use this page to customize Sierra.");
	}

	@Override
	protected Control createContents(Composite parent) {
		final Composite panel = new Composite(parent, SWT.NONE);
		GridLayout grid = new GridLayout();
		panel.setLayout(grid);

		final Group soGroup = new Group(panel, SWT.NONE);
		soGroup.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		soGroup.setText("Scan Output");
		f_path = new DirectoryFieldEditor(PreferenceConstants.P_SIERRA_PATH,
				"Directory:", soGroup);
		f_path.setPage(this);
		f_path.setPreferenceStore(getPreferenceStore());
		f_path.load();

		final Group pGroup = new Group(panel, SWT.NONE);
		pGroup.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		pGroup.setText("Scanned Projects");
		grid = new GridLayout();
		grid.numColumns = 2;
		pGroup.setLayout(grid);

		Label l = new Label(pGroup, SWT.WRAP);
		l.setText("The following projects have stored Sierra data."
				+ " You may want to delete Sierra data about any projects you"
				+ " no longer work on to conserve resources on your machine.");
		GridData data = new GridData();
		data.widthHint = 400;
		data.horizontalSpan = 2;
		l.setLayoutData(data);

		Table t = new Table(pGroup, SWT.FULL_SELECTION | SWT.MULTI);
		data = new GridData(SWT.FILL, SWT.FILL, true, true);
		data.heightHint = 200;
		t.setLayoutData(data);

		Button b = new Button(pGroup, SWT.PUSH);
		b.setText("Delete Sierra Data");
		b.setEnabled(false);
		b.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, false, false));

		f_balloonFlag = new BooleanFieldEditor(
				PreferenceConstants.P_SIERRA_BALLOON_FLAG,
				"Show 'balloon' notifications for scan start and completion.",
				panel);
		f_balloonFlag.setPage(this);
		f_balloonFlag.setPreferenceStore(getPreferenceStore());
		f_balloonFlag.load();

		(new Mediator(t, b)).init();

		return panel;
	}

	@Override
	protected void performDefaults() {
		f_path.loadDefault();
		f_balloonFlag.loadDefault();
		super.performDefaults();
	}

	@Override
	public boolean performOk() {
		f_path.store();
		f_balloonFlag.store();
		return super.performOk();
	}

	private static class Mediator {

		private final Table f_projectTable;
		private final Button f_delete;

		Mediator(Table projectTable, Button delete) {
			f_projectTable = projectTable;
			f_delete = delete;
		}

		void init() {
			final IProjectsObserver obs = new IProjectsObserver() {
				public void notify(final Projects p) {
					// Get into a UI thread!
					PlatformUI.getWorkbench().getDisplay().asyncExec(
							new Runnable() {
								public void run() {
									setTableContents(p.getProjectNames());
								}
							});
				}
			};
			// TODO: this refresh is a HACK...remove
			Projects.getInstance().refresh();
			Projects.getInstance().addObserver(obs);
			// fill table
			obs.notify(Projects.getInstance());

			f_projectTable.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
					f_delete
							.setEnabled(f_projectTable.getSelectionCount() != 0);
				}
			});

			f_delete.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
					TableItem[] selection = f_projectTable.getSelection();
					final List<String> projectNames = new ArrayList<String>();
					for (TableItem item : selection) {
						final String projectName = item.getText();
						projectNames.add(projectName);
					}
					if (projectNames.size() == 0)
						return;

					final boolean multiDelete = projectNames.size() > 1;

					final MessageBox confirmDelete = new MessageBox(
							f_projectTable.getShell(), SWT.ICON_WARNING
									| SWT.APPLICATION_MODAL | SWT.YES | SWT.NO);
					confirmDelete.setText("Confirm "
							+ (multiDelete ? "Multiple Project" : "Project")
							+ " Sierra Data Deletion");
					confirmDelete
							.setMessage("Are you sure you want to delete all Sierra data for "

									+ (multiDelete ? "these "
											+ projectNames.size() + " projects"
											: "project '" + projectNames.get(0)
													+ "'") + "?");
					if (confirmDelete.open() == SWT.NO)
						return; // bail
					/*
					 * Because this job is run from a modal dialog we need to
					 * manage showing its progress ourselves. Therefore, this
					 * job is not a typical workspace job.
					 */
					final DeleteProjectDataJob job = new DeleteProjectDataJob(
							projectNames);
					job.runModal(f_projectTable.getShell());
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