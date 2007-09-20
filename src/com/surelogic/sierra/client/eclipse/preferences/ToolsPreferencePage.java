package com.surelogic.sierra.client.eclipse.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.surelogic.common.eclipse.SLImages;
import com.surelogic.sierra.client.eclipse.Activator;

public class ToolsPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage {

	private static final String DESELECT_TOOL_WARNING = "An unchecked a tool will not be run during any scan invoked within this Eclipse workspace.  For more fine grained control of scan results, setup a <A HREF=\"results filter\">'Results Filter'</A> instead.";

	private static final String FINDBUGS_INFO = "<A HREF=\"http://findbugs.sourceforge.net/\">FindBugs\u2122</A> is a static "
			+ "analysis tool created at University of Maryland for finding bugs "
			+ "in Java code.";
	private static final String PMD_INFO = "PMD\u2122 is a static analysis tool "
			+ "to look for multiple issues like potential bugs, dead, duplicate "
			+ "and suboptimal code, and overcomplicated  expressions.";
	private static final String RECKONER_INFO = "Reckoner is metrics gathering tool "
			+ "created by SureLogic that collects metrics like logical lines of code "
			+ "and defect density for java code.";
	private static final String CHECKSTYLE_INFO = "CheckStyle\u2122 is a static "
			+ "analysis tool that identifies stylisitic issues with the java code.";
	private static final String TAB_SPACE = "\t";

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
		data.widthHint = 100;

		f_runFindbugsFlag = new BooleanFieldEditor(
				PreferenceConstants.P_RUN_FINDBUGS, "FindBugs\u2122",
				toolsGroup);
		f_runFindbugsFlag.setPage(this);
		f_runFindbugsFlag.setPreferenceStore(getPreferenceStore());
		f_runFindbugsFlag.load();

		addSpacedText(toolsGroup, FINDBUGS_INFO);

		f_runPMDFlag = new BooleanFieldEditor(PreferenceConstants.P_RUN_PMD,
				"PMD\u2122", toolsGroup);
		f_runPMDFlag.setPage(this);
		f_runPMDFlag.setPreferenceStore(getPreferenceStore());
		f_runPMDFlag.load();

		addSpacedText(toolsGroup, PMD_INFO);

		f_runReckonerFlag = new BooleanFieldEditor(
				PreferenceConstants.P_RUN_RECKONER, "Reckoner", toolsGroup);
		f_runReckonerFlag.setPage(this);
		f_runReckonerFlag.setPreferenceStore(getPreferenceStore());
		f_runReckonerFlag.load();

		addSpacedText(toolsGroup, RECKONER_INFO);

		f_runCheckStyleFlag = new BooleanFieldEditor(
				PreferenceConstants.P_RUN_CHECKSTYLE, "CheckStyle\u2122",
				toolsGroup);
		f_runCheckStyleFlag.setPage(this);
		f_runCheckStyleFlag.setPreferenceStore(getPreferenceStore());
		f_runCheckStyleFlag.load();

		addSpacedText(toolsGroup, CHECKSTYLE_INFO);

		final Composite warning = new Composite(panel, SWT.NONE);
		warning.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false,
				2, 1));
		gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		warning.setLayout(gridLayout);
		final Label warningImg = new Label(warning, SWT.NONE);
		warningImg.setImage(SLImages
				.getWorkbenchImage(ISharedImages.IMG_OBJS_WARN_TSK));
		warningImg
				.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));

		final Label deselectWarning = new Label(warning, SWT.WRAP);
		data = new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1);
		data.widthHint = 300;
		deselectWarning.setLayoutData(data);
		deselectWarning.setText(DESELECT_TOOL_WARNING);
		return panel;

	}

	public void init(IWorkbench workbench) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("Use this page to select the tools to be included in scan.");
	}

	private void addSpacedText(Composite parent, String text) {
		final Composite composite = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		composite.setLayout(gridLayout);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false,
				2, 1));

		addSpace(composite);

		final Label infoText = new Label(composite, SWT.WRAP);
		GridData data = new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1);
		data.widthHint = 300;
		infoText.setLayoutData(data);
		infoText.setText(text);
	}

	/**
	 * Utility to add a space
	 * 
	 * @param parent
	 */
	private void addSpace(Composite parent) {
		final Label tabSpace = new Label(parent, SWT.NONE);
		tabSpace.setText(TAB_SPACE);
		tabSpace.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false,
				1, 1));
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
