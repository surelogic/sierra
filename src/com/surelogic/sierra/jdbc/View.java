package com.surelogic.sierra.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface View {

	int read(ResultSet set, int idx) throws SQLException;
}
