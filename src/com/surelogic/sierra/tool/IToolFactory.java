package com.surelogic.sierra.tool;

import com.surelogic.sierra.tool.message.Config;

public interface IToolFactory {
	ITool create(Config config);
}
