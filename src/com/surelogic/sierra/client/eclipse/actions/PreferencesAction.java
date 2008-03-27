package com.surelogic.sierra.client.eclipse.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.dialogs.PreferencesUtil;

/**
 * Provides quick access to the sierra preferences. This was removed from the
 * Sierra item on the Eclipse main menu due to RfR requirements.
 */
public final class PreferencesAction extends Action {

	public PreferencesAction(String name) {
		super(name);
	}

	public static final String PREF_ID = "com.surelogic.sierra.client.eclipse.preferences.SierraPreferencePage";
	public static final String TOOLS_ID = "com.surelogic.sierra.client.eclipse.preferences.ToolsPreferencePage";
	public static final String FILTER_ID = "com.surelogic.sierra.client.eclipse.preferences.ScanFilterPreferencePage";
	public static final String DATA_ID = "com.surelogic.sierra.client.eclipse.preferences.ScanDataPreferencePage";
	public static final String LOCAL_SERVER_ID = "com.surelogic.sierra.eclipse.teamserver.preferences.LocalTeamServerPreferencePage";
	public static final String SERVER_INTERACTION_ID = "com.surelogic.sierra.client.eclipse.preferences.ServerInteractionPreferencePage";

	
	public static final String[] FILTER = new String[] { PREF_ID, TOOLS_ID,
			FILTER_ID, DATA_ID, LOCAL_SERVER_ID, SERVER_INTERACTION_ID };

	@Override
	public void run() {
		PreferencesUtil.createPreferenceDialogOn(null, PREF_ID,
				PreferencesAction.FILTER, null).open();
	}
}
