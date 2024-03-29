package com.surelogic.sierra.tool;

//import java.io.File;
import java.io.File;
import java.util.Set;

//import com.surelogic.sierra.tool.message.Config;

/**
 * An extension to a tool
 * 
 * @author edwin
 */
public interface IToolExtension {
  String getTool();

  String getId();

  String getVersion();

  File getJar();

  boolean isCore();

  /**
   * Returns all possible artifact types that can be gen'd by this tool for db
   * bootstrapping
   */
  Set<ArtifactType> getArtifactTypes();

  /**
   * If overridden, should merge its results with super.getRequiredJars()
   * 
   * @return a list of jars/libraries required by the tool(s)
   */
  // List<File> getRequiredJars(Config config);
}
