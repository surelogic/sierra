package com.surelogic.sierra.tool.findbugs;

import java.io.File;

import com.surelogic.sierra.tool.*;
import com.surelogic.sierra.tool.message.Config;

public class FindBugsToolFactory extends AbstractToolFactory {
	private static final String FINDBUGS = "findbugs";
	private static final String INFO = "<A HREF=\"http://findbugs.sourceforge.net\">FindBugs</A> is a static analysis tool created at University of Maryland for finding bugs in Java code.";
	
	public String getId() {
		return FINDBUGS;
	}
	
	public String getName() {
		return "FindBugs\u2122";
	}
	
	public String getHTMLInfo() {
		return INFO;
	}
	
	@Override
	public void init(File toolHome) {
		AbstractFindBugsTool.init(toolHome.getAbsolutePath());
	}
	
	public ITool create(Config config) {
		return new AbstractFindBugsTool(config);
	}
}
