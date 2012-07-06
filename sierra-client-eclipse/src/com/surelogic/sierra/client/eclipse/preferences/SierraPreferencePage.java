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
import org.eclipse.ui.PlatformUI;

import com.surelogic.common.XUtil;
import com.surelogic.common.core.EclipseUtility;
import com.surelogic.common.core.MemoryUtility;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.ui.EclipseUIUtility;
import com.surelogic.common.ui.adhoc.views.ExportQueryDialog;
import com.surelogic.common.ui.preferences.AbstractCommonPreferencePage;
import com.surelogic.sierra.client.eclipse.views.adhoc.AdHocDataSource;
import com.surelogic.sierra.tool.message.Importance;

public class SierraPreferencePage extends AbstractCommonPreferencePage {
	static private final String TOOL_MB_LABEL = "sierra.eclipse.preference.page.toolMemoryPreferenceLabel";

	private BooleanFieldEditor f_balloonFlag;
	private BooleanFieldEditor f_selectProjectsToScan;
	private BooleanFieldEditor f_showJSureResultsFlag;
	private BooleanFieldEditor f_showMarkersInJavaEditorFlag;
	private RadioGroupFieldEditor f_showAbove;
	private BooleanFieldEditor f_saveResources;
	private IntegerFieldEditor f_findingsListLimit;
	private Label f_estimate;
	private ScaleFieldEditor f_toolMemoryMB;

	public SierraPreferencePage() {
		super("sierra.eclipse.", SierraPreferencesUtility
				.getSwitchPreferences());
	}

	@Override
	protected Control createContents(final Composite parent) {
		final Composite panel = new Composite(parent, SWT.NONE);
		final GridLayout grid = new GridLayout();
		panel.setLayout(grid);

		final Group dataGroup = new Group(panel, SWT.NONE);
		dataGroup.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		dataGroup
				.setText(I18N.msg("sierra.eclipse.preference.page.group.data"));
		dataGroup.setLayout(new GridLayout());

		final Label dataDirectory = new Label(dataGroup, SWT.NONE);
		dataDirectory.setText(SierraPreferencesUtility.getSierraDataDirectory()
				.getAbsolutePath());
		dataDirectory.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false));

		final Group diGroup = new Group(panel, SWT.NONE);
		diGroup.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		diGroup.setText(I18N.msg("sierra.eclipse.preference.page.group.app"));

		f_findingsListLimit = new IntegerFieldEditor(
				SierraPreferencesUtility.FINDINGS_LIST_LIMIT,
				I18N.msg("sierra.eclipse.preference.page.findingsListLimit"),
				diGroup);
		f_findingsListLimit.fillIntoGrid(diGroup, 2);
		f_findingsListLimit.setPage(this);
		f_findingsListLimit.setPreferenceStore(EclipseUIUtility
				.getPreferences());
		f_findingsListLimit.load();

		f_balloonFlag = new BooleanFieldEditor(
				SierraPreferencesUtility.SHOW_BALLOON_NOTIFICATIONS,
				I18N.msg("sierra.eclipse.preference.page.balloonFlag"), diGroup);
		f_balloonFlag.fillIntoGrid(diGroup, 2);
		f_balloonFlag.setPage(this);
		f_balloonFlag.setPreferenceStore(EclipseUIUtility.getPreferences());
		f_balloonFlag.load();

		setupForPerspectiveSwitch(diGroup);

		f_selectProjectsToScan = new BooleanFieldEditor(
				SierraPreferencesUtility.ALWAYS_ALLOW_USER_TO_SELECT_PROJECTS_TO_SCAN,
				I18N.msg("sierra.eclipse.preference.page.selectProjectsToScan"),
				diGroup);
		f_selectProjectsToScan.fillIntoGrid(diGroup, 2);
		f_selectProjectsToScan.setPage(this);
		f_selectProjectsToScan.setPreferenceStore(EclipseUIUtility
				.getPreferences());
		f_selectProjectsToScan.load();

		f_showJSureResultsFlag = new BooleanFieldEditor(
				SierraPreferencesUtility.SHOW_JSURE_FINDINGS,
				I18N.msg("sierra.eclipse.preference.page.showJSureResultsFlag"),
				diGroup);
		f_showJSureResultsFlag.fillIntoGrid(diGroup, 2);
		f_showJSureResultsFlag.setPage(this);
		f_showJSureResultsFlag.setPreferenceStore(EclipseUIUtility
				.getPreferences());
		f_showJSureResultsFlag.load();

		f_showMarkersInJavaEditorFlag = new BooleanFieldEditor(
				SierraPreferencesUtility.SHOW_MARKERS,
				I18N.msg("sierra.eclipse.preference.page.showMarkersInJavaEditorFlag"),
				diGroup);
		f_showMarkersInJavaEditorFlag.fillIntoGrid(diGroup, 2);
		f_showMarkersInJavaEditorFlag.setPage(this);
		f_showMarkersInJavaEditorFlag.setPreferenceStore(EclipseUIUtility
				.getPreferences());
		f_showMarkersInJavaEditorFlag.load();

		f_showAbove = new RadioGroupFieldEditor(
				SierraPreferencesUtility.SHOW_MARKERS_AT_OR_ABOVE_IMPORTANCE,
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
		f_showAbove.setPreferenceStore(EclipseUIUtility.getPreferences());
		f_showAbove.load();

		diGroup.setLayout(new GridLayout(2, false));

		final Group memoryGroup = new Group(panel, SWT.NONE);
		memoryGroup.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		memoryGroup.setText(I18N
				.msg("sierra.eclipse.preference.page.group.scan"));

		final int estimatedMax = MemoryUtility.computeMaxMemorySizeInMb();
		int mb = EclipseUtility
				.getIntPreference(SierraPreferencesUtility.TOOL_MEMORY_MB);
		if (mb > estimatedMax) {
			mb = estimatedMax;
			EclipseUtility.setIntPreference(
					SierraPreferencesUtility.TOOL_MEMORY_MB, mb);
		}

		final String label = I18N.msg(TOOL_MB_LABEL, mb);
		f_toolMemoryMB = new ScaleFieldEditor(
				SierraPreferencesUtility.TOOL_MEMORY_MB, label + "     ",
				memoryGroup);
		f_toolMemoryMB.fillIntoGrid(memoryGroup, 2);
		f_toolMemoryMB.setMinimum(256);
		f_toolMemoryMB.setMaximum(estimatedMax);
		f_toolMemoryMB.setPageIncrement(256);
		f_toolMemoryMB.setPage(this);
		f_toolMemoryMB.setPreferenceStore(EclipseUIUtility.getPreferences());
		f_toolMemoryMB.load();
		f_toolMemoryMB.getScaleControl().addListener(SWT.Selection,
				new Listener() {
					@Override
					public void handleEvent(final Event event) {
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
				SierraPreferencesUtility.ALWAYS_SAVE_RESOURCES,
				I18N.msg("sierra.eclipse.preference.page.saveModified"),
				memoryGroup);
		f_saveResources.fillIntoGrid(memoryGroup, 2);
		f_saveResources.setPage(this);
		f_saveResources.setPreferenceStore(EclipseUIUtility.getPreferences());
		f_saveResources.load();

		memoryGroup.setLayout(new GridLayout(2, false));

		/*
		 * Allow access to help via the F1 key.
		 */
		PlatformUI
				.getWorkbench()
				.getHelpSystem()
				.setHelp(parent,
						"com.surelogic.sierra.client.eclipse.preferences-sierra");
		if (XUtil.useExperimental()) {
			final Button exportButton = new Button(parent, SWT.PUSH);
			exportButton.setText("Export New Queries File");
			exportButton.setLayoutData(new GridData(SWT.DEFAULT, SWT.DEFAULT,
					false, false));
			exportButton.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(final Event event) {
					new ExportQueryDialog(EclipseUIUtility.getShell(),
							AdHocDataSource.getManager()).open();
				}
			});
		}
		return panel;
	}

	private void updateMBInLabel() {
		final int mb = f_toolMemoryMB.getScaleControl().getSelection();
		f_toolMemoryMB.setLabelText(I18N.msg(TOOL_MB_LABEL, mb));
	}

	@Override
	protected void performDefaults() {
		f_balloonFlag.loadDefault();
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