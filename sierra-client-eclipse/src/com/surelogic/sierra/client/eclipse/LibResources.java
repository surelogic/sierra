package com.surelogic.sierra.client.eclipse;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public final class LibResources {
  /**
   * The name of the current version Sierra Ant tasks. This is used for the
   * directory created by the tool.
   */
  public static final String ANT_TASK_VERSION = "sierra-ant-5.0.0";

  /**
   * The name of the archive that contains the Sierra Ant tasks.
   */
  public static final String ANT_TASK_ZIP = "sierra-ant.zip";

  public static final String PATH = "/lib/";
  public static final String ANT_TASK_ZIP_PATHNAME = PATH + ANT_TASK_ZIP;

  public static InputStream getAntTaskZip() throws IOException {
    final URL url = LibResources.class.getResource(ANT_TASK_ZIP_PATHNAME);
    final InputStream is = url.openStream();
    return is;
  }
}
