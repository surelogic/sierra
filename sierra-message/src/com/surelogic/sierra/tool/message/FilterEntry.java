package com.surelogic.sierra.tool.message;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * Java class for FilterEntry complex type.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FilterEntry", propOrder =  {
    "type", "filtered"}
)
public class FilterEntry {
    @XmlElement(required = true)
    protected String type;
    protected boolean filtered;

    /**
     * Gets the value of the type property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setType(String value) {
        this.type = value;
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
        this.filtered = value;
    }
}
