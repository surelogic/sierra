package com.surelogic.sierra.eclipse.teamserver.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.dialogs.PreferencesUtil;

/**
 * Provides quick access to the local team server preferences.
 */
public final class PreferenceAction extends Action {

	public PreferenceAction(String name) {
		super(name);
	}

	public static final String SERVER_ID = "com.surelogic.sierra.eclipse.teamserver.preferences.LocalTeamServerPreferencePage";

	public static final String[] FILTER = new String[] { SERVER_ID };

	@Override
	public void run() {
		PreferencesUtil.createPreferenceDialogOn(null, SERVER_ID,
				PreferenceAction.FILTER, null).open();
	}
}
