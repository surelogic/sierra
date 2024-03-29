package com.surelogic.sierra.tool.message;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * 
 * Java class for FilterSet complex type. Conceptually, a filter set consists of
 * a list of finding types. It is referred to a a category in the user interface
 * and data access layers.
 * 
 * A filter set is uniquely identified by a uid, which in this case is the
 * string representation of a {@link UUID}. A filter set also possesses an
 * owner, which is referenced by the owner's server uid, and a revision.
 * 
 * The content of a filter set is represented by a list of parents, and a list
 * of filter entries. Each filter entry names a particular finding type, and
 * turns it on or off. The list of finding types comprising a given filter set
 * is the application of its filter entries to the union of the finding types of
 * its parents.
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FilterSet", propOrder = { "uid", "owner", "revision", "name", "info", "parent", "filter" })
public class FilterSet {
  @XmlElement(required = true)
  protected String uid;
  @XmlElement(required = true)
  protected String name;
  @XmlElement(required = true)
  protected String owner;
  @XmlElement(required = true)
  protected long revision;

  protected String info;

  protected List<String> parent;
  protected List<FilterEntry> filter;

  /**
   * Gets the value of the uid property.
   * 
   * @return possible object is {@link String }
   * 
   */
  public String getUid() {
    return uid;
  }

  /**
   * Sets the value of the uid property.
   * 
   * @param value
   *          allowed object is {@link String }
   * 
   */
  public void setUid(final String value) {
    uid = value;
  }

  public String getOwner() {
    return owner;
  }

  public void setOwner(final String owner) {
    this.owner = owner;
  }

  public long getRevision() {
    return revision;
  }

  public void setRevision(final long revision) {
    this.revision = revision;
  }

  public String getInfo() {
    return info;
  }

  public void setInfo(final String info) {
    this.info = info;
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
  public void setName(final String value) {
    name = value;
  }

  /**
   * Gets the value of the parent property.
   * 
   * <p>
   * This accessor method returns a reference to the live list, not a snapshot.
   * Therefore any modification you make to the returned list will be present
   * inside the JAXB object. This is why there is not a <CODE>set</CODE> method
   * for the parent property.
   * 
   * <p>
   * For example, to add a new item, do as follows:
   * 
   * <pre>
   * getParent().add(newItem);
   * </pre>
   * 
   * 
   * <p>
   * Objects of the following type(s) are allowed in the list {@link String }
   * 
   * 
   */
  public List<String> getParent() {
    if (parent == null) {
      parent = new ArrayList<>();
    }

    return parent;
  }

  /**
   * Gets the value of the filter property.
   * 
   * <p>
   * This accessor method returns a reference to the live list, not a snapshot.
   * Therefore any modification you make to the returned list will be present
   * inside the JAXB object. This is why there is not a <CODE>set</CODE> method
   * for the filter property.
   * 
   * <p>
   * For example, to add a new item, do as follows:
   * 
   * <pre>
   * getFilter().add(newItem);
   * </pre>
   * 
   * 
   * <p>
   * Objects of the following type(s) are allowed in the list
   * {@link FilterEntry }
   * 
   * 
   */
  public List<FilterEntry> getFilter() {
    if (filter == null) {
      filter = new ArrayList<>();
    }

    return filter;
  }
}
