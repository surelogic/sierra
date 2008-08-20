package com.surelogic.sierra.client.eclipse;

import java.io.File;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;

import org.eclipse.core.runtime.IPath;

import com.surelogic.common.adhoc.IAdHocDataSource;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.client.eclipse.preferences.PreferenceConstants;

public final class AdHocGlue implements IAdHocDataSource {

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

	public void badQuerySaveFileNotification(Exception e) {
		SLLogger.getLogger().log(Level.SEVERE,
				I18N.err(4, getQuerySaveFile().getAbsolutePath()), e);
	}

	public URL getDefaultQueryUrl() {
		return null;
	}
}
