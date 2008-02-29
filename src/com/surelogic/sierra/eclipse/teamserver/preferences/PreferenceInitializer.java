package com.surelogic.sierra.eclipse.teamserver.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import com.surelogic.sierra.eclipse.teamserver.Activator;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		final IPreferenceStore store = Activator.getDefault()
				.getPreferenceStore();
		store.setDefault(PreferenceConstants.P_SERVER_STAYS_RUNNING, true);
		store.setDefault(PreferenceConstants.P_LOG_SHOWING, 1);
		store.setDefault(PreferenceConstants.P_PORT, 13376);
	}
}
