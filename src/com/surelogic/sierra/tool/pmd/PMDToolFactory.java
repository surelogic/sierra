package com.surelogic.sierra.tool.pmd;

import com.surelogic.sierra.tool.*;
import com.surelogic.sierra.tool.message.Config;

public class PMDToolFactory extends AbstractToolFactory {
	private static final String PMD = "pmd";
	private static final String INFO = "<A HREF=\"http://pmd.sourceforge.net\">PMD</A> is a static analysis tool to look for multiple issues like potential bugs, dead and sub-optimal code, and over-complicated expressions.";

	public String getId() {
		return PMD;
	}
	
	public String getName() {
		return "PMD\u2122";
	}
	
	public String getHTMLInfo() {
		return INFO;
	}
	
	public ITool create(Config config) {
		return new AbstractPMDTool(config);
	}
}
