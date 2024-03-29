package com.surelogic.sierra.tool;

import java.io.Serializable;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * A descriptor for an artifact that can be generated by a tool
 * 
 * @author edwin
 */
public class ArtifactType implements Comparable<ArtifactType>, Serializable {
  private static final long serialVersionUID = 3705154196337992228L;

  public final String tool;
  public final String toolVersion;
  public final String plugin;
  public final String type;
  private String category;
  private boolean includeInScan;
  private boolean isComplete = false;
  private String findingType = null;

  private ArtifactType(String tool, String version, String plugin, String type, String category, boolean include) {
    this.tool = tool;
    this.toolVersion = version;
    this.plugin = plugin;
    this.type = type;
    this.category = category;
    includeInScan = include;
  }

  public static ArtifactType create(IToolFactory factory, Manifest manifest, String plugin, String type, String category) {
    return create(factory.getId(), factory.getVersion(), manifest, plugin, type, category);
  }

  public static ArtifactType create(String toolId, String toolVersion, Manifest manifest, String plugin, String type,
      String category) {
    final Attributes findingTypeMap, categoryMap, scanFilterBlacklist;
    if (manifest != null) {
      findingTypeMap = manifest.getAttributes(ToolUtil.FINDING_TYPE_MAPPING_KEY);
      categoryMap = manifest.getAttributes(ToolUtil.CATEGORY_MAPPING_KEY);
      scanFilterBlacklist = manifest.getAttributes(ToolUtil.SCAN_FILTER_BLACKLIST_KEY);
    } else {
      findingTypeMap = categoryMap = scanFilterBlacklist = null;
    }
    // Code below assumes that the artifact type is not mapped to an existing
    // finding type,
    // and uses the type name to lookup defaults
    boolean isComplete = false;
    if (categoryMap != null) {
      String tmpCategory = categoryMap.getValue(type);
      if (tmpCategory != null) {
        category = tmpCategory;
      }
      isComplete = true;
    }
    boolean includeInScan = true;
    if (scanFilterBlacklist != null) {
      includeInScan = scanFilterBlacklist.getValue(type) != null;
    }
    ArtifactType t = new ArtifactType(toolId, toolVersion, plugin, type, category, includeInScan);
    if (findingTypeMap != null) {
      String findingType = findingTypeMap.getValue(type);
      if (findingType != null) {
        t.setFindingType(findingType);
      }
      isComplete = true;
    }
    t.isComplete = isComplete;
    return t;
  }

  public String setFindingType(String type) {
    return (findingType = type);
  }

  public String getFindingType() {
    return findingType;
  }

  public String setCategory(String cat) {
    return (category = cat);
  }

  public String getCategory() {
    return category;
  }

  public void includeInScan(boolean val) {
    includeInScan = val;
  }

  public boolean includeInScan() {
    return includeInScan;
  }

  public boolean isComplete() {
    return isComplete;
  }

  @Override
  public int hashCode() {
    return tool.hashCode() + toolVersion.hashCode() + type.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof ArtifactType) {
      ArtifactType a = (ArtifactType) o;
      return tool.equals(a.tool) && toolVersion.equals(a.toolVersion) && type.equals(a.type);
    }
    return false;
  }

  public int compareTo(ArtifactType a) {
    int rv = tool.compareTo(a.tool);
    if (rv == 0) {
      rv = toolVersion.compareTo(a.toolVersion);
      if (rv == 0) {
        rv = type.compareTo(a.type);
      }
    }
    return rv;
  }
}
