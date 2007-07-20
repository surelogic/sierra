package com.surelogic.sierra.message;

import javax.xml.bind.annotation.XmlType;

@XmlType
public class FindingType {
	private String tool;
	private String mnemonic;

	public FindingType() {
	}

	public FindingType(String tool, String mnemonic) {
		this.tool = tool;
		this.mnemonic = mnemonic;
	}

	public String getTool() {
		return tool;
	}

	public String getMnemonic() {
		return mnemonic;
	}

	public void setTool(String tool) {
		this.tool = tool;
	}

	public void setMnemonic(String mnemonic) {
		this.mnemonic = mnemonic;
	}

}
