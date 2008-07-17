package com.surelogic.sierra.client.eclipse;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.core.runtime.IPath;

import com.surelogic.adhoc.AbstractAdHocGlue;
import com.surelogic.sierra.client.eclipse.preferences.PreferenceConstants;

public final class AdHocGlue extends AbstractAdHocGlue {
	private static final ExecutorService exec = 
        Executors.newSingleThreadExecutor();
	
	public Executor getExecutor() {
		return exec;
	}
	
	public Connection getConnection() throws SQLException {
		return Data.getInstance().getConnection();
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
