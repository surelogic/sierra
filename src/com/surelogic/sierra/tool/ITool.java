package com.surelogic.sierra.tool;

import java.io.File;
import java.util.*;

import com.surelogic.sierra.tool.analyzer.ILazyArtifactGenerator;

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
  Set<ArtifactType> getArtifactTypes();
  
  /**
   * Returns a list of jars required by the tool(s)
   */
  List<File> getRequiredJars();
  
  /**
   * Creates an instance of the tool to do one scan
   */
  IToolInstance create();
  
  /**
   * Creates an instance of the tool to do one scan
   */
  IToolInstance create(String name, ILazyArtifactGenerator generator);
}
