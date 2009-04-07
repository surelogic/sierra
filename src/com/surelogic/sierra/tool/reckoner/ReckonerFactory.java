package com.surelogic.sierra.tool.reckoner;

import com.surelogic.sierra.tool.AbstractToolFactory;
import com.surelogic.sierra.tool.ITool;
import com.surelogic.sierra.tool.message.Config;

public class ReckonerFactory extends AbstractToolFactory {	
	public ITool create(Config config) {
		return new Reckoner1_0Tool(this, config);
	}
}
