package com.surelogic.sierra.tool.pmd;

import com.surelogic.sierra.tool.*;
import com.surelogic.sierra.tool.message.Config;

public class PMDToolFactory extends AbstractToolFactory {
//	@Override
//	public String getVersion() {
//		return "4.2.4"/*PMD.VERSION*/;
//	}

	public ITool create(Config config) {
		return new AbstractPMDTool(this, config);
	}
}
