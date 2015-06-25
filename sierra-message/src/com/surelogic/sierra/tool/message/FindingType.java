package com.surelogic.sierra.tool.message;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Java class for FindingType complex type.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FindingType", propOrder = { "id", "artifact", "shortMessage", "info", "name" })
public class FindingType {
  @XmlElement(required = true)
  protected String id;
  protected List<ArtifactType> artifact;
  protected String shortMessage;
  protected String info;
  @XmlElement(required = true)
  protected String name;

  /**
   * Gets the value of the id property.
   *
   * @return possible object is {@link String }
   *
   */
  public String getId() {
    return id;
  }

  /**
   * Sets the value of the id property.
   *
   * @param value
   *          allowed object is {@link String }
   *
   */
  public void setId(String value) {
    this.id = value;
  }

  /**
   * Gets the value of the artifact property.
   *
   * <p>
   * This accessor method returns a reference to the live list, not a snapshot.
   * Therefore any modification you make to the returned list will be present
   * inside the JAXB object. This is why there is not a <CODE>set</CODE> method
   * for the artifact property.
   *
   * <p>
   * For example, to add a new item, do as follows:
   * 
   * <pre>
   * getArtifact().add(newItem);
   * </pre>
   *
   *
   * <p>
   * Objects of the following type(s) are allowed in the list
   * {@link ArtifactType }
   *
   *
   */
  public List<ArtifactType> getArtifact() {
    if (artifact == null) {
      artifact = new ArrayList<>();
    }

    return this.artifact;
  }

  /**
   * Gets the value of the shortMessage property.
   *
   * @return possible object is {@link String }
   *
   */
  public String getShortMessage() {
    return shortMessage;
  }

  /**
   * Sets the value of the shortMessage property.
   *
   * @param value
   *          allowed object is {@link String }
   *
   */
  public void setShortMessage(String value) {
    this.shortMessage = value;
  }

  /**
   * Gets the value of the info property.
   *
   * @return possible object is {@link String }
   *
   */
  public String getInfo() {
    return info;
  }

  /**
   * Sets the value of the info property.
   *
   * @param value
   *          allowed object is {@link String }
   *
   */
  public void setInfo(String value) {
    this.info = value;
  }

  /**
   * Gets the value of the name property.
   *
   * @return possible object is {@link String }
   *
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the value of the name property.
   *
   * @param value
   *          allowed object is {@link String }
   *
   */
  public void setName(String value) {
    this.name = value;
  }
}
