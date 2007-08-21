package com.surelogic.sierra.jdbc.run;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.surelogic.sierra.jdbc.LongRecord;

public class ProjectRecord extends LongRecord {
	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		final ProjectRecord other = (ProjectRecord) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	public int fill(PreparedStatement st, int idx) throws SQLException {
		st.setString(idx++, name);
		return idx;
	}
	
	public static String getFindSql() {
		return null;
	}
}
