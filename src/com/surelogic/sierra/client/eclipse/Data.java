package com.surelogic.sierra.client.eclipse;

import java.io.File;

import com.surelogic.common.derby.DerbyConnection;
import com.surelogic.common.eclipse.EclipseFileUtility;
import com.surelogic.common.jdbc.DBConnection;
import com.surelogic.common.jdbc.SchemaData;
import com.surelogic.sierra.client.eclipse.preferences.PreferenceConstants;
import com.surelogic.sierra.schema.SierraSchemaData;

public final class Data extends DerbyConnection {

	private static final Data INSTANCE = new Data();
	private String pluginStatePath;
	
	public static DBConnection getInstance() {
		INSTANCE.loggedBootAndCheckSchema();
		return INSTANCE;
	}

	private Data() {
		// Singleton
	}

	@Override
	protected boolean deleteDatabaseOnStartup() {
		return PreferenceConstants.deleteDatabaseOnStartup();
	}

	@Override
	protected void setDeleteDatabaseOnStartup(final boolean bool) {
		PreferenceConstants.setDeleteDatabaseOnStartup(bool);
	}

	@Override
	protected synchronized String getDatabaseLocation() {
		if (pluginStatePath == null) {
			if (EclipseFileUtility.COLOCATE_DATABASE) {
				pluginStatePath = EclipseFileUtility.getSierraDataDirectory().getAbsolutePath();
			} else {
				pluginStatePath = Activator.getDefault().getStateLocation().toOSString();
			}
		}
		return pluginStatePath + File.separator
				+ DATABASE_PATH_FRAGMENT;
	}

	@Override
	protected String getSchemaName() {
		return "SIERRAV1";
	}

	public SchemaData getSchemaLoader() {
		return new SierraSchemaData();
	}
}
