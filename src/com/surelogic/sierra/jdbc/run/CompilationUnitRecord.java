/**
 * 
 */
package com.surelogic.sierra.jdbc.run;

import static com.surelogic.sierra.jdbc.JDBCUtils.setNullableString;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.surelogic.sierra.jdbc.LongRecord;

class CompilationUnitRecord extends LongRecord {
	private String path;
	private String className;
	private String packageName;

	CompilationUnitRecord() {
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	// PATH,CLASS_NAME,PACKAGE_NAME
	public int fill(PreparedStatement st, int idx) throws SQLException {
		setNullableString(idx++, st, path);
		assert className != null;
		assert packageName != null;
		st.setString(idx++, className);
		st.setString(idx++, packageName);
		return idx;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((className == null) ? 0 : className.hashCode());
		result = prime * result
				+ ((packageName == null) ? 0 : packageName.hashCode());
		result = prime * result + ((path == null) ? 0 : path.hashCode());
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
		if (className == null) {
			if (other.className != null)
				return false;
		} else if (!className.equals(other.className))
			return false;
		if (packageName == null) {
			if (other.packageName != null)
				return false;
		} else if (!packageName.equals(other.packageName))
			return false;
		if (path == null) {
			if (other.path != null)
				return false;
		} else if (!path.equals(other.path))
			return false;
		return true;
	}

}