package com.surelogic.sierra.client.eclipse.properties;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PropertyPage;

/**
 * 
 * @author Tanmay.Sinha
 * 
 */
public class TigerPropertyPage extends PropertyPage {

	private static final String OWNER_PROPERTY = "OWNER";

	private Text ownerText;

	private Text resultDirectoryText;

	/**
	 * Constructor for TigerPropertyPage.
	 */
	public TigerPropertyPage() {
		super();
	}

	/**
	 * @see PreferencePage#createContents(Composite)
	 */
	@Override
	protected Control createContents(Composite parent) {

		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		composite.setLayout(layout);

		GridData data = new GridData(GridData.FILL);
		// data.grabExcessHorizontalSpace = true;

		composite.setLayoutData(data);

		Composite resultDirComposite = new Composite(composite, SWT.NULL);
		GridLayout gridLayout = new GridLayout(3, false);
		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 0;
		resultDirComposite.setLayout(gridLayout);
		resultDirComposite
				.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label dirResults = new Label(resultDirComposite, SWT.NULL);
		dirResults.setText("Select results directory: ");

		resultDirectoryText = new Text(resultDirComposite, SWT.BORDER);
		resultDirectoryText.setText("");
		resultDirectoryText.setEditable(false);
		resultDirectoryText
				.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Button browseResultDirectory = new Button(resultDirComposite, SWT.TRAIL);
		browseResultDirectory.setText("Browse...");
		browseResultDirectory.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog dialog = new DirectoryDialog(getShell());
				String dirSelected = dialog.open();

				if (dirSelected != null) {
					resultDirectoryText.setText(dirSelected);
				} else {
					setDefaultResultDir();
				}
			}

		});

		return composite;
	}

	protected void setDefaultResultDir() {
		resultDirectoryText.setText("C:\\Tiger\\results");

	}

	@Override
	protected void performDefaults() {
		// Nothing to do
	}

	@Override
	public boolean performOk() {
		// store the value in the owner text field
		try {
			((IResource) getElement()).setPersistentProperty(new QualifiedName(
					"", OWNER_PROPERTY), ownerText.getText());
		} catch (CoreException e) {
			return false;
		}
		return true;
	}

	@Override
	protected void performApply() {
		// Nothing to do
	}

}