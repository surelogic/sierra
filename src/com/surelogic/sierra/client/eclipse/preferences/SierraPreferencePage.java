package com.surelogic.sierra.client.eclipse.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.surelogic.sierra.client.eclipse.Activator;
import com.surelogic.sierra.tool.message.Importance;

public class SierraPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage {

	BooleanFieldEditor f_balloonFlag;
	BooleanFieldEditor f_showMarkersInJavaEditorFlag;
	RadioGroupFieldEditor f_showAbove;
	BooleanFieldEditor f_saveResources;
	IntegerFieldEditor f_findingsListLimit;

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

		return panel;
	}

	@Override
	protected void performDefaults() {
		f_balloonFlag.loadDefault();
		f_showMarkersInJavaEditorFlag.loadDefault();
		f_showAbove.loadDefault();
		f_saveResources.loadDefault();
		f_findingsListLimit.loadDefault();
		super.performDefaults();
	}

	@Override
	public boolean performOk() {
		f_balloonFlag.store();
		f_showMarkersInJavaEditorFlag.store();
		f_showAbove.store();
		f_saveResources.store();
		f_findingsListLimit.store();
		return super.performOk();
	}
}