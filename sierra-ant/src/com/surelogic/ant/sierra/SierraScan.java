package com.surelogic.ant.sierra;

import static com.surelogic.sierra.tool.SierraToolConstants.PARSED_FILE_SUFFIX;
import static com.surelogic.sierra.tool.SierraToolConstants.PARSED_ZIP_FILE_SUFFIX;
import static com.surelogic.sierra.tool.SierraToolConstants.USE_ZIP;

import java.io.File;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Javac;
import org.apache.tools.ant.taskdefs.compilers.CompilerAdapter;

import com.surelogic.common.jobs.remote.RemoteSLJobConstants;

public class SierraScan extends Javac {

  /**
   * The location of sierra-ant
   */
  private String home;

  /**
   * The intended location of the resulting scan document
   */
  private String document;

  /**
   * The name of the project being scanned
   */
  private String project;

  private File properties;

  public SierraScan() {
    super();
    setIncludeantruntime(false);
  }

  public String getHome() {
    return home;
  }

  public void setHome(String home) {
    this.home = home;
  }

  public String getProjectName() {
    return project;
  }

  public void setProjectName(String p) {
    project = p;
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
