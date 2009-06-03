package com.surelogic.sierra.client.eclipse;

import java.io.File;

import com.surelogic.common.FileUtility;
import com.surelogic.common.derby.DerbyConnection;
import com.surelogic.common.jdbc.DBConnection;
import com.surelogic.common.jdbc.SchemaData;
import com.surelogic.sierra.client.eclipse.preferences.PreferenceConstants;
import com.surelogic.sierra.schema.SierraSchemaData;

public final class Data extends DerbyConnection {

	private static final Data INSTANCE = new Data();
	private volatile String f_dataPath;

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
		if (f_dataPath == null) {
			final File dataDir = PreferenceConstants.getSierraDataDirectory();
			final File dbDir = new File(dataDir, FileUtility.DB_PATH_FRAGMENT);
			f_dataPath = dbDir.getAbsolutePath();
		}
		return f_dataPath;
	}

	@Override
	protected String getSchemaName() {
		return "SIERRAV1";
	}

	public SchemaData getSchemaLoader() {
		return new SierraSchemaData();
	}
}
