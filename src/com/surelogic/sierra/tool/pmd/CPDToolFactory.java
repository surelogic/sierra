package com.surelogic.sierra.tool.pmd;

import java.util.Collections;
import java.util.Set;

import com.surelogic.sierra.tool.*;
import com.surelogic.sierra.tool.analyzer.ILazyArtifactGenerator;
import com.surelogic.sierra.tool.message.Config;

public class CPDToolFactory extends AbstractToolFactory {	
//	@Override
	public Set<ArtifactType> getArtifactTypes() {
		// TODO Auto-generated method stub
		return Collections.emptySet();
	}
	
	@Override
	protected IToolInstance create(Config config,
			ILazyArtifactGenerator generator, boolean close) {
		return new CPD4_1Tool(this, config, generator, close);
	}


}
