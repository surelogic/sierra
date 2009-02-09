package com.surelogic.sierra.schema;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

import com.surelogic.common.jdbc.SchemaAction;
import com.surelogic.sierra.jdbc.user.Password;
import com.surelogic.sierra.jdbc.user.SierraGroup;

public class Server_0000 implements SchemaAction {

	public void run(final Connection c) throws SQLException {
		final String serverUuid = UUID.randomUUID().toString();
		c.createStatement().execute(
				"INSERT INTO SERVER (UUID) VALUES('" + serverUuid + "')");
		final PreparedStatement nameSt = c
				.prepareStatement("INSERT INTO SERVER_IDENTITY (UUID,NAME,REVISION) VALUES (?,?,?)");
		try {
			nameSt.setString(1, serverUuid);
			nameSt.setString(2, "* no name *");
			nameSt.setLong(3, 0L);
			nameSt.execute();
		} finally {
			nameSt.close();
		}
		final PreparedStatement nameSettingSt = c
				.prepareStatement("INSERT INTO SITE_SETTINGS (SETTING_NAME, SETTING_VALUE) VALUES ('Name', ?)");
		try {
			nameSettingSt.setString(1, "* no name *");
			nameSettingSt.execute();
		} finally {
			nameSettingSt.close();
		}

		final PreparedStatement groupSt = c
				.prepareStatement("INSERT INTO SIERRA_GROUP (NAME,INFO) VALUES (?,?)");
		try {
			for (final SierraGroup g : SierraGroup.values()) {
				groupSt.setString(1, g.getName());
				groupSt.setString(2, g.getDescription());
				groupSt.execute();
			}
		} finally {
			groupSt.close();
		}
		final PreparedStatement userSt = c
				.prepareStatement("INSERT INTO SIERRA_USER (USER_NAME,SALT,HASH) VALUES (?,?,?)");
		try {
			final Password password = Password.newPassword("adminadmin");
			userSt.setString(1, "admin");
			userSt.setInt(2, password.getSalt());
			userSt.setBytes(3, password.getHash());
			userSt.execute();
		} finally {
			userSt.close();
		}
		final Statement userGroupSt = c.createStatement();
		try {
			userGroupSt
					.execute("INSERT INTO GROUP_USER_RELTN (USER_ID, GROUP_ID) VALUES ((SELECT ID FROM SIERRA_USER WHERE USER_NAME = 'admin'),(SELECT ID FROM SIERRA_GROUP WHERE NAME = 'Administrators'))");
		} finally {
			userGroupSt.close();
		}
		final Statement st = c.createStatement();
		try {
			st.execute("INSERT INTO QUALIFIER(NAME) VALUES('__ALL_SCANS__')");
		} finally {
			st.close();
		}
		SchemaUtil.setupServerScanFilter(c, serverUuid);
	}
}
