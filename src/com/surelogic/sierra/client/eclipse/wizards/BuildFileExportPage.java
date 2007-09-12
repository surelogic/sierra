package com.surelogic.sierra.client.eclipse.wizards;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import com.surelogic.sierra.client.eclipse.model.ConfigGenerator;
import com.surelogic.sierra.tool.analyzer.BuildFileGenerator;
import com.surelogic.sierra.tool.config.Config;

/**
 * The Sierra Build File Export page
 * 
 * @see org.eclipse.ant.internal.ui.datatransferAntBuildFileExportPage
 */
public class BuildFileExportPage extends WizardPage {

	private CheckboxTableViewer f_TableViewer;
	private List<IJavaProject> f_SelectedJavaProjects = new ArrayList<IJavaProject>();
	private Button f_overrideCheckbox;
	private Text f_buildfilenameText;

	public BuildFileExportPage() {
		super("SierraBuildfileExportWizardPage"); //$NON-NLS-1$
		setPageComplete(false);
		setTitle("Generate Sierra Buildfiles");
		setDescription("Generates Sierra buildfiles for the selected Java projects");
	}

	/*
	 * @see IDialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent) {

		initializeDialogUnits(parent);

		Composite workArea = new Composite(parent, SWT.NONE);
		setControl(workArea);

		workArea.setLayout(new GridLayout());
		workArea.setLayoutData(new GridData(GridData.FILL_BOTH
				| GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL));

		Label titel = new Label(workArea, SWT.NONE);
		titel
				.setText("Select the projects to use to generate the Sierra buildfiles:");

		Composite listComposite = new Composite(workArea, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginWidth = 0;
		layout.makeColumnsEqualWidth = false;
		listComposite.setLayout(layout);

		listComposite.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
				| GridData.GRAB_VERTICAL | GridData.FILL_BOTH));

		Table table = new Table(listComposite, SWT.CHECK | SWT.BORDER
				| SWT.V_SCROLL | SWT.H_SCROLL);
		f_TableViewer = new CheckboxTableViewer(table);
		table.setLayout(new TableLayout());
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		data.heightHint = 300;
		table.setLayoutData(data);
		f_TableViewer.setContentProvider(new WorkbenchContentProvider() {
			@Override
			public Object[] getElements(Object element) {
				if (element instanceof IJavaProject[]) {
					return (IJavaProject[]) element;
				}
				return null;
			}
		});
		f_TableViewer.setLabelProvider(new WorkbenchLabelProvider());
		f_TableViewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				if (event.getElement() instanceof IJavaProject) {
					IJavaProject holder = (IJavaProject) event.getElement();
					if (event.getChecked()) {
						f_SelectedJavaProjects.add(holder);

					} else {
						f_SelectedJavaProjects.remove(holder);
					}
					updateEnablement();
				}
			}
		});

		initializeProjects();
		createSelectionButtons(listComposite);
		createCheckboxes(workArea);
		createTextFields(workArea);
		setControl(workArea);
		updateEnablement();
		Dialog.applyDialogFont(parent);
	}

	private void createSelectionButtons(Composite composite) {

		Composite buttonsComposite = new Composite(composite, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		buttonsComposite.setLayout(layout);

		buttonsComposite.setLayoutData(new GridData(
				GridData.VERTICAL_ALIGN_BEGINNING));

		Button selectAll = new Button(buttonsComposite, SWT.PUSH);
		selectAll.setText("Select All");
		selectAll.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				for (int i = 0; i < f_TableViewer.getTable().getItemCount(); i++) {
					if (f_TableViewer.getElementAt(i) instanceof IJavaProject) {
						IJavaProject holder = (IJavaProject) f_TableViewer
								.getElementAt(i);
						f_SelectedJavaProjects.add(holder);
					}
				}
				f_TableViewer.setAllChecked(true);
				updateEnablement();
			}
		});
		setButtonLayoutData(selectAll);

		Button deselectAll = new Button(buttonsComposite, SWT.PUSH);
		deselectAll.setText("Deselect All");
		deselectAll.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				f_SelectedJavaProjects.clear();
				f_TableViewer.setAllChecked(false);
				updateEnablement();
			}
		});
		setButtonLayoutData(deselectAll);
	}

	private void createCheckboxes(Composite composite) {

		f_overrideCheckbox = new Button(composite, SWT.CHECK);
		f_overrideCheckbox.setSelection(false);
		f_overrideCheckbox.setText("Override existing Sierra buildfile(s)");
	}

	private void createTextFields(Composite composite) {

		// buildfilename
		Composite containerGroup = new Composite(composite, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		containerGroup.setLayout(layout);
		containerGroup.setLayoutData(new GridData(
				GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));

		// label
		Label buildfilenameLabel = new Label(containerGroup, SWT.NONE);
		buildfilenameLabel.setText("Name for the Sierra Buildfile:");

		// text field
		f_buildfilenameText = new Text(containerGroup, SWT.SINGLE | SWT.BORDER);
		f_buildfilenameText.setText("sierra.xml"); //$NON-NLS-1$
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL
				| GridData.GRAB_HORIZONTAL);
		f_buildfilenameText.setLayoutData(data);

		ModifyListener listener = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updateEnablement();
			}
		};
		f_buildfilenameText.addModifyListener(listener);
	}

	private void initializeProjects() {
		IWorkspaceRoot rootWorkspace = ResourcesPlugin.getWorkspace().getRoot();
		IJavaModel javaModel = JavaCore.create(rootWorkspace);
		IJavaProject[] javaProjects;
		try {
			javaProjects = javaModel.getJavaProjects();
		} catch (JavaModelException e) {
			javaProjects = new IJavaProject[0];
		}
		f_TableViewer.setInput(javaProjects);
		// Check any necessary projects
		if (f_SelectedJavaProjects != null) {
			f_TableViewer.setCheckedElements(f_SelectedJavaProjects
					.toArray(new IJavaProject[f_SelectedJavaProjects.size()]));
		}
	}

	private void updateEnablement() {
		boolean complete = true;

		// TODO: Implement file name check. Filenames with invalid characters
		// are still permitted

		if (f_SelectedJavaProjects.size() == 0) {
			setErrorMessage("Project name must be specified");
			complete = false;
		}
		if (f_buildfilenameText.getText().length() == 0) {
			setErrorMessage("Buildfile name is required");
			complete = false;
		}
		if (complete) {
			setErrorMessage(null);
		}
		setPageComplete(complete);
	}

	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible) {
			f_TableViewer.getTable().setFocus();
		}
	}

	protected void setSelectedProjects(
			List<IJavaProject> JavaProjselectedJavaProjectsects) {
		f_SelectedJavaProjects.addAll(JavaProjselectedJavaProjectsects);
	}

	/**
	 * Generate the build files.
	 */
	public boolean generateBuildfiles() {
		List<Config> configs = ConfigGenerator.getInstance().getConfigs(
				f_SelectedJavaProjects);
		BuildFileGenerator.getInstance().writeBuildFiles(configs,
				f_overrideCheckbox.getSelection());

		return true;
	}
}
