package com.surelogic.sierra.tool.message;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessOrder;
import javax.xml.bind.annotation.XmlAccessorOrder;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorOrder(XmlAccessOrder.ALPHABETICAL)
@XmlType
@XmlRootElement
public class SyncProjectResponse {

  private List<SyncTrailResponse> trails;

  private String scanFilter;

  private long commitRevision;

  public List<SyncTrailResponse> getTrails() {
    if (trails == null) {
      trails = new ArrayList<>();
    }
    return trails;
  }

  public void setTrails(final List<SyncTrailResponse> trails) {
    this.trails = trails;
  }

  public long getCommitRevision() {
    return commitRevision;
  }

  public void setCommitRevision(final long commitRevision) {
    this.commitRevision = commitRevision;
  }

  public String getScanFilter() {
    return scanFilter;
  }

  public void setScanFilter(final String scanFilter) {
    this.scanFilter = scanFilter;
  }

}
