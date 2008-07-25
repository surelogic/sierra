package com.surelogic.sierra.tool;

import java.net.URI;
import java.util.logging.Logger;

import com.surelogic.common.jobs.SLProgressMonitor;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.tool.analyzer.MessageArtifactFileGenerator;
import com.surelogic.sierra.tool.message.ArtifactGenerator;
import com.surelogic.sierra.tool.message.Config;
import com.surelogic.sierra.tool.targets.ToolTarget;

public abstract class AbstractTool implements ITool {
  protected static final Logger LOG = SLLogger.getLogger("sierra");
  protected static final int JAVA_SUFFIX_LEN = ".java".length();
  
  protected static void setupToolForProject(IToolInstance ti, Config config) {
    for(ToolTarget t : config.getTargets()) {
      ti.addTarget(t);
    }
    for(URI path : config.getPaths()) {
      ti.addToClassPath(path);
    }
    ti.setOption(IToolInstance.COMPLIANCE_LEVEL, config.getComplianceLevel());
    ti.setOption(IToolInstance.SOURCE_LEVEL, config.getSourceLevel());
    ti.setOption(IToolInstance.TARGET_LEVEL, config.getTargetLevel());
  }

  private final String description;
  private final String name;
  private final String title;
  private final String version;
  protected final boolean debug;

  /**
   * Perhaps this should read from a file
   */
  protected AbstractTool(String name, String version, String title, String desc, boolean debug) {
    this.name = name;
    this.version = version;
    this.title = title;
    this.description = desc;
    this.debug = debug;
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

  public IToolInstance create(Config config) {
    ArtifactGenerator generator = 
      new MessageArtifactFileGenerator(config.getScanDocument(), config);
    IToolInstance ti =  create(generator, true);
    setupToolForProject(ti, config);
    return ti;
  }
  
  public IToolInstance create(final ArtifactGenerator generator) {
    return create(generator, false);
  }
  
  protected abstract IToolInstance create(final ArtifactGenerator generator, boolean close);
  
  protected static TestCode getTestCode(String code) {
    if (code == null) {
      return TestCode.NONE;
    }
    return TestCode.valueOf(code);
  }
}
