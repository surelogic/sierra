package com.surelogic.sierra.jdbc.user;

import java.sql.Connection;
import java.sql.SQLException;

import com.surelogic.sierra.jdbc.record.BaseMapper;
import com.surelogic.sierra.jdbc.record.GroupRecord;
import com.surelogic.sierra.jdbc.record.UserGroupRelationRecord;
import com.surelogic.sierra.jdbc.record.RecordMapper;
import com.surelogic.sierra.jdbc.record.UpdateBaseMapper;
import com.surelogic.sierra.jdbc.record.UpdateRecordMapper;
import com.surelogic.sierra.jdbc.record.UserRecord;

public class UserRecordFactory {

	private final UpdateRecordMapper userMapper;
	private final UpdateRecordMapper groupMapper;
	private final RecordMapper groupUserMapper;

	private UserRecordFactory(Connection conn) throws SQLException {
		userMapper = new UpdateBaseMapper(conn,
				"INSERT INTO SIERRA_USER (USER_NAME,SALT,HASH) VALUES (?,?,?)",
				"SELECT ID, SALT, HASH FROM SIERRA_USER WHERE USER_NAME = ?",
				"DELETE FROM SIERRA_USER WHERE ID = ?",
				"UPDATE SIERRA_USER SET SALT = ?, HASH = ? WHERE ID = ?");
		groupMapper = new UpdateBaseMapper(conn,
				"INSERT INTO SIERRA_GROUP (NAME,INFO) VALUES (?,?)",
				"SELECT ID, INFO FROM SIERRA_GROUP WHERE NAME = ?",
				"DELETE FROM SIERRA_GROUP WHERE ID = ?",
				"UPDATE SIERRA_GROUP SET NAME = ?, INFO = ? WHERE ID = ?");
		groupUserMapper = new BaseMapper(
				conn,
				"INSERT INTO GROUP_USER_RELTN (USER_ID, GROUP_ID) VALUES (?,?)",
				"SELECT USER_ID,GROUP_ID FROM GROUP_USER_RELTN WHERE USER_ID = ? AND GROUP_ID = ?",
				null, false);
	}

	public static UserRecordFactory getInstance(Connection conn)
			throws SQLException {
		return new UserRecordFactory(conn);
	}

	UserRecord newUser() {
		return new UserRecord(userMapper);
	}

	GroupRecord newGroup() {
		return new GroupRecord(groupMapper);
	}

	UserGroupRelationRecord newGroupUser() {
		return new UserGroupRelationRecord(groupUserMapper);
	}
}
