package com.surelogic.sierra.tool.pmd;

import com.surelogic.sierra.tool.*;
import com.surelogic.sierra.tool.message.Config;

public class CPDToolFactory extends AbstractToolFactory {
	private static final String CPD = "CPD";
	private static final String INFO = "<A HREF=\"http://pmd.sourceforge.net\">CPD</A> is a static analysis tool that looks for duplicated code.";
		
	public String getId() {
		return CPD;
	}
	
	public String getName() {
		return "CPD";
	}
	
	@Override 
	public String getVersion() {
		return "4.1";
	}
	
	public String getHTMLInfo() {
		return INFO;
	}
	
	public ITool create(Config config) {
		return new CPD4_1Tool(this, config);
	}
}
