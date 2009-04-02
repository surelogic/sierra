package com.surelogic.sierra.tool.pmd;

import com.surelogic.sierra.tool.*;
import com.surelogic.sierra.tool.message.Config;

public class CPDToolFactory extends AbstractToolFactory {
	public ITool create(Config config) {
		return new CPD4_1Tool(config);
	}
}
