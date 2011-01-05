package com.surelogic.sierra.tool;

import com.surelogic.common.jobs.remote.AbstractPluginUtil;
import com.surelogic.sierra.tool.message.Config;

/**
 * Collects various methods that use a Config object
 * 
 * @author edwin
 */
public final class ConfigUtil extends AbstractPluginUtil {
	protected final Config config;

	public ConfigUtil(Config config) {
		this.config = config;
	}

	@Override
	protected String getPluginDir(final String pluginId, boolean required) {
		return config.getPluginDir(pluginId, required);
	}
}
