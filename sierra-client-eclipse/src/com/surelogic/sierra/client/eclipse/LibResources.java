package com.surelogic.sierra.client.eclipse;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public final class LibResources {

  /**
   * The name of the archive that contains the Sierra Ant tasks.
   * <p>
   * Within this zip should be a single directory of the form
   * <tt>sierra-ant</tt>. The name of the zip file is versioned when it is saved
   * to the disk, e.g., <tt>sierra-ant-5.6.0.zip</tt>.
   */
  public static final String ANT_TASK_ZIP = "sierra-ant.zip";

  /**
   * The name of the archive that contains the Sierra Maven plugin.
   * <p>
   * Within this zip should be a single directory of the form
   * <tt>sierra-maven</tt>. The name of the zip file is versioned when it is
   * saved to the disk, e.g., <tt>sierra-maven-5.6.0.zip</tt>.
   */
  public static final String MAVEN_PLUGIN_ZIP = "sierra-maven.zip";

  public static final String PATH = "/lib/";
  public static final String ANT_TASK_ZIP_PATHNAME = PATH + ANT_TASK_ZIP;

  public static InputStream getAntTaskZip() throws IOException {
    final URL url = LibResources.class.getResource(ANT_TASK_ZIP_PATHNAME);
    final InputStream is = url.openStream();
    return is;
  }
}
