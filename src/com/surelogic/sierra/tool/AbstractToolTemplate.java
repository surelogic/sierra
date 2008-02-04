package com.surelogic.sierra.tool;

import com.surelogic.common.SLProgressMonitor;
import com.surelogic.sierra.tool.AbstractTool;
import com.surelogic.sierra.tool.AbstractToolInstance;
import com.surelogic.sierra.tool.IToolInstance;
import com.surelogic.sierra.tool.message.ArtifactGenerator;

/**
 * Template for AbstractTool implementations
 * 
 * @author Edwin.Chan
 */
public abstract class AbstractToolTemplate extends AbstractTool {
  public AbstractToolTemplate(String version) {
    super("", version, "", "");
  }

  @Override
  protected IToolInstance create(ArtifactGenerator generator,
                                 SLProgressMonitor monitor, boolean close) {
    return new AbstractToolInstance(this, generator, monitor, close) {
      @Override
      protected void execute() throws Exception {      
        
      }
    };
  }
}
