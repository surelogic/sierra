package com.surelogic.sierra.client.eclipse;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;

import org.eclipse.core.runtime.IPath;

import com.surelogic.adhoc.AbstractAdHocGlue;
import com.surelogic.sierra.client.eclipse.preferences.PreferenceConstants;

public final class AdHocGlue extends AbstractAdHocGlue {

	public Connection getConnection() throws SQLException {
		return Data.getConnection();
	}

	public int getMaxRowsPerQuery() {
		return PreferenceConstants.getFindingsListLimit();
	}

	public File getQuerySaveFile() {
		IPath pluginState = Activator.getDefault().getStateLocation();
		return new File(pluginState.toOSString()
				+ System.getProperty("file.separator") + "queries.xml");
	}
}
