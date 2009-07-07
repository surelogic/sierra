package com.surelogic.sierra.tool.message;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlType(propOrder = { "tool", "version", "mnemonic", "display", "findingType" })
@XmlAccessorType(XmlAccessType.FIELD)
public class ExtensionArtifactType {
	protected String tool;
	protected String version;
	protected String mnemonic;
	protected String display;
	protected String findingType;

	public String getTool() {
		return tool;
	}

	public void setTool(final String tool) {
		this.tool = tool;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(final String version) {
		this.version = version;
	}

	public String getMnemonic() {
		return mnemonic;
	}

	public void setMnemonic(final String mnemonic) {
		this.mnemonic = mnemonic;
	}

	public String getDisplay() {
		return display;
	}

	public void setDisplay(final String display) {
		this.display = display;
	}

	public String getFindingType() {
		return findingType;
	}

	public void setFindingType(final String findingType) {
		this.findingType = findingType;
	}

}
