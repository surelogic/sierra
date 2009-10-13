package com.surelogic.sierra.client.eclipse.preferences;

import java.io.File;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import com.surelogic.common.FileUtility;
import com.surelogic.common.eclipse.EclipseUtility;
import com.surelogic.common.i18n.I18N;
import com.surelogic.sierra.client.eclipse.Activator;
import com.surelogic.sierra.client.eclipse.Tools;
import com.surelogic.sierra.client.eclipse.views.ServerStatusSort;
import com.surelogic.sierra.tool.IToolFactory;
import com.surelogic.sierra.tool.ToolUtil;
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
		store.setDefault(PreferenceConstants.P_PROMPT_PERSPECTIVE_SWITCH, true);
		store.setDefault(PreferenceConstants.P_AUTO_PERSPECTIVE_SWITCH, true);
		store
				.setDefault(
						PreferenceConstants.P_SIERRA_SHOW_MARKERS_AT_OR_ABOVE_IMPORTANCE,
						Importance.HIGH.toString());

		store.setDefault(PreferenceConstants.P_SIERRA_ALWAYS_SAVE_RESOURCES,
				false);
		store.setDefault(PreferenceConstants.P_SIERRA_SHOW_MARKERS, true);
		store.setDefault(PreferenceConstants.P_SIERRA_SHOW_JSURE_FINDINGS,
				false);
		store.setDefault(PreferenceConstants.P_FINDINGS_LIST_LIMIT, 2000);
		store.setDefault(PreferenceConstants.P_TOOL_MEMORY_MB, 1024);
		store.setDefault(PreferenceConstants.P_WARN_MAXIMUM_FINDINGS_SHOWN,
				true);
		store.setDefault(PreferenceConstants.P_SELECT_PROJECTS_TO_SCAN, true);
		store.setDefault(PreferenceConstants.P_OMIT_EMPTY_SYNCHRONIZE_ENTRIES,
				false);
		store.setDefault(PreferenceConstants.P_SERVER_STATUS_SORT,
				ServerStatusSort.BY_SERVER.toString());
		store.setDefault(
				PreferenceConstants.P_SERVER_INTERACTION_PERIOD_IN_MINUTES, 5);
		store.setDefault(
				PreferenceConstants.P_SERVER_INTERACTION_AUDIT_THRESHOLD, 200);
		store.setDefault(
				PreferenceConstants.P_SERVER_INTERACTION_RETRY_THRESHOLD, 4);
		store.setDefault(PreferenceConstants.P_SERVER_FAILURE_REPORTING,
				ServerFailureReport.SHOW_BALLOON.toString());
		store.setDefault(PreferenceConstants.P_DATA_DIRECTORY,
				getDefaultDataDirectory());

		// Get the data directory and ensure that it actually exists.
		final String path = store
				.getString(PreferenceConstants.P_DATA_DIRECTORY);
		if (path == null) {
			throw new IllegalStateException(I18N.err(44, "P_DATA_DIRECTORY"));
		}
		final File dataDir = new File(path);
		if (!FileUtility.createDirectory(dataDir)) {
			throw new RuntimeException("Unable to create " + path);
		}

		// Check if tools dir setup yet
		final String toolsDirSet = System
				.getProperty(ToolUtil.CUSTOM_TOOLS_PATH_PROP_NAME);
		if (toolsDirSet == null) {
			final String origToolsDir = System
					.getProperty(ToolUtil.TOOLS_PATH_PROP_NAME);
			// set Sierra tools dir if unavailable
			if (origToolsDir == null) {
				System.setProperty(ToolUtil.CUSTOM_TOOLS_PATH_PROP_NAME, "");
				System.setProperty(ToolUtil.TOOLS_PATH_PROP_NAME, path);
			} else {
				System.setProperty(ToolUtil.CUSTOM_TOOLS_PATH_PROP_NAME,
						origToolsDir);
			}
		}
		for (final IToolFactory f : Tools.findToolFactories()) {
			store.setDefault(PreferenceConstants.getToolPref(f), true);
		}
	}

	private String getDefaultDataDirectory() {
		final File root = EclipseUtility.getWorspacePath();
		final File path = new File(root, FileUtility.SIERRA_DATA_PATH_FRAGMENT);
		return path.getAbsolutePath();
	}
}
