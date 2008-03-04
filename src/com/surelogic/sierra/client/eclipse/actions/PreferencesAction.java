package com.surelogic.sierra.client.eclipse.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.dialogs.PreferencesUtil;

/**
 * TODO: this was removed from the main menu due to RfR requirements.
 */
public final class PreferencesAction extends Action implements
		IWorkbenchWindowActionDelegate {
	public PreferencesAction() {
		super();
	}

	public PreferencesAction(String name) {
		super(name);
	}

	public static final String PREF_ID = "com.surelogic.sierra.client.eclipse.preferences.SierraPreferencePage";
	public static final String TOOLS_ID = "com.surelogic.sierra.client.eclipse.preferences.ToolsPreferencePage";
	public static final String FILTER_ID = "com.surelogic.sierra.client.eclipse.preferences.ScanFilterPreferencePage";
	public static final String DATA_ID = "com.surelogic.sierra.client.eclipse.preferences.ScanDataPreferencePage";

	public void dispose() {
		// Nothing to do
	}

	public void init(IWorkbenchWindow window) {
		// Nothing to do
	}

	@Override
	public void run() {
		run(this);
	}

	public void run(IAction action) {
		PreferencesUtil.createPreferenceDialogOn(null, PREF_ID,
				new String[] { PREF_ID, TOOLS_ID, FILTER_ID, DATA_ID }, null)
				.open();
	}

	public void selectionChanged(IAction action, ISelection selection) {
		// Nothing to do
	}
}
