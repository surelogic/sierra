/**
 * 
 */
package com.surelogic.sierra.jdbc.record;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class CompilationUnitRecord extends LongRecord {
	private String packageName;
	private String compilation;

	public CompilationUnitRecord(RecordMapper mapper) {
		super(mapper);
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public String getCompilation() {
		return compilation;
	}

	public void setCompilation(String compilation) {
		this.compilation = compilation;
	}

	@Override
	protected int fill(PreparedStatement st, int idx) throws SQLException {
		st.setString(idx++, packageName);
		st.setString(idx++, compilation);
		return idx;
	}

	@Override
	protected int fillWithNk(PreparedStatement st, int idx) throws SQLException {
		return fill(st, idx);
	}

	@Override
	protected int readAttributes(ResultSet set, int idx) throws SQLException {
		return idx;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((compilation == null) ? 0 : compilation.hashCode());
		result = prime * result
				+ ((packageName == null) ? 0 : packageName.hashCode());
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
		final CompilationUnitRecord other = (CompilationUnitRecord) obj;
		if (compilation == null) {
			if (other.compilation != null)
				return false;
		} else if (!compilation.equals(other.compilation))
			return false;
		if (packageName == null) {
			if (other.packageName != null)
				return false;
		} else if (!packageName.equals(other.packageName))
			return false;
		return true;
	}

}