package com.surelogic.sierra.client.eclipse;

import java.io.File;

import org.eclipse.core.runtime.IPath;

import com.surelogic.common.derby.DerbyConnection;
import com.surelogic.common.jdbc.DBConnection;
import com.surelogic.common.jdbc.SchemaData;
import com.surelogic.sierra.client.eclipse.preferences.PreferenceConstants;
import com.surelogic.sierra.schema.SierraSchemaData;

public final class Data extends DerbyConnection {

	private static final Data INSTANCE = new Data();

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
	protected String getDatabaseLocation() {
		final IPath pluginState = Activator.getDefault().getStateLocation();
		return pluginState.toOSString() + File.separator
				+ DATABASE_PATH_FRAGMENT;
	}

	@Override
	protected String getSchemaName() {
		return "SIERRA";
	}

	public SchemaData getSchemaLoader() {
		return new SierraSchemaData();
	}
}
