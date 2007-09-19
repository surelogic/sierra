package com.surelogic.sierra.client.eclipse.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.surelogic.sierra.client.eclipse.Activator;

public class ToolsPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage {

	private BooleanFieldEditor f_runFindbugsFlag;
	private BooleanFieldEditor f_runPMDFlag;
	private BooleanFieldEditor f_runReckonerFlag;
	private BooleanFieldEditor f_runCheckStyleFlag;

	@Override
	protected Control createContents(Composite parent) {
		final Composite panel = new Composite(parent, SWT.NONE);

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;

		panel.setLayout(gridLayout);
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, false);
		panel.setLayoutData(data);

		final Group toolsGroup = new Group(panel, SWT.NONE);
		toolsGroup.setText("Tools");
		toolsGroup.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false,
				1, 1));
		gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		toolsGroup.setLayout(gridLayout);

		data = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		data.widthHint = 500;

		f_runFindbugsFlag = new BooleanFieldEditor(
				PreferenceConstants.P_RUN_FINDBUGS, "FindBugs", toolsGroup);
		f_runFindbugsFlag.setPage(this);
		f_runFindbugsFlag.setPreferenceStore(getPreferenceStore());
		f_runFindbugsFlag.load();

		f_runPMDFlag = new BooleanFieldEditor(PreferenceConstants.P_RUN_PMD,
				"PMD", toolsGroup);
		f_runPMDFlag.setPage(this);
		f_runPMDFlag.setPreferenceStore(getPreferenceStore());
		f_runPMDFlag.load();

		f_runReckonerFlag = new BooleanFieldEditor(
				PreferenceConstants.P_RUN_RECKONER,
				"Reckoner (Sierra Metrics)", toolsGroup);
		f_runReckonerFlag.setPage(this);
		f_runReckonerFlag.setPreferenceStore(getPreferenceStore());
		f_runReckonerFlag.load();

		f_runCheckStyleFlag = new BooleanFieldEditor(
				PreferenceConstants.P_RUN_CHECKSTYLE, "CheckStyle", toolsGroup);
		f_runCheckStyleFlag.setPage(this);
		f_runCheckStyleFlag.setPreferenceStore(getPreferenceStore());
		f_runCheckStyleFlag.load();

		return panel;

	}

	public void init(IWorkbench workbench) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("Use this page to select the tools to be included in scan");
	}

	@Override
	protected void performApply() {
		f_runFindbugsFlag.store();
		f_runCheckStyleFlag.store();
		f_runPMDFlag.store();
		f_runReckonerFlag.store();
		super.performApply();
	}

	@Override
	protected void performDefaults() {
		f_runFindbugsFlag.loadDefault();
		f_runCheckStyleFlag.loadDefault();
		f_runPMDFlag.loadDefault();
		f_runReckonerFlag.loadDefault();
		super.performDefaults();
	}

	@Override
	public boolean performOk() {
		f_runFindbugsFlag.store();
		f_runCheckStyleFlag.store();
		f_runPMDFlag.store();
		f_runReckonerFlag.store();
		return super.performOk();
	}

}
