package com.surelogic.sierra.ant;

import static com.surelogic.sierra.tool.SierraToolConstants.PARSED_FILE_SUFFIX;
import static com.surelogic.sierra.tool.SierraToolConstants.PARSED_ZIP_FILE_SUFFIX;
import static com.surelogic.sierra.tool.SierraToolConstants.USE_ZIP;

import java.io.File;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Javac;
import org.apache.tools.ant.taskdefs.compilers.CompilerAdapter;

import com.surelogic.NonNull;
import com.surelogic.Nullable;
import com.surelogic.common.SLUtility;
import com.surelogic.common.jobs.remote.RemoteSLJobConstants;

/**
 * Sierra scan Ant task.
 */
public class SierraScan extends Javac {
  /**
   * The location of built Sierra Ant task.
   */
  private String sierraAntHome;

  /**
   * The name of the project being scanned.
   */
  private String projectName;

  /**
   * The name of the directory to place the scan zip.
   */
  @Nullable
  private String sierraScanDir;

  /**
   * The location of the 'surelogic-tools.properties' file.
   */
  @Nullable
  private String surelogicToolsPropertiesFile;

  /**
   * The location of the Sierra ant task.
   * 
   * @return the location of the Sierra ant task.
   */
  public String getSierraAntHome() {
    return sierraAntHome;
  }

  /**
   * The location of the Sierra ant task.
   * 
   * @param value
   *          the location of the Sierra ant task.
   */
  public void setSierraAntHome(String value) {
    sierraAntHome = value;
  }

  /**
   * Human readable name for the project being scanned.
   * 
   * @return the name of the project being scanned.
   */
  public String getProjectName() {
    return projectName;
  }

  /**
   * Human readable name for the project being scanned.
   * 
   * @param value
   *          the name of the project being scanned.
   */
  public void setProjectName(String value) {
    projectName = value;
  }

  /**
   * The name of the directory to place the scan zip.
   * 
   * @return the name of the directory to place the scan zip.
   */
  @Nullable
  public String getSierraScanDir() {
    return sierraScanDir;
  }

  /**
   * The name of the output file.
   * 
   * @param value
   *          the name of the output file.
   */
  public void setSierraScanDir(String value) {
    sierraScanDir = value;
  }

  /**
   * The path to the surelogic-tools.properties file.
   * 
   * @return the path to the surelogic-tools.properties file.
   */
  @Nullable
  public String getSurelogicToolsPropertiesFile() {
    return surelogicToolsPropertiesFile;
  }

  /**
   * The path to the surelogic-tools.properties file.
   * 
   * @param value
   *          the path to the surelogic-tools.properties file.
   */
  public void setSurelogicToolsPropertiesFile(String value) {
    surelogicToolsPropertiesFile = value;
  }

  /**
   * Gets the Sierra ant task directory.
   * 
   * @return the Sierra ant task directory.
   * @throws BuildException
   *           if the directory doesn't exist on the disk.
   */
  public File getSierraAntHomeAsFile() {
    final File result = new File(sierraAntHome);
    if (!result.isDirectory())
      throw new BuildException("SierraAntHome does not exist: " + result.getAbsolutePath());
    return result;
  }

  /**
   * Gets the location to place the output scan zip into
   * 
   * @return an file to put the scan in.
   */
  @NonNull
  public File getSierraScanDirAsFile() {
    final String outDir;
    if (sierraScanDir == null)
      outDir = ".";
    else
      outDir = sierraScanDir;
    final File result = new File(outDir);
    if (!result.isDirectory())
      throw new BuildException("Sierra scan output directory does not exist: " + result.getAbsolutePath());
    return result;
  }

  /**
   * Gets the location of the 'surelogic-tools.properties' file. If none is set
   * the current working directory is checked.
   * 
   * @return the location of the 'surelogic-tools.properties' file, or
   *         {@code null} if none.
   * @throws BuildException
   *           if the file doesn't exist on the disk and the Ant script
   *           specified a precise location.
   */
  @Nullable
  public File getSurelogicToolsPropertiesAsFile() {
    final boolean pathnameSet = surelogicToolsPropertiesFile != null;
    final String pathname = pathnameSet ? surelogicToolsPropertiesFile : "./" + SLUtility.SL_TOOLS_PROPS_FILE;
    final File result = new File(pathname);
    if (result.isFile())
      return result;
    else {
      if (pathnameSet)
        throw new BuildException("SierraAntHome does not exist: " + result.getAbsolutePath());
      else
        return null;
    }
  }

  /**
   * The intended location of the resulting scan document
   */
  private String document;

  private File properties;

  public SierraScan() {
    super();
    setIncludeantruntime(false);
  }

  public String getDocument() {
    return document;
  }

  public File getScanFile() {
    return new File(getDocument() + (USE_ZIP ? PARSED_ZIP_FILE_SUFFIX : PARSED_FILE_SUFFIX));
  }

  public void setDocument(String doc) {
    document = doc;
  }

  @Override
  protected void scanDir(File srcDir, File destDir, String[] files) {
    File[] newFiles = new File[files.length];
    int i = 0;
    for (String name : files) {
      newFiles[i] = new File(srcDir, name);
      i++;
    }

    if (newFiles.length > 0) {
      File[] newCompileList = new File[compileList.length + newFiles.length];
      System.arraycopy(compileList, 0, newCompileList, 0, compileList.length);
      System.arraycopy(newFiles, 0, newCompileList, compileList.length, newFiles.length);
      compileList = newCompileList;
    }
  }

  public File getProperties() {
    return properties;
  }

  public void setProperties(File properties) {
    this.properties = properties;
  }

  @Override
  public void execute() {
    log(String.format("Writing scan document to %s.", getScanFile().getAbsolutePath()));
    super.execute();
  }

  /**
   * Modified from Javac.compile()
   */
  @Override
  protected void compile() {
    File destDir = getDestdir();

    if (compileList.length > 0) {
      log("Scanning " + compileList.length + " source file" + (compileList.length == 1 ? "" : "s") + " in "
          + destDir.getAbsolutePath());

      if (listFiles) {
        for (int i = 0; i < compileList.length; i++) {
          String filename = compileList[i].getAbsolutePath();
          log(filename);
        }
      }

      System.setProperty(RemoteSLJobConstants.RUNNING_REMOTELY, "true");
      CompilerAdapter adapter = new SierraJavacAdapter(this);

      // now we need to populate the compiler adapter
      adapter.setJavac(this);

      // finally, lets execute the compiler!!
      if (!adapter.execute()) {
        if (failOnError) {
          throw new BuildException("Failed", getLocation());
        } else {
          log("Failed", Project.MSG_ERR);
        }
      }
    }
  }
}
