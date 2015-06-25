package com.surelogic.sierra.tool.message;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ListCategoryResponse {

  protected List<ServerRevision> servers;

  protected List<FilterSet> filterSets;

  protected List<String> deletions;

  protected List<ExtensionName> dependencies;

  public List<ServerRevision> getServerRevisions() {
    if (servers == null) {
      servers = new ArrayList<>();
    }
    return servers;
  }

  public List<FilterSet> getFilterSets() {
    if (filterSets == null) {
      filterSets = new ArrayList<>();
    }
    return filterSets;
  }

  public List<String> getDeletions() {
    if (deletions == null) {
      deletions = new ArrayList<>();
    }
    return deletions;
  }

  public List<ExtensionName> getDependencies() {
    if (dependencies == null) {
      dependencies = new ArrayList<>();
    }
    return dependencies;
  }

}
