package com.surelogic.sierra.tool;

import java.net.URI;

import com.surelogic.common.jobs.SLJob;
import com.surelogic.sierra.tool.message.ArtifactGenerator;
import com.surelogic.sierra.tool.targets.IToolTarget;

/**
 * Represents one scan using the given tool
 * 
 * Note that the methods here do not depend on any plugins in case we want to
 * run the tools in a separate JVM
 * 
 * @author Edwin.Chan
 */
public interface IToolInstance extends SLJob {
  /**
   * e.g. "FindBugs"
   */
  String getId();

  /**
   * e.g. "1.3.0"
   */
  String getVersion();

  /**
   * e.g. "FindBugs (TM)"
   */
  String getName();

  /**
   * e.g. "<a href="http://findbugs.sf.net">FindBugs</a> is a blahblahblah"
   */
  String getHTMLInfo();

  /**
   * Returns the stream being used to report results from this tool
   * 
   * @return the ArtifactGenerator being used with this IToolInstance
   */
  ArtifactGenerator getGenerator();

  /**
   * Adds a target that will be scanned/processed/etc by the tool implementation
   * on run()
   */
  void addTarget(IToolTarget target);

  /**
   * Adds a jar, class, etc that may be used if the tool requires the project
   * class path
   */
  void addToClassPath(URI loc);

  void reportError(String msg, Throwable t);

  void reportError(String msg);

  void setOption(String key, String value);

  /**
   * An option to set the Java language compliance level
   */
  String COMPLIANCE_LEVEL = "compliance.level";

  /**
   * An option to set the Java source language level
   */
  String SOURCE_LEVEL = "source.level";

  /**
   * An option to set the Java target binary level
   */
  String TARGET_LEVEL = "target.level";
}
