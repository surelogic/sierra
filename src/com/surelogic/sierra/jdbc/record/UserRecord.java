package com.surelogic.sierra.jdbc.record;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.surelogic.sierra.jdbc.user.Password;

public class UserRecord extends LongUpdatableRecord {

	private String userName;
	private Password password;

	public UserRecord(UpdateRecordMapper mapper) {
		super(mapper);
	}

	@Override
	public int fillUpdatedFields(PreparedStatement st, int idx)
			throws SQLException {
		st.setInt(idx++, password.getSalt());
		st.setBytes(idx++, password.getHash());
		return idx;
	}

	@Override
	public int fill(PreparedStatement st, int idx) throws SQLException {
		idx = fillWithNk(st, idx);
		st.setInt(idx++, password.getSalt());
		st.setBytes(idx++, password.getHash());
		return idx;
	}

	@Override
	public int fillWithNk(PreparedStatement st, int idx) throws SQLException {
		st.setString(idx++, userName);
		return idx;
	}

	@Override
	public int readAttributes(ResultSet set, int idx) throws SQLException {
		password = Password.restorePassword(set.getInt(idx++), set
				.getBytes(idx++));
		return idx;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public Password getPassword() {
		return password;
	}

	public void setPassword(Password password) {
		this.password = password;
	}

}
