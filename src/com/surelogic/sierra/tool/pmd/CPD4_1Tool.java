package com.surelogic.sierra.tool.pmd;

import com.surelogic.sierra.tool.analyzer.ILazyArtifactGenerator;
import com.surelogic.sierra.tool.message.Config;

public class CPD4_1Tool extends AbstractCPDTool {
  public CPD4_1Tool(CPDToolFactory f, Config config, ILazyArtifactGenerator generator, boolean close) {
    super(f, config, generator, close);
  }
}
