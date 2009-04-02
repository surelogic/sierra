package com.surelogic.sierra.tool.reckoner;

import com.surelogic.sierra.tool.AbstractToolFactory;
import com.surelogic.sierra.tool.ITool;
import com.surelogic.sierra.tool.message.Config;

public class ReckonerFactory extends AbstractToolFactory {
	public String getId() {
		return "reckoner";
	}
	
	public ITool create(Config config) {
		return new Reckoner1_0Tool(config);
	}
}
