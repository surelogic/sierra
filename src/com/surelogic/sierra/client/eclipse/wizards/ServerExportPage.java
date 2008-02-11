package com.surelogic.sierra.client.eclipse.wizards;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;

import com.surelogic.common.eclipse.SLImages;
import com.surelogic.sierra.client.eclipse.model.SierraServer;
import com.surelogic.sierra.client.eclipse.model.SierraServerManager;
import com.surelogic.sierra.client.eclipse.model.SierraServerPersistence;

public class ServerExportPage extends WizardPage {

	private CheckboxTableViewer f_TableViewer;
	private final List<SierraServer> f_SelectedSierraServers = new ArrayList<SierraServer>();
	private Text f_exportFilenameText;

	public ServerExportPage() {
		super("SierraServerExportWizardPage"); //$NON-NLS-1$
		setPageComplete(false);
		setTitle("Export Sierra Team Server Locations");
		setDescription("Export the selected Sierra Team Server locations");
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
		titel.setText("Select the Sierra Team Server locations to export:");

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
		f_TableViewer.setContentProvider(new SierraServerContentProvider());
		f_TableViewer.setLabelProvider(new SierraServerLabelProvider());
		f_TableViewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				if (event.getElement() instanceof SierraServer) {
					SierraServer holder = (SierraServer) event.getElement();
					if (event.getChecked()) {
						f_SelectedSierraServers.add(holder);

					} else {
						f_SelectedSierraServers.remove(holder);
					}
					updateEnablement();
				}
			}
		});

		initializeProjects();
		createSelectionButtons(listComposite);
		createTextFields(workArea);
		setControl(workArea);
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
					if (f_TableViewer.getElementAt(i) instanceof SierraServer) {
						SierraServer holder = (SierraServer) f_TableViewer
								.getElementAt(i);
						f_SelectedSierraServers.add(holder);
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
				f_SelectedSierraServers.clear();
				f_TableViewer.setAllChecked(false);
				updateEnablement();
			}
		});
		setButtonLayoutData(deselectAll);
	}

	private void createTextFields(Composite composite) {

		// Server file name
		Composite containerGroup = new Composite(composite, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		containerGroup.setLayout(layout);
		containerGroup.setLayoutData(new GridData(
				GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));

		// Label
		Label buildfilenameLabel = new Label(containerGroup, SWT.NONE);
		buildfilenameLabel.setText("Export file:");

		f_exportFilenameText = new Text(containerGroup, SWT.SINGLE | SWT.BORDER);
		f_exportFilenameText.setText(System.getProperty("user.home")
				+ System.getProperty("file.separator")
				+ "sierra-team-server-locations.xml");
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL
				| GridData.GRAB_HORIZONTAL);
		f_exportFilenameText.setLayoutData(data);

		final Button browseButton = new Button(containerGroup, SWT.PUSH);
		browseButton.setText("Browse...");
		browseButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false,
				false));
		browseButton.addListener(SWT.Selection, new Listener() {
			private FileDialog fd;

			public void handleEvent(Event event) {
				if (fd == null) {
					fd = new FileDialog(getShell(), SWT.SAVE);
					fd.setText("Destination File");
					fd.setFilterExtensions(new String[] { "*.xml", "*.*" });
					fd.setFilterNames(new String[] { "XML Files (*.xml)",
							"All Files (*.*)" });
				}
				final String fileName = f_exportFilenameText.getText();
				int i = fileName.lastIndexOf(System
						.getProperty("file.separator"));
				if (i != -1) {
					final String path = fileName.substring(0, i);
					fd.setFilterPath(path);
					if (i + 1 < fileName.length()) {
						final String file = fileName.substring(i + 1);
						fd.setFileName(file);
					}
				}
				final String selectedFilename = fd.open();
				if (selectedFilename != null) {
					f_exportFilenameText.setText(selectedFilename);
				}
			}
		});

		ModifyListener listener = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updateEnablement();
			}
		};
		f_exportFilenameText.addModifyListener(listener);
	}

	private void initializeProjects() {
		Set<SierraServer> servers = SierraServerManager.getInstance()
				.getServers();

		f_TableViewer.setInput(servers);
		// Check any necessary projects
		if (f_SelectedSierraServers != null) {
			f_TableViewer.setCheckedElements(f_SelectedSierraServers
					.toArray(new IJavaProject[f_SelectedSierraServers.size()]));
		}
	}

	private void updateEnablement() {
		boolean complete = true;

		// TODO: Implement file name check. Filenames with invalid characters
		// are still permitted

		if (f_SelectedSierraServers.isEmpty()) {
			setErrorMessage("At least one Sierra Team Server location must be selected");
			complete = false;
		}
		if (f_exportFilenameText.getText().length() == 0) {
			setErrorMessage("Export file name is required");
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

	public boolean exportServers() {
		SierraServerPersistence.export(SierraServerManager.getInstance(),
				f_SelectedSierraServers, new File(f_exportFilenameText
						.getText()));
		return true;
	}

	private static class SierraServerContentProvider implements
			IStructuredContentProvider {

		public Object[] getElements(Object inputElement) {
			if (inputElement instanceof Set) {
				Set<?> servers = (Set<?>) inputElement;
				return servers.toArray();
			}
			return null;
		}

		public void dispose() {
			// Nothing to do
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			// Nothing to do
		}
	}

	private static class SierraServerLabelProvider implements ILabelProvider {

		public Image getImage(Object element) {
			if (element instanceof SierraServer) {
				return SLImages.getImage(SLImages.IMG_SIERRA_SERVER);
			}
			return null;
		}

		public String getText(Object element) {
			if (element instanceof SierraServer) {
				SierraServer holder = (SierraServer) element;
				return holder.getLabel();
			}
			return null;
		}

		public void addListener(ILabelProviderListener listener) {
			// Nothing to do
		}

		public void dispose() {
			// Nothing to do
		}

		public boolean isLabelProperty(Object element, String property) {
			// Nothing to do
			return false;
		}

		public void removeListener(ILabelProviderListener listener) {
			// Nothing to do
		}
	}
}
