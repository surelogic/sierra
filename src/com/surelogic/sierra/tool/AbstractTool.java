package com.surelogic.sierra.tool;

import java.net.URI;
import java.util.logging.Logger;

import com.surelogic.common.SLProgressMonitor;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.tool.analyzer.MessageArtifactFileGenerator;
import com.surelogic.sierra.tool.message.ArtifactGenerator;
import com.surelogic.sierra.tool.message.Config;
import com.surelogic.sierra.tool.targets.ToolTarget;

public abstract class AbstractTool implements ITool {
  protected static final Logger LOG = SLLogger.getLogger("sierra");
  
  protected static void setupToolForProject(IToolInstance ti, Config config) {
    for(ToolTarget t : config.getTargets()) {
      ti.addTarget(t);
    }
    for(URI path : config.getPaths()) {
      ti.addToClassPath(path);
    }
  }

  private final String description;
  private final String name;
  private final String title;
  private final String version;

  /**
   * Perhaps this should read from a file
   */
  protected AbstractTool(String name, String version, String title, String desc) {
    this.name = name;
    this.version = version;
    this.title = title;
    this.description = desc;
  }

  public final String getHtmlDescription() {
    return description;
  }

  public final String getName() {
    return name;
  }

  public final String getTitle() {
    return title;
  }

  public final String getVersion() {
    return version;
  }

  public IToolInstance create(Config config, SLProgressMonitor monitor) {
    ArtifactGenerator generator = 
      new MessageArtifactFileGenerator(config.getScanDocument(), config);
    IToolInstance ti =  create(generator, monitor, true);
    setupToolForProject(ti, config);
    return ti;
  }
  
  public IToolInstance create(final ArtifactGenerator generator, 
                                    final SLProgressMonitor monitor) {
    return create(generator, monitor, false);
  }
  
  protected abstract IToolInstance create(final ArtifactGenerator generator, 
                                 final SLProgressMonitor monitor, boolean close);
}
