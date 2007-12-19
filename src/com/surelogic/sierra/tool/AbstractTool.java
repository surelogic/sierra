package com.surelogic.sierra.tool;

public abstract class AbstractTool implements ITool {
  private final String description;
  private final String name;
  private final String title;
  private final String version;

  /**
   * Perhaps this should read from a file
   */
  protected AbstractTool(String name, String version, String title, String desc) {
    this.name = name;
    this.version = version;
    this.title = title;
    this.description = desc;
  }

  public final String getHtmlDescription() {
    return description;
  }

  public final String getName() {
    return name;
  }

  public final String getTitle() {
    return title;
  }

  public final String getVersion() {
    return version;
  }

}
