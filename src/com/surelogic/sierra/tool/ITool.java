package com.surelogic.sierra.tool;

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
}
