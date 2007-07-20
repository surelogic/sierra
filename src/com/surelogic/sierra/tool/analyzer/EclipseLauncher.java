package com.surelogic.sierra.tool.analyzer;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.FileLocator;

import com.surelogic.sierra.SierraTool;
import com.surelogic.sierra.SierraLogger;
import com.surelogic.sierra.tool.config.BaseConfig;

public class EclipseLauncher extends Launcher {

	private static final Logger log = SierraLogger.getLogger("Sierra");

	public EclipseLauncher(String name, BaseConfig baseConfig) {
		super(name, baseConfig);
		baseConfig.setToolsDirectory(getToolsDirectory());

	}

	@Override
	public String getToolsDirectory() {

		String commonDirectory = "";

		URL relativeURL = SierraTool.getDefault().getBundle()
				.getEntry("");

		try {

			URL commonPathURL = FileLocator.resolve(relativeURL);
			commonDirectory = commonPathURL.getPath();
			commonDirectory = commonDirectory.replace("/", File.separator);

			return commonDirectory;

		} catch (IOException e) {
			log.log(Level.SEVERE, "Error getting plugin directory.", e);
		}

		return commonDirectory;

	}

}
