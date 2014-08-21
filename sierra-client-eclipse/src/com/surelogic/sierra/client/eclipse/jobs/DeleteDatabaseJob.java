package com.surelogic.sierra.client.eclipse.jobs;

import java.util.logging.Level;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import com.surelogic.common.FileUtility;
import com.surelogic.common.core.jobs.SLProgressMonitorWrapper;
import com.surelogic.common.core.logging.SLEclipseStatusUtility;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.jobs.SLProgressMonitor;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.client.eclipse.Data;
import com.surelogic.sierra.client.eclipse.Tools;
import com.surelogic.sierra.client.eclipse.model.ConnectedServerManager;
import com.surelogic.sierra.client.eclipse.model.DatabaseHub;
import com.surelogic.sierra.client.eclipse.preferences.SierraPreferencesUtility;

public final class DeleteDatabaseJob extends AbstractSierraDatabaseJob {

    public DeleteDatabaseJob() {
        super("Deleting Sierra database.");
    }

    @Override
    protected IStatus run(final IProgressMonitor monitor) {
        final SLProgressMonitor slMonitor = new SLProgressMonitorWrapper(
                monitor, "Deleting the Sierra database from the file system.");
        slMonitor.begin();
        ConnectedServerManager.getInstance().clear();
        // Projects.getInstance().clear();
        FileUtility.recursiveDelete(SierraPreferencesUtility
                .getSierraScanDirectory());
        Data.getInstance().destroy();
        try {
            Data.getInstance().bootAndCheckSchema();
            Tools.checkForNewArtifactTypes();
        } catch (Exception e) {
            final int code = 171;
            final String msg = I18N.err(code);
            SLLogger.getLogger().log(Level.SEVERE, msg, e);
            return SLEclipseStatusUtility.createErrorStatus(code, msg, e);
        }
        DatabaseHub.getInstance().notifyDatabaseDeleted();
        SLLogger.getLogger().info("The client database has been deleted");
        return Status.OK_STATUS;
    }

}
