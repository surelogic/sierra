/*
 * Created on Jan 18, 2008
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package com.surelogic.sierra.tool.ant;

import java.io.File;

import org.apache.tools.ant.*;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.compilers.*;
import org.apache.tools.ant.types.*;
import org.apache.tools.ant.util.StringUtils;

import com.surelogic.common.SLProgressMonitor;
import com.surelogic.sierra.tool.*;
import com.surelogic.sierra.tool.message.Config;
import com.surelogic.sierra.tool.targets.*;
import com.surelogic.sierra.tool.targets.IToolTarget.Type;;

public class SierraJavacAdapter extends DefaultCompilerAdapter {
  public boolean execute() throws BuildException {
    Config config = createConfig();
    ToolUtil.scan(config, new Monitor(), true);

    return false;
  }

  private Config createConfig() {
    Config config = new Config();
    setupConfig(config, false);
    logAndAddFilesToCompile(config);

    return config;
  }

  private void addPath(Config config, Type aux, Path path) {
    // TODO Auto-generated method stub
    path.list();
  }

  /**
   * Originally based on DefaultCompilerAdapter.setupJavacCommandlineSwitches()
   */
  protected Config setupConfig(Config cmd, boolean useDebugLevel) {
    Path classpath = getCompileClasspath();
    //  For -sourcepath, use the "sourcepath" value if present.
    //  Otherwise default to the "srcdir" value.
    Path sourcepath = null;
    if (compileSourcepath != null) {
      sourcepath = compileSourcepath;
    } else {
      sourcepath = src;
    }

    /*
    if (memoryMaximumSize != null) {
      if (!attributes.isForkedJavac()) {
        attributes.log("Since fork is false, ignoring "
            + "memoryMaximumSize setting.",
            Project.MSG_WARN);
      } else {
        cmd.createArgument().setValue(memoryParameterPrefix
            + "mx" + memoryMaximumSize);
      }
    }
    */
    
    if (destDir != null) {
      cmd.addTarget(new FullDirectoryTarget(Type.BINARY, 
                    destDir.toURI()));
    }

    addPath(cmd, Type.AUX, classpath);
    //  If the buildfile specifies sourcepath="", then don't
    //  output any sourcepath.
    if (sourcepath.size() > 0) {
      addPath(cmd, Type.SOURCE, sourcepath);
    }

    Path bp = getBootClassPath();
    if (bp.size() > 0) {
      addPath(cmd, Type.AUX, bp);
    }

    /*
    if (verbose) {
      cmd.createArgument().setValue("-verbose");
    }
    */
    
    return cmd;
  }

  /**
   * Based on DefaultCompilerAdapter.logAndAddFilesToCompile()
   */
  protected void logAndAddFilesToCompile(Config config) {
    attributes.log("Compilation for " + config.getProject(),
        Project.MSG_VERBOSE);

    StringBuffer niceSourceList = new StringBuffer("File");
    if (compileList.length != 1) {
      niceSourceList.append("s");
    }
    niceSourceList.append(" to be compiled:");

    niceSourceList.append(StringUtils.LINE_SEP);

    for (int i = 0; i < compileList.length; i++) {
      String arg = compileList[i].getAbsolutePath();
      config.addTarget(new FileTarget(Type.SOURCE, new File(arg).toURI(), null));
      niceSourceList.append("    ");
      niceSourceList.append(arg);
      niceSourceList.append(StringUtils.LINE_SEP);
    }
    /*

    if (attributes.getSourcepath() != null) {
      addPath(config, Type.SOURCE, attributes.getSourcepath());  
    } else {
      addPath(config, Type.SOURCE, attributes.getSrcdir());  
    }
    addPath(config, Type.AUX, attributes.getClasspath());
    */
    
    attributes.log(niceSourceList.toString(), Project.MSG_VERBOSE);
  }

  class Monitor implements SLProgressMonitor {
    public void beginTask(String name, int totalWork) {
      // TODO Auto-generated method stub

    }

    public void done() {
      // TODO Auto-generated method stub

    }

    public void error(String msg) {
      // TODO Auto-generated method stub

    }

    public void error(String msg, Throwable t) {
      // TODO Auto-generated method stub

    }

    public void failed(String msg) {
      // TODO Auto-generated method stub

    }

    public void failed(String msg, Throwable t) {
      // TODO Auto-generated method stub

    }

    public Throwable getFailureTrace() {
      // TODO Auto-generated method stub
      return null;
    }

    public void internalWorked(double work) {
      // TODO Auto-generated method stub

    }

    public boolean isCanceled() {
      // TODO Auto-generated method stub
      return false;
    }

    public void setCanceled(boolean value) {
      // TODO Auto-generated method stub

    }

    public void setTaskName(String name) {
      // TODO Auto-generated method stub

    }

    public void subTask(String name) {
      // TODO Auto-generated method stub

    }

    public void worked(int work) {
      // TODO Auto-generated method stub

    }

  }
}
