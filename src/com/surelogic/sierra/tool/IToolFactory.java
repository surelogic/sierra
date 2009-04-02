package com.surelogic.sierra.tool;

import java.io.File;

import com.surelogic.sierra.tool.message.Config;

public interface IToolFactory {
	String getId();
	String getName();
	String getHTMLInfo();
	boolean isProduction();
	
	void init(File toolHome);
	ITool create(Config config);
}
