package com.surelogic.sierra.tool.message;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlType(propOrder = { "name", "version" })
@XmlAccessorType(XmlAccessType.FIELD)
public class ExtensionName {
	protected String name;
	protected String version;

	public ExtensionName() {
		// Do nothing
	}

	public ExtensionName(final String name, final String version) {
		this.name = name;
		this.version = version;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(final String version) {
		this.version = version;
	}

}
