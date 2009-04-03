package com.surelogic.sierra.tool.reckoner;

import com.surelogic.sierra.tool.AbstractToolFactory;
import com.surelogic.sierra.tool.ITool;
import com.surelogic.sierra.tool.message.Config;

public class ReckonerFactory extends AbstractToolFactory {
	private static final String INFO = "<A HREF=\"http://www.surelogic.com\">Reckoner</A> is a static analysis tool created by SureLogic, Inc. that collects metrics about Java code.";

	public String getId() {
		return "reckoner";
	}
	
	public String getName() {
		return "Reckoner";
	}
	
	@Override
	public String getVersion() {
		return "1.0";
	}
	
	public String getHTMLInfo() {
		return INFO;
	}
	
	public ITool create(Config config) {
		return new Reckoner1_0Tool(this, config);
	}
}
