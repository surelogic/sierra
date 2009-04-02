package com.surelogic.sierra.tool.findbugs;

import java.io.File;

import com.surelogic.sierra.tool.ITool;
import com.surelogic.sierra.tool.IToolFactory;
import com.surelogic.sierra.tool.message.Config;

public class FindBugsToolFactory implements IToolFactory {
	private static final String FINDBUGS = "findbugs";
	
	public String getId() {
		return FINDBUGS;
	}
	
	public void init(File toolHome) {
		AbstractFindBugsTool.init(toolHome.getAbsolutePath());
	}
	
	public ITool create(Config config) {
		return new AbstractFindBugsTool(config);
	}
}
