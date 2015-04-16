package com.surelogic.sierra.tool.message;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Java class for FindingTypeFilter complex type.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FindingTypeFilter", propOrder = { "name", "importance",
		"filtered" })
public class FindingTypeFilter {
	@XmlElement(required = true)
	protected String name;
	protected Importance importance;
	protected boolean filtered;

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
	 *            allowed object is {@link String }
	 * 
	 */
	public void setName(String value) {
		name = value;
	}

	/**
	 * Gets the value of the importance property.
	 * 
	 * @return possible object is {@link Importance }
	 * 
	 */
	public Importance getImportance() {
		return importance;
	}

	/**
	 * Sets the value of the importance property.
	 * 
	 * @param value
	 *            allowed object is {@link Importance }
	 * 
	 */
	public void setImportance(Importance value) {
		importance = value;
	}

	/**
	 * Gets the value of the filtered property.
	 * 
	 */
	public boolean isFiltered() {
		return filtered;
	}

	/**
	 * Sets the value of the filtered property.
	 * 
	 */
	public void setFiltered(boolean value) {
		filtered = value;
	}
}
