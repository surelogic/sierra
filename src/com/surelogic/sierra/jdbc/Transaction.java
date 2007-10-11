package com.surelogic.sierra.jdbc;

import java.sql.PreparedStatement;
import java.sql.Statement;

interface Transaction {

	PreparedStatement prepareStatement();

	Statement createStatement();
	
	void finished();
}
