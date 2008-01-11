/*
 * Created on Jan 11, 2008
 */
package com.surelogic.sierra.tool;

import java.util.Collections;
import java.util.Set;

import com.surelogic.common.SLProgressMonitor;
import com.surelogic.sierra.tool.message.ArtifactGenerator;

public class RemoteTool extends AbstractTool {
  public RemoteTool() {
    super("Remote", "1.0", "Remote", "Tool for running other tools in another JVM");
  }
  
  public Set<String> getArtifactTypes() {
    return Collections.emptySet();
  }
  
  public IToolInstance create(ArtifactGenerator generator, SLProgressMonitor monitor) {
    // FIX generator should be created in the other JVM
    // TODO Auto-generated method stub
    return null;
  }
}
