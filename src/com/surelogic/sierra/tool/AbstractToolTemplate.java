package com.surelogic.sierra.tool;

import com.surelogic.common.jobs.SLProgressMonitor;
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
  protected IToolInstance create(ArtifactGenerator generator,
                                 SLProgressMonitor monitor, boolean close) {
    return new AbstractToolInstance(debug, this, generator, monitor, close) {
      @Override
      protected void execute() throws Exception {      
        // Nothing to do yet
      }
    };
  }
}
