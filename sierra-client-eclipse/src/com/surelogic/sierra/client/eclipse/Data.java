package com.surelogic.sierra.client.eclipse;

import java.io.File;
import java.io.PrintWriter;

import org.eclipse.jface.dialogs.MessageDialog;

import com.surelogic.common.FileUtility;
import com.surelogic.common.core.EclipseUtility;
import com.surelogic.common.derby.DerbyConnection;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.jdbc.DBConnection;
import com.surelogic.common.jdbc.SchemaData;
import com.surelogic.common.ui.EclipseUIUtility;
import com.surelogic.sierra.client.eclipse.preferences.SierraPreferencesUtility;
import com.surelogic.sierra.schema.SierraSchemaData;

public final class Data extends DerbyConnection {

    private static final Data INSTANCE = new Data();

    private static final String VERSION2FILE = "schema.txt";

    private volatile String f_dataPath;

    public static DBConnection getInstance() {
        return INSTANCE;
    }

    private Data() {
        // Singleton
    }

    @Override
    protected boolean deleteDatabaseOnStartup() {
        return EclipseUtility
                .getBooleanPreference(SierraPreferencesUtility.DELETE_DB_ON_STARTUP);
    }

    @Override
    protected void setDeleteDatabaseOnStartup(final boolean bool) {
        EclipseUtility.setBooleanPreference(
                SierraPreferencesUtility.DELETE_DB_ON_STARTUP, true);
    }

    @Override
    protected synchronized String getDatabaseLocation() {
        if (f_dataPath == null) {
            final File dataDir = SierraPreferencesUtility
                    .getSierraDataDirectory();
            final File dbDir = new File(dataDir, FileUtility.DB_PATH_FRAGMENT);
            f_dataPath = dbDir.getAbsolutePath();
        }
        return f_dataPath;
    }

    @Override
    public synchronized void shutdown() {
        super.shutdown();
        f_dataPath = null;
    }

    @Override
    protected String getSchemaName() {
        return "SIERRAV2";
    }

    @Override
    public synchronized void bootAndCheckSchema() throws Exception {
        if (!f_booted) {
            final File dbDir = new File(getDatabaseLocation());
            final File versionFile = new File(
                    SierraPreferencesUtility.getSierraDataDirectory(),
                    VERSION2FILE);
            /*
             * Check for an older version of the database
             */
            if (!versionFile.exists()) {
                if (dbDir.exists()) {
                    MessageDialog
                    .openWarning(
                            EclipseUIUtility.getShell(),
                            I18N.msg("sierra.eclipse.oldDatabaseMessage.title"),
                            I18N.msg(
                                            "sierra.eclipse.oldDatabaseMessage.content",
                                            dbDir));
                    FileUtility.recursiveDelete(dbDir);
                }
                PrintWriter writer = new PrintWriter(versionFile);
                try {
                    writer.println(getSchemaName());
                } finally {
                    writer.close();
                }
            }
        }
        super.bootAndCheckSchema();
    }

    @Override
    public SchemaData getSchemaLoader() {
        return new SierraSchemaData();
    }
}
