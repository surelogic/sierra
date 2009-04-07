package com.surelogic.sierra.tool.findbugs;

import java.io.File;

import com.surelogic.sierra.tool.*;
import com.surelogic.sierra.tool.message.Config;

public class FindBugsToolFactory extends AbstractToolFactory {	
//	@Override
//	public String getVersion() {
//		return "1.3.7"/*Version.RELEASE_BASE*/;
//	}
	
	@Override
	public void init(File toolHome, File pluginDir) {
		super.init(toolHome, pluginDir);
		AbstractFindBugsTool.init(toolHome.getAbsolutePath());
	}
	
	public ITool create(Config config) {
		return new AbstractFindBugsTool(this, config);
	}
}
