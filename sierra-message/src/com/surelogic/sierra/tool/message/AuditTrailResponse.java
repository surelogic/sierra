package com.surelogic.sierra.tool.message;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessOrder;
import javax.xml.bind.annotation.XmlAccessorOrder;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorOrder(XmlAccessOrder.ALPHABETICAL)
@XmlType
@XmlRootElement
public class AuditTrailResponse {
  private List<TrailObsoletion> obsolete;
  private List<AuditTrailUpdate> update;

  public List<AuditTrailUpdate> getUpdate() {
    return update;
  }

  public void setUpdate(List<AuditTrailUpdate> update) {
    this.update = update;
  }

  public List<TrailObsoletion> getObsolete() {
    return obsolete;
  }

  public void setObsolete(List<TrailObsoletion> obsolete) {
    this.obsolete = obsolete;
  }
}
