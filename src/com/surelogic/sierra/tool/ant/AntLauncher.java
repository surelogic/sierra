/**
 * 
 */
package com.surelogic.sierra.tool.ant;

import com.surelogic.sierra.tool.analyzer.Launcher;
import com.surelogic.sierra.tool.config.BaseConfig;

/**
 * @author ethan
 *
 */
public class AntLauncher extends Launcher {

	public AntLauncher(String Name, BaseConfig baseConfig) {
		super(Name, baseConfig);
	}

	/* (non-Javadoc)
	 * @see com.surelogic.sierra.tool.analyzer.Launcher#getToolsDirectory()
	 */
	@Override
	public String getToolsDirectory() {
		return null;
	}

}
