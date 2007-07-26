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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PropertyPage;

public class TigerFindBugsProperties extends PropertyPage {

	private static final String OWNER_PROPERTY = "OWNER";

	private static final String DEFAULT_OWNER = "John Doe";

	private Text ownerText;

	// private Table rules;

	/**
	 * Constructor for SamplePropertyPage.
	 */
	public TigerFindBugsProperties() {
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

		Label ruleSet = new Label(composite, SWT.NONE);
		ruleSet.setText("Select rules:");

		// rules = new Table(composite, SWT.CHECK | SWT.BORDER);

		addFBRules();

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

	private void addFBRules() {

		// TODO Replace this with DB call to get the set of rules
		// Definition sampleDefintion = new Definition();
		// sampleDefintion.setDescription("Some rule that i made up");

		// This will go in a loop
		// TableItem tab1 = new TableItem(rules, SWT.NONE);
		// tab1.setText(sampleDefintion.getDescription());

	}

}