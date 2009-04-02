package com.surelogic.sierra.tool.pmd;

import com.surelogic.sierra.tool.*;
import com.surelogic.sierra.tool.message.Config;

public class PMDToolFactory extends AbstractToolFactory {
	private static final String PMD = "pmd";
	
	public String getId() {
		return PMD;
	}
	
	public ITool create(Config config) {
		return new AbstractPMDTool(config);
	}
}
