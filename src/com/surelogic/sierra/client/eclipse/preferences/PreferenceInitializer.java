package com.surelogic.sierra.client.eclipse.preferences;

import java.io.File;
import java.util.logging.Level;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.client.eclipse.Activator;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	static private final String RAW = System.getProperty("user.home")
			+ System.getProperty("file.separator") + "Sierra";

	@Override
	public void initializeDefaultPreferences() {
		final IPreferenceStore store = Activator.getDefault()
				.getPreferenceStore();
		create(RAW);
		store.setDefault(PreferenceConstants.P_SIERRA_PATH, RAW);
	}

	/**
	 * Creates the specified path in the filesystem unless the path already
	 * exists.
	 * 
	 * @param path
	 *            the filesystem path.
	 */
	private void create(final String path) {
		File p = new File(path);
		if (!p.exists()) {
			if (!p.mkdirs()) {
				SLLogger.getLogger().log(Level.SEVERE,
						"Couldn't create " + path);
			}
		}
	}
}
