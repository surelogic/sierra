package com.surelogic.sierra.client.eclipse.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.preference.ScaleFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;

import com.surelogic.common.eclipse.MemoryUtility;
import com.surelogic.common.i18n.I18N;
import com.surelogic.sierra.client.eclipse.Activator;
import com.surelogic.sierra.tool.message.Importance;

public class SierraPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage {

	static private final String ESTIMATE_LABEL = "sierra.eclipse.computedMaxToolMemoryLabel";
	static private final String TOOL_MB_LABEL = "sierra.eclipse.toolMemoryPreferenceLabel";

	BooleanFieldEditor f_balloonFlag;
	BooleanFieldEditor f_selectProjectsToScan;
	BooleanFieldEditor f_showMarkersInJavaEditorFlag;
	RadioGroupFieldEditor f_showAbove;
	BooleanFieldEditor f_saveResources;
	IntegerFieldEditor f_findingsListLimit;
	Label f_estimate;
	ScaleFieldEditor f_toolMemoryMB;

	public void init(IWorkbench workbench) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("Use this page to customize Sierra.");
	}

	@Override
	protected Control createContents(Composite parent) {
		final Composite panel = new Composite(parent, SWT.NONE);
		GridLayout grid = new GridLayout();
		panel.setLayout(grid);

		final Group diGroup = new Group(panel, SWT.NONE);
		diGroup.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		diGroup.setText("Appearance");

		f_findingsListLimit = new IntegerFieldEditor(
				PreferenceConstants.P_FINDINGS_LIST_LIMIT,
				"Maximum number of findings shown in 'Findings Quick Search' results:",
				diGroup);
		f_findingsListLimit.setPage(this);
		f_findingsListLimit.setPreferenceStore(getPreferenceStore());
		f_findingsListLimit.load();

		f_balloonFlag = new BooleanFieldEditor(
				PreferenceConstants.P_SIERRA_BALLOON_FLAG,
				"Show 'balloon' notifications for scan start and completion.",
				diGroup);
		f_balloonFlag.setPage(this);
		f_balloonFlag.setPreferenceStore(getPreferenceStore());
		f_balloonFlag.load();

		f_selectProjectsToScan = new BooleanFieldEditor(
				PreferenceConstants.P_SELECT_PROJECTS_TO_SCAN,
				"Allow the user to select the set of projects to scan even when projects are selected in the Package Explorer.",
				diGroup);
		f_selectProjectsToScan.setPage(this);
		f_selectProjectsToScan.setPreferenceStore(getPreferenceStore());
		f_selectProjectsToScan.load();

		f_showMarkersInJavaEditorFlag = new BooleanFieldEditor(
				PreferenceConstants.P_SIERRA_SHOW_MARKERS,
				"Show markers for findings in the Java editor.", diGroup);
		f_showMarkersInJavaEditorFlag.setPage(this);
		f_showMarkersInJavaEditorFlag.setPreferenceStore(getPreferenceStore());
		f_showMarkersInJavaEditorFlag.load();

		f_showAbove = new RadioGroupFieldEditor(
				PreferenceConstants.P_SIERRA_SHOW_MARKERS_AT_OR_ABOVE_IMPORTANCE,
				"Only show markers for findings in the Java editor at or above",
				1, new String[][] {
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
		f_showAbove.setPage(this);
		f_showAbove.setPreferenceStore(getPreferenceStore());
		f_showAbove.load();

		f_saveResources = new BooleanFieldEditor(
				PreferenceConstants.P_SIERRA_ALWAYS_SAVE_RESOURCES,
				"Save all modified resources automatically prior to a scan.",
				panel);
		f_saveResources.setPage(this);
		f_saveResources.setPreferenceStore(getPreferenceStore());
		f_saveResources.load();

		final Group memoryGroup = new Group(panel, SWT.NONE);
		memoryGroup.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		memoryGroup.setText("Memory usage");

		final int estimatedMax = MemoryUtility.computeMaxMemorySize();
		int mb = PreferenceConstants.getToolMemoryMB();
		if (mb > estimatedMax) {
			mb = estimatedMax;
			PreferenceConstants.setToolMemoryMB(mb);
		}

		f_estimate = new Label(memoryGroup, SWT.NONE);
		f_estimate.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false,
				false, 2, 1));
		f_estimate.setText(I18N.msg(ESTIMATE_LABEL, estimatedMax));

		final String label = I18N.msg(TOOL_MB_LABEL, mb);
		f_toolMemoryMB = new ScaleFieldEditor(
				PreferenceConstants.P_TOOL_MEMORY_MB, label + "     ",
				memoryGroup);
		f_toolMemoryMB.setMinimum(256);
		f_toolMemoryMB.setMaximum(estimatedMax);
		f_toolMemoryMB.setPageIncrement(256);
		f_toolMemoryMB.setPage(this);
		f_toolMemoryMB.setPreferenceStore(getPreferenceStore());
		f_toolMemoryMB.load();
		final ScaleFieldEditor toolMemoryMB = f_toolMemoryMB;
		f_toolMemoryMB.getScaleControl().addListener(SWT.Selection,
				new Listener() {
					public void handleEvent(Event event) {
						final int mb = toolMemoryMB.getScaleControl()
								.getSelection();
						toolMemoryMB.setLabelText(I18N.msg(TOOL_MB_LABEL, mb));
					}
				});

		/*
		 * Allow access to help via the F1 key.
		 */
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent,
				"com.surelogic.sierra.client.eclipse.preferences-sierra");

		return panel;
	}

	@Override
	protected void performDefaults() {
		f_balloonFlag.loadDefault();
		f_selectProjectsToScan.loadDefault();
		f_showMarkersInJavaEditorFlag.loadDefault();
		f_showAbove.loadDefault();
		f_saveResources.loadDefault();
		f_findingsListLimit.loadDefault();
		f_toolMemoryMB.loadDefault();
		super.performDefaults();
	}

	@Override
	public boolean performOk() {
		f_balloonFlag.store();
		f_selectProjectsToScan.store();
		f_showMarkersInJavaEditorFlag.store();
		f_showAbove.store();
		f_saveResources.store();
		f_findingsListLimit.store();
		f_toolMemoryMB.store();
		return super.performOk();
	}
}