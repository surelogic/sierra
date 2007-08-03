/**
 * 
 */
package com.surelogic.sierra.jdbc.tool;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.surelogic.sierra.jdbc.Record;

public class FindingTypeKey {
	private Long id;
	private final String tool;
	private final String mnemonic;

	public FindingTypeKey(String tool, String mnemonic) {
		this.tool = tool;
		this.mnemonic = mnemonic;
	}

	public int fill(PreparedStatement st, int idx) throws SQLException {
		st.setString(idx++, tool);
		st.setString(idx++, mnemonic);
		return idx;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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
		return true;
	}

}