package com.surelogic.sierra.client.eclipse;

import java.io.File;

import org.eclipse.core.runtime.IPath;

import com.surelogic.common.derby.DerbyConnection;
import com.surelogic.common.jdbc.SchemaData;
import com.surelogic.sierra.client.eclipse.preferences.PreferenceConstants;
import com.surelogic.sierra.schema.SierraSchemaData;

public final class Data extends DerbyConnection {

	public static final String SCHEMA_PACKAGE = "com.surelogic.sierra.schema";

	private static final String SCHEMA_NAME = "SIERRA";

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
		return SCHEMA_NAME;
	}

	private static final Data data = new Data();

	private Data() {
		// Singleton
	}

	public static Data getInstance() {
		return data;
	}

	@Override
	protected SchemaData getSchemaLoader() {
		return new SierraSchemaData();
	}

}
