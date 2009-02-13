package com.surelogic.sierra.client.eclipse.wizards;

import java.io.File;
import java.util.logging.Level;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;

import com.surelogic.common.CommonImages;
import com.surelogic.common.eclipse.SLImages;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.client.eclipse.model.selection.SelectionManager;

public class FindingSearchImportPage extends AbstractImportWizardPage {

	private static final String IMPORT_SEARCH_WARNING = "Importing finding searches will override existing finding searches using the same name";

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
		warningImg.setImage(SLImages.getImage(CommonImages.IMG_WARNING));
		warningImg
				.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));

		final Link deselectWarning = new Link(warning, SWT.NONE);
		GridData data = new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1);
		data.widthHint = 300;
		deselectWarning.setLayoutData(data);
		deselectWarning.setText(IMPORT_SEARCH_WARNING);
	}

	@Override
	protected void layoutImportFileGroup(Composite importFileSelectionGroup) {
		GridLayout layout = new GridLayout(3, false);
		importFileSelectionGroup.setLayout(layout);
		importFileSelectionGroup.setLayoutData(new GridData(SWT.FILL, SWT.NONE,
				true, true));

		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		f_importFilenameField.setLayoutData(data);

		f_importFileBrowseButton.setLayoutData(new GridData(SWT.FILL, SWT.NONE,
				false, false));
	}

	public boolean importSearches() {
		File holder = new File(f_importFilenameField.getText());
		if (holder.exists() && !holder.isDirectory()) {
			try {
				SelectionManager.getInstance().load(holder);
				return true;
			} catch (Exception ex) {
				SLLogger.getLogger().log(
						Level.SEVERE,
						"Error when importing finding searches from the file "
								+ holder.getAbsolutePath(), ex);
			}
		}

		return false;
	}
}
