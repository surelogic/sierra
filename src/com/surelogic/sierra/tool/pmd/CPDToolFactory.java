package com.surelogic.sierra.tool.pmd;

import com.surelogic.sierra.tool.*;
import com.surelogic.sierra.tool.message.Config;

public class CPDToolFactory extends AbstractToolFactory {
	private static final String CPD = "cpd";
		
	public String getId() {
		return CPD;
	}
	
	public ITool create(Config config) {
		return new CPD4_1Tool(config);
	}
}
