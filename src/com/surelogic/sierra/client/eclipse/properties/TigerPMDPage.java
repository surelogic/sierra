package com.surelogic.sierra.client.eclipse.properties;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PropertyPage;

public class TigerPMDPage extends PropertyPage {

	private static final String OWNER_PROPERTY = "OWNER";

	private static final String DEFAULT_OWNER = "John Doe";

	private Text ownerText;

	/**
	 * Constructor for SamplePropertyPage.
	 */
	public TigerPMDPage() {
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
		data.grabExcessHorizontalSpace = true;
		composite.setLayoutData(data);

		return composite;
	}

	@Override
	protected void performDefaults() {
		// Populate the owner text field with the default value
		ownerText.setText(DEFAULT_OWNER);
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

}