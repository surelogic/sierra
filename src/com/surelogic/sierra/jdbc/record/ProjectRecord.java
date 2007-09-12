package com.surelogic.sierra.jdbc.record;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import static com.surelogic.sierra.jdbc.JDBCUtils.*;

public final class ProjectRecord extends LongUpdatableRecord {
	private String name;
	private String serverUid;

	public ProjectRecord(UpdateRecordMapper mapper) {
		super(mapper);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getServerUid() {
		return serverUid;
	}

	public void setServerUid(String serverUid) {
		this.serverUid = serverUid;
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

	@Override
	protected int fill(PreparedStatement st, int idx) throws SQLException {
		idx = fillWithNk(st, idx);
		setNullableString(idx++, st, serverUid);
		return idx;
	}

	@Override
	protected int fillWithNk(PreparedStatement st, int idx) throws SQLException {
		st.setString(idx++, name);
		return idx;
	}

	@Override
	protected int readAttributes(ResultSet set, int idx) throws SQLException {
		serverUid = set.getString(idx++);
		return idx;
	}

	@Override
	protected int fillUpdatedFields(PreparedStatement st, int idx)
			throws SQLException {
		idx = fill(st, idx);
		return idx;
	}
}
