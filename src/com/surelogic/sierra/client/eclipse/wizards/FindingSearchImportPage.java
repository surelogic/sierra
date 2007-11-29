package com.surelogic.sierra.client.eclipse.wizards;

import java.io.File;
import java.util.logging.Level;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISharedImages;

import com.surelogic.common.eclipse.SLImages;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.client.eclipse.model.selection.SelectionManager;

public class FindingSearchImportPage extends WizardPage {

	private static final String IMPORT_SEARCH_WARNING = "Importing finding searches will override existing finding searches using the same name";
	private Text f_importFilenameField;
	private Button f_importFileBrowseButton;
	private boolean f_invalidFile = true;

	public FindingSearchImportPage() {
		super("FindingSearchImportPage"); //$NON-NLS-1$
		setPageComplete(false);
		setTitle("Import Finding Searches");
		setDescription("Select a finding search file to import");
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

		createImportFileGroup(workArea);
		addWarning(workArea);
		setControl(workArea);
		Dialog.applyDialogFont(parent);
	}

	private void addWarning(Composite parent) {
		final Composite warning = new Composite(parent, SWT.NONE);
		warning.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false,
				2, 1));
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		warning.setLayout(gridLayout);
		final Label warningImg = new Label(warning, SWT.NONE);
		warningImg.setImage(SLImages
				.getWorkbenchImage(ISharedImages.IMG_OBJS_WARN_TSK));
		warningImg
				.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));

		final Link deselectWarning = new Link(warning, SWT.NONE);
		GridData data = new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1);
		data.widthHint = 300;
		deselectWarning.setLayoutData(data);
		deselectWarning.setText(IMPORT_SEARCH_WARNING);
	}

	private void updateEnablement() {
		boolean complete = true;

		// TODO: Implement file name check. Filenames with invalid characters
		// are still permitted

		if (f_invalidFile) {
			setErrorMessage("Invalid file");
			complete = false;
		}
		if (f_importFilenameField.getText().length() == 0) {
			setErrorMessage("Import file name is required");
			complete = false;
		}

		if (complete) {
			setErrorMessage(null);
		}

		setPageComplete(complete);
	}

	private void createImportFileGroup(Composite parent) {

		// import file selection group
		Composite importFileSelectionGroup = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(3, false);
		importFileSelectionGroup.setLayout(layout);
		importFileSelectionGroup.setLayoutData(new GridData(SWT.FILL, SWT.NONE,
				true, true));

		Label dest = new Label(importFileSelectionGroup, SWT.NONE);
		dest.setText("From file:");

		// import filename entry field
		f_importFilenameField = new Text(importFileSelectionGroup, SWT.SINGLE
				| SWT.BORDER);
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		f_importFilenameField.setLayoutData(data);

		// import file browse button
		f_importFileBrowseButton = new Button(importFileSelectionGroup,
				SWT.PUSH);
		f_importFileBrowseButton.setText("Browse");
		f_importFileBrowseButton.setLayoutData(new GridData(SWT.FILL, SWT.NONE,
				false, false));
		f_importFileBrowseButton.addListener(SWT.Selection, new Listener() {
			private FileDialog fd;

			public void handleEvent(Event event) {
				if (fd == null) {
					fd = new FileDialog(getShell(), SWT.OPEN);
					fd.setText("Destination File");
					fd.setFilterExtensions(new String[] { "*.xml", "*.*" });
					fd.setFilterNames(new String[] { "XML Files (*.xml)",
							"All Files (*.*)" });
				}

				final String selectedFilename = fd.open();
				if (selectedFilename != null) {
					f_importFilenameField.setText(selectedFilename);
				}
			}
		});

		new Label(parent, SWT.NONE); // vertical spacer

		ModifyListener listener = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				File holder = new File(f_importFilenameField.getText());

				if (holder.exists() && !holder.isDirectory()) {
					f_invalidFile = false;
				}
				updateEnablement();
			}
		};

		f_importFilenameField.addModifyListener(listener);
	}

	public boolean importSearches() {
		File holder = new File(f_importFilenameField.getText());
		if (holder.exists() && !holder.isDirectory()) {
			try {
				SelectionManager.getInstance().load(holder);
				return true;
			} catch (Exception ex) {
				SLLogger.getLogger("sierra").log(
						Level.SEVERE,
						"Error when importing finding searches from the file "
								+ holder.getAbsolutePath(), ex);
			}
		}

		return false;
	}
}
