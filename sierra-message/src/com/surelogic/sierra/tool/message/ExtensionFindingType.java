package com.surelogic.sierra.tool.message;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(propOrder = { "id", "name", "shortMessage", "info" })
@XmlAccessorType(XmlAccessType.FIELD)
public class ExtensionFindingType {
  @XmlElement(required = true)
  protected String id;
  protected String shortMessage;
  protected String info;
  @XmlElement(required = true)
  protected String name;

  public String getId() {
    return id;
  }

  public void setId(final String id) {
    this.id = id;
  }

  public String getShortMessage() {
    return shortMessage;
  }

  public void setShortMessage(final String shortMessage) {
    this.shortMessage = shortMessage;
  }

  public String getInfo() {
    return info;
  }

  public void setInfo(final String info) {
    this.info = info;
  }

  public String getName() {
    return name;
  }

  public void setName(final String name) {
    this.name = name;
  }

}
