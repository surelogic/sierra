package com.surelogic.sierra.tool;

import java.util.*;

import com.surelogic.common.SLProgressMonitor;
import com.surelogic.sierra.tool.message.ArtifactGenerator;

/**
 * API for querying the tools about what they are,
 * and what artifacts they can create
 * 
 * @author Edwin.Chan 
 */
public interface ITool {
  /**
   * e.g. "FindBugs"
   */
  String getName();

  /**
   * e.g. "1.3.0"
   */
  String getVersion(); 
  
  /**
   * e.g. "FindBugs (TM)"
   */
  String getTitle();

  /**
   * e.g. "<a href="http://findbugs.sf.net">FindBugs</a> is a blahblahblah"
   */
  String getHtmlDescription();

  /**
   * Returns all possible artifact types that can be gen'd by this tool 
   * for db bootstrapping
   */
  Set<String> getArtifactTypes();
  
  /**
   * Creates an instance of the tool to do one scan
   */
  IToolInstance create(ArtifactGenerator generator, SLProgressMonitor monitor);
}
