package com.surelogic.sierra.client.eclipse.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.dialogs.PreferencesUtil;

public final class PreferencesAction implements IWorkbenchWindowActionDelegate {

	private static final String PREF_ID = "com.surelogic.sierra.client.eclipse.preferences.SierraPreferencePage";

	public void dispose() {
		// Nothing to do
	}

	public void init(IWorkbenchWindow window) {
		// Nothing to do
	}

	public void run(IAction action) {
		PreferencesUtil.createPreferenceDialogOn(null, PREF_ID,
				new String[] { PREF_ID }, null).open();
	}

	public void selectionChanged(IAction action, ISelection selection) {
		// Nothing to do
	}
}
