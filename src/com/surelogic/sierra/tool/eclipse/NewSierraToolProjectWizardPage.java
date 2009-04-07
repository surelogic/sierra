package com.surelogic.sierra.tool.eclipse;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;

public class NewSierraToolProjectWizardPage extends WizardPage {	
	// <A HREF=\"http://findbugs.sourceforge.net\">FindBugs</A> is
	private static final String INITIAL_DESCRIPTION = "A static analysis tool";
	private static final int ID = 0;
	private static final int VERSION = 1;
	private static final int NAME = 2;
	private static final int PROVIDER = 3;
	private static final int WEBSITE = 4;
	private static final String[] FIELD_LABELS = new String[] {
			"Tool Id:", 
			"Tool Version:", 
			"Tool Name:", 
			"Tool Provider:", 
			"Tool Website:"
	};

	final WizardNewProjectCreationPage namePage;	
	boolean firstTimeVisible = true;
	Text[] fields;
	Text description;

	private final Listener nameModifyListener = new Listener() {
		public void handleEvent(Event e) {
			boolean valid = validatePage();
			setPageComplete(valid);                
		}
	};

	protected NewSierraToolProjectWizardPage(WizardNewProjectCreationPage namePage) {
		super("NewSierraToolProjectWizardDetails");
		this.namePage = namePage;
	}

	@Override
	public void setVisible(boolean visible) {
		if (firstTimeVisible) {
			firstTimeVisible = false;
			initFields();
		}		
		super.setVisible(visible);
	}

	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		createToolPropertiesGroup(composite);
		createToolDescriptionGroup(composite);
		setPageComplete(validatePage());
		setControl(composite);
	}

	private boolean validatePage() {
		if (description.getText().length() == 0) {
			return false;
		}
		if (fields != null) {
			for(Text f : fields) {
				if (f.getText().length() == 0) {
					return false;
				}
			}
			return true;
		}

		return false;
	}

	private void createToolDescriptionGroup(Composite parent) {
		Composite projectGroup = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		projectGroup.setLayout(layout);
		projectGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
		projectGroup.setFont(parent.getFont());

		Label projectLabel = new Label(projectGroup, SWT.NONE);
		projectLabel.setText("Tool Description:");

		description = new Text(projectGroup, SWT.BORDER);
		GridData data = new GridData(GridData.FILL_BOTH);
		description.setLayoutData(data);
		description.setText(INITIAL_DESCRIPTION);
		description.addListener(SWT.Modify, nameModifyListener);
	}

	private void createToolPropertiesGroup(Composite parent) {
		// project specification group
		Composite projectGroup = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		projectGroup.setLayout(layout);
		projectGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		projectGroup.setFont(parent.getFont());

		fields = new Text[FIELD_LABELS.length];
		int i = 0;
		for(String label : FIELD_LABELS) {        
			// new project label
			Label projectLabel = new Label(projectGroup, SWT.NONE);
			projectLabel.setText(label);
			projectLabel.setFont(parent.getFont());

			// new project name entry field
			Text projectNameField = new Text(projectGroup, SWT.BORDER);

			GridData data = new GridData(GridData.FILL_HORIZONTAL);
			projectNameField.setLayoutData(data);
			projectNameField.setFont(parent.getFont());      
			fields[i] = projectNameField;
			i++;
		}
	}

	private void initFields() {
		// Set the initial value first before listener
		// to avoid handling an event during the initialization.
		final String dottedName = namePage.getProjectName().replace('-', '.');
		fields[0].setText("com.someone."+dottedName);
		fields[1].setText("1.0.0");

		for(Text f : fields) {
			f.addListener(SWT.Modify, nameModifyListener);  
		}
	}
	
	public String getToolId() {
		return fields[ID].getText();
	}
	
	public String getToolVersion() {
		return fields[VERSION].getText();
	}

	public String getToolName() {
		return fields[NAME].getText();
	}

	public String getToolProvider() {
		return fields[PROVIDER].getText();
	}

	public String getToolWebsite() {
		return fields[WEBSITE].getText();
	}

	public String getToolDescription() {
		return description.getText();
	}
}
