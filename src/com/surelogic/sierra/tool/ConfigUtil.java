package com.surelogic.sierra.tool;

import java.io.File;
import java.util.*;
import com.surelogic.sierra.tool.message.Config;

public final class ConfigUtil {
	protected final Config config;

	public ConfigUtil(Config config) {
		this.config = config;
	}

	protected String getPluginDir(final boolean debug, final String pluginId) {
		return getPluginDir(debug, pluginId, true);
	}

	protected String getPluginDir(final boolean debug, final String pluginId,
			boolean required) {
		final String pluginDir = config.getPluginDir(pluginId, required);
		if (debug) {
			System.out.println(pluginId + " = " + pluginDir);
		}
		//usedPlugins.add(pluginId);
		return pluginDir;
	}

	public void addPluginToPath(final boolean debug, Collection<File> path, final String pluginId) {
		addPluginToPath(debug, path, pluginId, false);
	}

	protected void addPluginToPath(final boolean debug, Collection<File> path, final String pluginId, boolean unpacked) {
		final String pluginDir = getPluginDir(debug, pluginId);
		if (unpacked) {
			boolean workspaceExists = addToPath(path, pluginDir + "/bin", false); // in workspace
			if (!workspaceExists) {
				addToPath(path, pluginDir); // as plugin
			}
		} else if (pluginDir.endsWith(".jar")) {
			addToPath(path, pluginDir); // as plugin
		} else {
			addToPath(path, pluginDir + "/bin"); // in workspace
		}
	}

	/**
	 * @return true if found
	 */
	protected boolean addPluginJarsToPath(final boolean debug, Collection<File> path, 
			                              final String pluginId, String... jars) {
		return addPluginJarsToPath(debug, path, pluginId, false, jars);
	}

	/**
	 * @param exclusive
	 *            If true, try each of the jars in sequence until one exists
	 * @return true if found
	 */
	protected boolean addPluginJarsToPath(final boolean debug, 
			Collection<File> path, final String pluginId, boolean exclusive,
			String... jars) {
		boolean rv = true;
		final String pluginDir = getPluginDir(debug, pluginId);
		for (String jar : jars) {
			boolean exists = addToPath(path, pluginDir + '/' + jar,
					!exclusive);
			if (exclusive && exists) {
				return true;
			}
			rv = rv && exists;
		}
		return rv;
	}

	protected void addAllPluginJarsToPath(final boolean debug,
			Collection<File> path, final String pluginId, String libPath) {
		final String pluginDir = getPluginDir(debug, pluginId);
		findJars(path, pluginDir + '/' + libPath);
	}

	protected boolean addToPath(Collection<File> path, String name) {
		return addToPath(path, new File(name), true);
	}

	protected boolean addToPath(Collection<File> path, String name, boolean required) {
		return addToPath(path, new File(name), required);
	}

	protected boolean addToPath(Collection<File> path, File f, boolean required) {
		final boolean exists = f.exists();
		if (!exists) {
			if (required) {
				// FIX
				throw new RuntimeException("Missing required library: "+f.getAbsolutePath());
						/*
						RemoteSLJobConstants.ERROR_CODE_MISSING_FOR_JOB,
						f.getAbsolutePath());
						*/
			}
		} else {
			path.add(f);
		}
		return exists;
	}

	protected void findJars(Collection<File> path, String folder) {
		findJars(path, new File(folder));
	}

	protected void findJars(Collection<File> path, File folder) {
		for (File f : folder.listFiles()) {
			String name = f.getName();
			if (name.endsWith(".jar")) {
				path.add(f);
			}
		}
	}
}
