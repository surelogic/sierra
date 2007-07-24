package com.surelogic.sierra.tool.message;

import javax.xml.bind.annotation.XmlType;

@XmlType
public class FindingType {
	private String tool;
	private String mnemonic;

	public FindingType() {
		// Nothing to do
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((mnemonic == null) ? 0 : mnemonic.hashCode());
		result = prime * result + ((tool == null) ? 0 : tool.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final FindingType other = (FindingType) obj;
		if (mnemonic == null) {
			if (other.mnemonic != null)
				return false;
		} else if (!mnemonic.equals(other.mnemonic))
			return false;
		if (tool == null) {
			if (other.tool != null)
				return false;
		} else if (!tool.equals(other.tool))
			return false;
		return true;
	}

}
