package com.surelogic.sierra.client.eclipse.preferences;

import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.surelogic.sierra.client.eclipse.Activator;

public class SierraPreferencePage extends FieldEditorPreferencePage implements
		IWorkbenchPreferencePage {

	public SierraPreferencePage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("Use this page to customize Sierra.");
	}

	/**
	 * Creates the field editors. Field editors are abstractions of the common
	 * GUI blocks needed to manipulate various types of preferences. Each field
	 * editor knows how to save and restore itself.
	 */
	@Override
	public void createFieldEditors() {
		addField(new DirectoryFieldEditor(PreferenceConstants.P_SIERRA_PATH,
				"&Sierra directory:", getFieldEditorParent()));
	}

	public void init(IWorkbench workbench) {
		// nothing extra to do here
	}
}