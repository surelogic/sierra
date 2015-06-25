package com.surelogic.sierra.tool.message;

import java.util.Collection;

import com.surelogic.common.tool.SureLogicToolsPropertiesUtility;

/**
 * Implementors of RunGenerator generally allow someone to build a
 * representation of a run. Possible implementations might represent a run in
 * memory, in the database, or in a message sent to a remote server. The output
 * of a RunGenerator is implementation specific, RunGenerator merely provides an
 * interface that allows runs to be built.
 */
public interface ScanGenerator {
  ScanGenerator uid(String uid);

  ScanGenerator javaVersion(String version);

  ScanGenerator javaVendor(String vendor);

  ScanGenerator project(String projectName);

  /**
   * The generated run will belong to the specified set of timeseries. This
   * method should never be called to build a run in the client database.
   *
   * @param timeseries
   * @return
   */
  ScanGenerator timeseries(Collection<String> timeseries);

  ScanGenerator user(String userName);

  ScanGenerator extension(String name, String version);

  /**
   * This is a string representation of the filter applied to this scan, as
   * would be produced by
   * {@link SureLogicToolsPropertiesUtility#toStringConciseExcludedFoldersAndPackages(String[], String[])}
   * 
   * @param filterString
   * @return
   */
  ScanGenerator externalFilter(String filterString);

  ArtifactGenerator build();

  /**
   * Finished is called when all scan/artifact generation is done
   *
   * @return the uuid of the scan;
   */
  public String finished();
}
