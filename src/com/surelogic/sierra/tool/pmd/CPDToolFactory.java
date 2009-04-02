package com.surelogic.sierra.tool.pmd;

import com.surelogic.sierra.tool.ITool;
import com.surelogic.sierra.tool.IToolFactory;
import com.surelogic.sierra.tool.message.Config;

public class CPDToolFactory implements IToolFactory {
	public ITool create(Config config) {
		return new CPD4_1Tool(config);
	}
}
