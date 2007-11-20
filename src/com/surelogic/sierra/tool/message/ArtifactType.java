package com.surelogic.sierra.tool.message;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for ArtifactType complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name=&quot;ArtifactType&quot;&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base=&quot;{http://www.w3.org/2001/XMLSchema}anyType&quot;&gt;
 *       &lt;attribute name=&quot;mnemonic&quot; use=&quot;required&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}string&quot; /&gt;
 *       &lt;attribute name=&quot;tool&quot; use=&quot;required&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}string&quot; /&gt;
 *       &lt;attribute name=&quot;version&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}string&quot; /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ArtifactType", propOrder = { "any" })
public class ArtifactType {

	@XmlAttribute(required = true)
	protected String mnemonic;
	@XmlAttribute(required = true)
	protected String tool;
	@XmlAttribute
	protected String version;

	/**
	 * Gets the value of the mnemonic property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getMnemonic() {
		return mnemonic;
	}

	/**
	 * Sets the value of the mnemonic property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setMnemonic(String value) {
		this.mnemonic = value;
	}

	/**
	 * Gets the value of the tool property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getTool() {
		return tool;
	}

	/**
	 * Sets the value of the tool property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setTool(String value) {
		this.tool = value;
	}

	/**
	 * Gets the value of the version property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * Sets the value of the version property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setVersion(String value) {
		this.version = value;
	}

}
