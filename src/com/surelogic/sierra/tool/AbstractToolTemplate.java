package com.surelogic.sierra.tool;

import com.surelogic.common.jobs.*;
import com.surelogic.sierra.tool.message.ArtifactGenerator;

/**
 * Template for AbstractTool implementations
 * 
 * @author Edwin.Chan
 */
public abstract class AbstractToolTemplate extends AbstractTool {
  public AbstractToolTemplate(String version, boolean debug) {
    super("", version, "", "", debug);
  }

  @Override
  protected IToolInstance create(ArtifactGenerator generator, boolean close) {
    return new AbstractToolInstance(debug, this, generator, close) {
      @Override
      protected SLStatus execute(SLProgressMonitor mon) throws Exception {      
    	  throw new UnsupportedOperationException("Nothing to do yet");
      }
    };
  }
}
