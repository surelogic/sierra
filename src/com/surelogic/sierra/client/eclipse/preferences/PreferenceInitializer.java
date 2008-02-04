package com.surelogic.sierra.client.eclipse.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import com.surelogic.sierra.client.eclipse.Activator;
import com.surelogic.sierra.tool.message.Importance;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		final IPreferenceStore store = Activator.getDefault()
				.getPreferenceStore();
		store.setDefault(PreferenceConstants.P_SIERRA_DELETE_DB_ON_STARTUP,
				false);
		store.setDefault(PreferenceConstants.P_SIERRA_BALLOON_FLAG, true);
		store
				.setDefault(
						PreferenceConstants.P_SIERRA_SHOW_MARKERS_AT_OR_ABOVE_IMPORTANCE,
						Importance.HIGH.toString());
		store.setDefault(PreferenceConstants.P_RUN_CHECKSTYLE, false);
		store.setDefault(PreferenceConstants.P_RUN_FINDBUGS, true);
		store.setDefault(PreferenceConstants.P_RUN_PMD, true);
		store.setDefault(PreferenceConstants.P_RUN_RECKONER, true);
		store.setDefault(PreferenceConstants.P_SIERRA_ALWAYS_SAVE_RESOURCES,
				false);
		store.setDefault(PreferenceConstants.P_SIERRA_SHOW_MARKERS, true);
		store.setDefault(PreferenceConstants.P_FINDINGS_LIST_LIMIT, 2000);
		store.setDefault(PreferenceConstants.P_WARN_MAXIMUM_FINDINGS_SHOWN,
				true);
	}
}
