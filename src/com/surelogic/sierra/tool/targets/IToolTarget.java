package com.surelogic.sierra.tool.targets;

import java.net.*;

/**
 * This represents some files to be processed by a tool
 * Note that these should be serializable in case the 
 * tools are running in a separate JVM
 * 
 * @author Edwin.Chan
 */
public interface IToolTarget {
  enum Kind {
    FILE, DIRECTORY, JAR  
  }
  
  /**
   * @return true if this refers to Java source files
   */
  boolean isSource();
  
  Kind getKind();
  
  /**
   * @return the location of the file, directory, or jar
   */
  URI getLocation();

  /**   
   * @param relativePath The path from the location above
   * @return true if the resource at the relative path should
   *         not be processed
   */
  boolean exclude(String relativePath);
  
  /**
   * @return A list of URIs for the various files in this target
   */
  Iterable<URI> getFiles();
}
