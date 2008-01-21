/*
 * Created on Jan 17, 2008
 */
package com.surelogic.sierra.tool.ant;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Javac;
import org.apache.tools.ant.taskdefs.compilers.CompilerAdapter;
//import org.apache.tools.ant.util.GlobPatternMapper;
//import org.apache.tools.ant.util.SourceFileScanner;

public class SierraScan extends Javac { 
  /***************************************************************************
   * Ant Task Attributes
   **************************************************************************/

  // Optional attribute, if present, we send the scan document will be sent to
  // this server
  private String server = null;

  // Optional attribute, required when we send the scan document will be sent
  // to this server.
  private String user;
  // Optional attribute, required when we send the scan document will be sent
  // to this server.
  private String password;
  
  // Optional, but req'd if URL is set. Comma-separated list of qualifiers
  private final List<String> qualifiers = new ArrayList<String>();
  
  @Override
  protected void scanDir(File srcDir, File destDir, String[] files) {
    /*
    GlobPatternMapper m = new GlobPatternMapper();
    m.setFrom("*.java");
    m.setTo("*.class");
    SourceFileScanner sfs = new SourceFileScanner(this);
    File[] newFiles = sfs.restrictAsFiles(files, srcDir, destDir, m);
    */
    File[] newFiles = new File[files.length];
    int i = 0;
    for(String name : files) {
      newFiles[i] = new File(srcDir, name); 
      i++;
    }

    if (newFiles.length > 0) {
        File[] newCompileList
            = new File[compileList.length + newFiles.length];
        System.arraycopy(compileList, 0, newCompileList, 0,
                compileList.length);
        System.arraycopy(newFiles, 0, newCompileList,
                compileList.length, newFiles.length);
        compileList = newCompileList;
    }
  }
  
  /**
   * Modified from Javac.compile()
   */
  @Override
  protected void compile() {
    File destDir = this.getDestdir();
    System.out.println(destDir.getAbsolutePath());
    
    if (compileList.length > 0) {
        log("Scanning " + compileList.length + " source file"
            + (compileList.length == 1 ? "" : "s")
            //+ (destDir != null ? " to " + destDir : "")
            );

        if (listFiles) {
            for (int i = 0; i < compileList.length; i++) {
              String filename = compileList[i].getAbsolutePath();
              log(filename);
            }
        }

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
  
  /***************************************************************************
   * Getters and Setters for attributes
   **************************************************************************/

  public void setServer(String server) {
    this.server = server;
  }

  public String getServer() {
    return server;
  }

  public String getUser() {
    return user;
  }

  public void setUser(String user) {
    this.user = user;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }
  
  /**
   * @return the serverQualifier
   */
  public final List<String> getQualifiers() {
    return qualifiers;
  }

  /**
   * @param serverQualifier
   *            the serverQualifier to set
   */
  public final void setQualifiers(String qualifiers) {
    String[] q = qualifiers.split(",");
    for (String qualifier : q) {
      this.qualifiers.add(qualifier.trim());
    }
  }
}
