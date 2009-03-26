package com.surelogic.sierra.tool;

import java.io.File;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.tool.analyzer.*;
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
	File doc = config.getScanDocument();	
	ILazyArtifactGenerator generator;
    if (doc.getName().endsWith(SierraToolConstants.PARSED_ZIP_FILE_SUFFIX)) {
      generator = new LazyZipArtifactGenerator(config);
    } else {
      generator = new MessageArtifactFileGenerator(doc, config);
    }
    IToolInstance ti =  create(config.getProject(), generator, true);
    setupToolForProject(ti, config);
    return ti;
  }
  
  public IToolInstance create(String name, ILazyArtifactGenerator generator) {
    return create(name, generator, false);
  }
  
  protected abstract IToolInstance create(String name, ILazyArtifactGenerator generator, boolean close);
  
  public List<File> getRequiredJars() {
	  return Collections.emptyList();
  }
}
