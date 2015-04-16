package com.surelogic.sierra.tool.message;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * Java class for ArtifactType complex type.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ArtifactType")
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
		mnemonic = value;
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
		tool = value;
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
		version = value;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((mnemonic == null) ? 0 : mnemonic.hashCode());
		result = prime * result + ((tool == null) ? 0 : tool.hashCode());
		result = prime * result + ((version == null) ? 0 : version.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final ArtifactType other = (ArtifactType) obj;
		if (mnemonic == null) {
			if (other.mnemonic != null) {
				return false;
			}
		} else if (!mnemonic.equals(other.mnemonic)) {
			return false;
		}
		if (tool == null) {
			if (other.tool != null) {
				return false;
			}
		} else if (!tool.equals(other.tool)) {
			return false;
		}
		if (version == null) {
			if (other.version != null) {
				return false;
			}
		} else if (!version.equals(other.version)) {
			return false;
		}
		return true;
	}

}
