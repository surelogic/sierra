package com.surelogic.sierra.client.eclipse.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.preference.ScaleFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

import com.surelogic.common.CommonImages;
import com.surelogic.common.FileUtility;
import com.surelogic.common.eclipse.MemoryUtility;
import com.surelogic.common.eclipse.SLImages;
import com.surelogic.common.eclipse.dialogs.ChangeDataDirectoryDialog;
import com.surelogic.common.eclipse.preferences.AbstractLicensePreferencePage;
import com.surelogic.common.i18n.I18N;
import com.surelogic.sierra.client.eclipse.Activator;
import com.surelogic.sierra.tool.message.Importance;

public class SierraPreferencePage extends AbstractLicensePreferencePage {

	static private final String TOOL_MB_LABEL = "sierra.eclipse.preference.page.toolMemoryPreferenceLabel";

	private BooleanFieldEditor f_balloonFlag;
	private BooleanFieldEditor f_promptPerspectiveSwitch;
	private BooleanFieldEditor f_autoPerspectiveSwitch;
	private BooleanFieldEditor f_selectProjectsToScan;
	private BooleanFieldEditor f_showJSureResultsFlag;
	private BooleanFieldEditor f_showMarkersInJavaEditorFlag;
	private RadioGroupFieldEditor f_showAbove;
	private BooleanFieldEditor f_saveResources;
	private IntegerFieldEditor f_findingsListLimit;
	private Label f_estimate;
	private ScaleFieldEditor f_toolMemoryMB;
	private Label f_dataDirectory;

	public void init(IWorkbench workbench) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription(I18N.msg("sierra.eclipse.preference.page.title.msg"));
	}

	@Override
	protected Control createContents(Composite parent) {
		final Composite panel = new Composite(parent, SWT.NONE);
		GridLayout grid = new GridLayout();
		panel.setLayout(grid);

		final Group dataGroup = new Group(panel, SWT.NONE);
		dataGroup.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		dataGroup
				.setText(I18N.msg("sierra.eclipse.preference.page.group.data"));
		dataGroup.setLayout(new GridLayout(2, false));

		f_dataDirectory = new Label(dataGroup, SWT.NONE);
		updateDataDirectory();
		f_dataDirectory.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false));

		final Button change = new Button(dataGroup, SWT.PUSH);
		change.setText(I18N
				.msg("sierra.eclipse.preference.page.changeDataDirectory"));
		change.setLayoutData(new GridData(SWT.DEFAULT, SWT.DEFAULT, false,
				false));
		change.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				ChangeDataDirectoryDialog
						.open(
								change.getShell(),
								FileUtility.getSierraDataDirectoryAnchor(),
								I18N
										.msg("sierra.change.data.directory.dialog.title"),
								SLImages.getImage(CommonImages.IMG_SIERRA_LOGO),
								I18N
										.msg("sierra.change.data.directory.dialog.information"),
								null, null);
				updateDataDirectory();
			}
		});

		final Group diGroup = new Group(panel, SWT.NONE);
		diGroup.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		diGroup.setText(I18N.msg("sierra.eclipse.preference.page.group.app"));

		f_findingsListLimit = new IntegerFieldEditor(
				PreferenceConstants.P_FINDINGS_LIST_LIMIT,
				I18N.msg("sierra.eclipse.preference.page.findingsListLimit"),
				diGroup);
		f_findingsListLimit.fillIntoGrid(diGroup, 2);
		f_findingsListLimit.setPage(this);
		f_findingsListLimit.setPreferenceStore(getPreferenceStore());
		f_findingsListLimit.load();

		f_balloonFlag = new BooleanFieldEditor(
				PreferenceConstants.P_SIERRA_BALLOON_FLAG, I18N
						.msg("sierra.eclipse.preference.page.balloonFlag"),
				diGroup);
		f_balloonFlag.fillIntoGrid(diGroup, 2);
		f_balloonFlag.setPage(this);
		f_balloonFlag.setPreferenceStore(getPreferenceStore());
		f_balloonFlag.load();

		f_promptPerspectiveSwitch = new BooleanFieldEditor(
				PreferenceConstants.P_PROMPT_PERSPECTIVE_SWITCH,
				I18N
						.msg("sierra.eclipse.preference.page.promptPerspectiveSwitch"),
				diGroup);
		f_promptPerspectiveSwitch.fillIntoGrid(diGroup, 2);
		f_promptPerspectiveSwitch.setPage(this);
		f_promptPerspectiveSwitch.setPreferenceStore(getPreferenceStore());
		f_promptPerspectiveSwitch.load();

		f_autoPerspectiveSwitch = new BooleanFieldEditor(
				PreferenceConstants.P_AUTO_PERSPECTIVE_SWITCH,
				I18N
						.msg("sierra.eclipse.preference.page.autoPerspectiveSwitch"),
				diGroup);
		f_autoPerspectiveSwitch.fillIntoGrid(diGroup, 2);
		f_autoPerspectiveSwitch.setPage(this);
		f_autoPerspectiveSwitch.setPreferenceStore(getPreferenceStore());
		f_autoPerspectiveSwitch.load();

		f_selectProjectsToScan = new BooleanFieldEditor(
				PreferenceConstants.P_SELECT_PROJECTS_TO_SCAN,
				I18N.msg("sierra.eclipse.preference.page.selectProjectsToScan"),
				diGroup);
		f_selectProjectsToScan.fillIntoGrid(diGroup, 2);
		f_selectProjectsToScan.setPage(this);
		f_selectProjectsToScan.setPreferenceStore(getPreferenceStore());
		f_selectProjectsToScan.load();

		f_showJSureResultsFlag = new BooleanFieldEditor(
				PreferenceConstants.P_SIERRA_SHOW_JSURE_FINDINGS,
				I18N.msg("sierra.eclipse.preference.page.showJSureResultsFlag"),
				diGroup);
		f_showJSureResultsFlag.fillIntoGrid(diGroup, 2);
		f_showJSureResultsFlag.setPage(this);
		f_showJSureResultsFlag.setPreferenceStore(getPreferenceStore());
		f_showJSureResultsFlag.load();

		f_showMarkersInJavaEditorFlag = new BooleanFieldEditor(
				PreferenceConstants.P_SIERRA_SHOW_MARKERS,
				I18N
						.msg("sierra.eclipse.preference.page.showMarkersInJavaEditorFlag"),
				diGroup);
		f_showMarkersInJavaEditorFlag.fillIntoGrid(diGroup, 2);
		f_showMarkersInJavaEditorFlag.setPage(this);
		f_showMarkersInJavaEditorFlag.setPreferenceStore(getPreferenceStore());
		f_showMarkersInJavaEditorFlag.load();

		f_showAbove = new RadioGroupFieldEditor(
				PreferenceConstants.P_SIERRA_SHOW_MARKERS_AT_OR_ABOVE_IMPORTANCE,
				I18N.msg("sierra.eclipse.preference.page.showAbove"), 1,
				new String[][] {
						{ Importance.CRITICAL.toStringSentenceCase(),
								Importance.CRITICAL.toString() },
						{ Importance.HIGH.toStringSentenceCase(),
								Importance.HIGH.toString() },
						{ Importance.MEDIUM.toStringSentenceCase(),
								Importance.MEDIUM.toString() },
						{ Importance.LOW.toStringSentenceCase(),
								Importance.LOW.toString() },
						{ Importance.IRRELEVANT.toStringSentenceCase(),
								Importance.IRRELEVANT.toString() } },

				diGroup);
		f_showAbove.fillIntoGrid(diGroup, 2);
		f_showAbove.setPage(this);
		f_showAbove.setPreferenceStore(getPreferenceStore());
		f_showAbove.load();

		diGroup.setLayout(new GridLayout(2, false));

		final Group memoryGroup = new Group(panel, SWT.NONE);
		memoryGroup.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		memoryGroup.setText(I18N
				.msg("sierra.eclipse.preference.page.group.scan"));

		final int estimatedMax = MemoryUtility.computeMaxMemorySizeInMb();
		int mb = PreferenceConstants.getToolMemoryMB();
		if (mb > estimatedMax) {
			mb = estimatedMax;
			PreferenceConstants.setToolMemoryMB(mb);
		}

		final String label = I18N.msg(TOOL_MB_LABEL, mb);
		f_toolMemoryMB = new ScaleFieldEditor(
				PreferenceConstants.P_TOOL_MEMORY_MB, label + "     ",
				memoryGroup);
		f_toolMemoryMB.fillIntoGrid(memoryGroup, 2);
		f_toolMemoryMB.setMinimum(256);
		f_toolMemoryMB.setMaximum(estimatedMax);
		f_toolMemoryMB.setPageIncrement(256);
		f_toolMemoryMB.setPage(this);
		f_toolMemoryMB.setPreferenceStore(getPreferenceStore());
		f_toolMemoryMB.load();
		f_toolMemoryMB.getScaleControl().addListener(SWT.Selection,
				new Listener() {
					public void handleEvent(Event event) {
						updateMBInLabel();
					}
				});

		f_estimate = new Label(memoryGroup, SWT.NONE);
		f_estimate.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false,
				false, 2, 1));
		f_estimate.setText(I18N.msg(
				"sierra.eclipse.preference.page.computedMaxToolMemoryLabel",
				estimatedMax));

		f_saveResources = new BooleanFieldEditor(
				PreferenceConstants.P_SIERRA_ALWAYS_SAVE_RESOURCES, I18N
						.msg("sierra.eclipse.preference.page.saveModified"),
				memoryGroup);
		f_saveResources.fillIntoGrid(memoryGroup, 2);
		f_saveResources.setPage(this);
		f_saveResources.setPreferenceStore(getPreferenceStore());
		f_saveResources.load();

		memoryGroup.setLayout(new GridLayout(2, false));

		/*
		 * Allow access to help via the F1 key.
		 */
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent,
				"com.surelogic.sierra.client.eclipse.preferences-sierra");

		return panel;
	}

	private void updateMBInLabel() {
		final int mb = f_toolMemoryMB.getScaleControl().getSelection();
		f_toolMemoryMB.setLabelText(I18N.msg(TOOL_MB_LABEL, mb));
	}

	private void updateDataDirectory() {
		f_dataDirectory.setText(FileUtility.getSierraDataDirectory()
				.getAbsolutePath());
	}

	@Override
	protected void performDefaults() {
		f_balloonFlag.loadDefault();
		f_promptPerspectiveSwitch.loadDefault();
		f_autoPerspectiveSwitch.loadDefault();
		f_selectProjectsToScan.loadDefault();
		f_showJSureResultsFlag.loadDefault();
		f_showMarkersInJavaEditorFlag.loadDefault();
		f_showAbove.loadDefault();
		f_saveResources.loadDefault();
		f_findingsListLimit.loadDefault();
		f_toolMemoryMB.loadDefault();
		super.performDefaults();

		updateMBInLabel();
	}

	@Override
	public boolean performOk() {
		f_balloonFlag.store();
		f_promptPerspectiveSwitch.store();
		f_autoPerspectiveSwitch.store();
		f_selectProjectsToScan.store();
		f_showJSureResultsFlag.store();
		f_showMarkersInJavaEditorFlag.store();
		f_showAbove.store();
		f_saveResources.store();
		f_findingsListLimit.store();
		f_toolMemoryMB.store();
		return super.performOk();
	}
}