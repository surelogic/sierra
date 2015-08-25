package com.surelogic.sierra.tool.pmd;

import java.io.File;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.CommandlineJava;
import org.apache.tools.ant.types.Path;

import com.surelogic.common.core.jobs.EclipseLocalConfig;
import com.surelogic.common.jobs.remote.*;
import com.surelogic.sierra.tool.SierraToolConstants;

public class LocalPMDRuleSetQueryJob extends AbstractLocalSLJob<EclipseLocalConfig> {
  // public static final String RULESETS_ZIP = "rulesets.zip";
  final String version;

  protected LocalPMDRuleSetQueryJob(String name, int work, EclipseLocalConfig config, String v) {
    super(name, work, config);
    version = v;
  }

  @Override
  protected Class<? extends AbstractRemoteSLJob> getRemoteClass() {
    return RemotePMDRuleSetQueryJob.class;
  }

  @Override
  protected void setupClassPath(ConfigHelper util, CommandlineJava cmdj, Project proj, Path path) {
    util.addPluginToPath(COMMON_PLUGIN_ID);
    util.addPluginJarsToPath(COMMON_PLUGIN_ID, "lib/runtime/commons-lang3-3.4.jar");
    util.addPluginAndJarsToPath(SierraToolConstants.PMD_PLUGIN_ID, "lib");
    // TODO anything else?

    util.addPluginToPath(SierraToolConstants.TOOL_PLUGIN_ID);

    for (File jar : util.getPath()) {
      addToPath(proj, path, jar, true);
    }
  }

  @Override
  protected void finishSetupJVM(boolean debug, CommandlineJava cmdj, Project proj) {
    File result = new File(config.getRunDirectory());
    cmdj.createVmArgument().setValue("-D" + RemotePMDRuleSetQueryJob.QUERY_RESULTS_DIR + "=" + result.getAbsolutePath());
    cmdj.createVmArgument().setValue("-D" + RemotePMDRuleSetQueryJob.PMD_VERSION + "=" + version);

    // System.out.println("Created PMD query classpath: "+cmdj.getClasspath());
  }
}
