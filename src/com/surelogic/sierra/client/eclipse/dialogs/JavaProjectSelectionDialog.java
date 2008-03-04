package com.surelogic.sierra.client.eclipse.dialogs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.progress.UIJob;

import com.surelogic.common.eclipse.SLImages;
import com.surelogic.common.eclipse.jobs.SLUIJob;
import com.surelogic.sierra.client.eclipse.preferences.PreferenceConstants;

public final class JavaProjectSelectionDialog extends Dialog {

	private final String f_label;
	private final String f_shellTitle;
	private final Image f_shellImage;

	private final List<IJavaProject> f_javaProjects;
	private Table f_projectTable;
	/**
	 * Aliased and visible to the static call
	 * {@link #getProjects(String, String, Image, List)}.
	 */
	private final List<IJavaProject> f_selectedProjects;

	public static List<IJavaProject> getProjects(final String label,
			final String shellTitle, final Image shellImage,
			final List<IJavaProject> javaProjects) {
		if (javaProjects.isEmpty()
				|| PreferenceConstants.alwaysAllowUserToSelectProjectsToScan()) {
			final List<IJavaProject> mutableProjectList = new ArrayList<IJavaProject>();
			// Copied from AbstractScan
			UIJob job = new SLUIJob() {
				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					final Shell shell = PlatformUI.getWorkbench()
							.getActiveWorkbenchWindow().getShell();
					final JavaProjectSelectionDialog dialog = new JavaProjectSelectionDialog(
							shell, label, shellTitle, shellImage, javaProjects,
							mutableProjectList);

					if (dialog.open() == Window.CANCEL) {
						return Status.CANCEL_STATUS;
					}
					return Status.OK_STATUS;
				}
			};
			IStatus status = job.runInUIThread(null);
			if (status == Status.CANCEL_STATUS) {
				return Collections.emptyList();
			}
			return mutableProjectList;
		}
		return javaProjects;
	}

	private JavaProjectSelectionDialog(Shell parentShell, String label,
			String shellTitle, Image shellImage,
			List<IJavaProject> javaProjects,
			List<IJavaProject> mutableProjectList) {
		super(parentShell);
		this.f_label = label;
		setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX);
		f_shellTitle = shellTitle;
		f_shellImage = shellImage;
		f_javaProjects = javaProjects;
		f_selectedProjects = mutableProjectList;
	}

	@Override
	protected final void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setImage(f_shellImage);
		newShell.setText(f_shellTitle);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite panel = (Composite) super.createDialogArea(parent);
		GridLayout gridLayout = new GridLayout();
		panel.setLayout(gridLayout);

		final Label label = new Label(panel, SWT.WRAP);
		label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
		label.setText(f_label);

		f_projectTable = new Table(panel, SWT.FULL_SELECTION | SWT.CHECK);
		f_projectTable.setLayoutData(new GridData(GridData.FILL_BOTH));

		try {
			final IWorkspaceRoot root = ResourcesPlugin.getWorkspace()
					.getRoot();
			final IJavaModel javaModel = JavaCore.create(root);
			for (IJavaProject jp : javaModel.getJavaProjects()) {
				TableItem item = new TableItem(f_projectTable, SWT.NONE);
				item.setText(jp.getElementName());
				item.setImage(SLImages
						.getWorkbenchImage(IDE.SharedImages.IMG_OBJ_PROJECT));
				item.setData(jp);
				if (f_javaProjects.contains(jp)) {
					item.setChecked(true);
					f_selectedProjects.add(jp);
				}
			}
		} catch (JavaModelException e) {
			throw new RuntimeException(e);
		}

		f_projectTable.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				setOKState();
			}
		});

		final Composite allNonePanel = new Composite(panel, SWT.NONE);
		allNonePanel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
				false));
		final GridLayout allNoneLayout = new GridLayout();
		allNoneLayout.numColumns = 2;
		allNoneLayout.makeColumnsEqualWidth = true;
		allNonePanel.setLayout(allNoneLayout);
		final Button allButton = new Button(allNonePanel, SWT.PUSH);
		allButton.setText("Select All");
		allButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
		allButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				if (f_projectTable != null && !f_projectTable.isDisposed()) {
					for (TableItem item : f_projectTable.getItems()) {
						item.setChecked(true);
					}
					setOKState();
				}
			}
		});
		final Button noneButton = new Button(allNonePanel, SWT.PUSH);
		noneButton.setText("Deselect All");
		noneButton
				.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
		noneButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				if (f_projectTable != null && !f_projectTable.isDisposed()) {
					for (TableItem item : f_projectTable.getItems()) {
						item.setChecked(false);
					}
					setOKState();
				}
			}
		});

		final Button check = new Button(panel, SWT.CHECK);
		check.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
		check
				.setText("Show this dialog even when projects are selected in the Package Explorer");
		check.setSelection(PreferenceConstants
				.alwaysAllowUserToSelectProjectsToScan());
		check.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				boolean show = !PreferenceConstants
						.alwaysAllowUserToSelectProjectsToScan();
				PreferenceConstants
						.setAlwaysAllowUserToSelectProjectsToScan(show);
				check.setSelection(show);
			}
		});

		return panel;
	}

	@Override
	protected final Control createContents(Composite parent) {
		final Control contents = super.createContents(parent);
		setOKState();
		return contents;
	}

	private final void setOKState() {
		/*
		 * Remember what is checked.
		 */
		f_selectedProjects.clear();
		if (f_projectTable != null && !f_projectTable.isDisposed()) {
			for (TableItem item : f_projectTable.getItems()) {
				if (item.getChecked()) {
					f_selectedProjects.add((IJavaProject) item.getData());
				}
			}
			/*
			 * Set the state of the OK button.
			 */
			getButton(IDialogConstants.OK_ID).setEnabled(
					!f_selectedProjects.isEmpty());
		}
	}
}
