/**
 * 
 */
package com.surelogic.sierra.jdbc.tool;

import java.util.StringTokenizer;

public class FindingTypeKey {
	private final String tool;
	private final String version;
	private final String mnemonic;

	FindingTypeKey(String tool, String version, String mnemonic) {
		this.tool = tool;
		this.version = version;
		this.mnemonic = mnemonic;
	}

	public String getTool() {
		return tool;
	}

	public String getVersion() {
		return version;
	}

	public String getMnemonic() {
		return mnemonic;
	}

	@Override
	public String toString() {
		return tool + ":" + version + ":" + mnemonic;
	}

	public static FindingTypeKey fromString(String key) {
		StringTokenizer tok = new StringTokenizer(key, ":");
		return new FindingTypeKey(tok.nextToken(), tok.nextToken(), tok
				.nextToken());
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
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final FindingTypeKey other = (FindingTypeKey) obj;
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
		if (version == null) {
			if (other.version != null)
				return false;
		} else if (!version.equals(other.version))
			return false;
		return true;
	}

}