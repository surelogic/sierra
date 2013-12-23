package com.surelogic.sierra.tool.pmd;

import java.util.*;

import com.surelogic.sierra.tool.*;
import com.surelogic.sierra.tool.analyzer.ILazyArtifactGenerator;
import com.surelogic.sierra.tool.message.Config;

public class CPDToolFactory extends AbstractToolFactory {	
	@Override
  public Collection<IToolExtension> getExtensions() {
		// TODO Auto-generated method stub
		return Collections.emptySet();
	}
	
	@Override
	protected IToolInstance create(Config config,
			ILazyArtifactGenerator generator, boolean close) {
		return new CPD5_0Tool(this, config, generator, close);
	}
}
