package com.surelogic.sierra.tool;

import java.net.URL;

import com.surelogic.common.SLProgressMonitor;
import com.surelogic.sierra.tool.targets.IToolTarget;

/**
 * Represents one scan using the given tool
 * 
 * Note that the methods here do not depend on any plugins in case
 * we want to run the tools in a separate JVM
 * 
 * @author Edwin.Chan
 */
public interface IToolInstance extends ITool, Runnable {
  SLProgressMonitor getProgressMonitor();

  /**
   * Adds a target that will be scanned/processed/etc by the tool 
   * implementation on run()
   */
  void addTarget(IToolTarget target); 
  
  /**
   * Adds a jar, class, etc that may be used if the tool requires 
   * the project class path
   */
  void addToClassPath(URL loc); 
}
