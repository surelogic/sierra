package com.surelogic.sierra.tool;

import com.surelogic.common.jobs.*;
import com.surelogic.sierra.tool.analyzer.ILazyArtifactGenerator;
import com.surelogic.sierra.tool.message.Config;

/**
 * Template for AbstractTool implementations
 * 
 * @author Edwin.Chan
 */
public abstract class AbstractToolTemplate extends AbstractTool {
  public AbstractToolTemplate(IToolFactory f, Config config) {
    super(f, config);
  }

  @Override
  protected IToolInstance create(String name, ILazyArtifactGenerator generator, boolean close) {
    return new AbstractToolInstance(debug, this, generator, close) {
      @Override
      protected SLStatus execute(SLProgressMonitor mon) throws Exception {      
    	  throw new UnsupportedOperationException("Nothing to do yet");
      }
    };
  }
}
