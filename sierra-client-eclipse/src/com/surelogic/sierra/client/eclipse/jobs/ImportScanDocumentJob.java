package com.surelogic.sierra.client.eclipse.jobs;

import java.io.File;
import java.sql.SQLException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;

import com.surelogic.common.core.jobs.SLProgressMonitorWrapper;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.jobs.SLProgressMonitor;
import com.surelogic.common.ui.EclipseUIUtility;
import com.surelogic.common.ui.jobs.SLUIJob;
import com.surelogic.sierra.client.eclipse.model.BuglinkData;

public final class ImportScanDocumentJob extends AbstractSierraDatabaseJob {

    private final File f_scanDocument;
    private final String projectName;
    private final Runnable runAfter;

    public ImportScanDocumentJob(final File scanDocument) {
        this(scanDocument, null, null);
    }

    public ImportScanDocumentJob(final File scanDocument, final String proj,
            final Runnable r) {
        super("Loading "
                + (proj != null ? "scan document for " + proj : scanDocument
                        .getName()));
        setPriority(Job.DECORATE);

        f_scanDocument = scanDocument;
        if (proj == null) {
            throw new IllegalArgumentException("Project may not be null.");
        } else {
            projectName = proj;
        }
        runAfter = r;
    }

    @Override
    protected IStatus run(final IProgressMonitor monitor) {
        final SLProgressMonitorWrapper slProgressMonitorWrapper = new SLProgressMonitorWrapper(
                monitor, getName());
        slProgressMonitorWrapper.begin(100);
        try {
            loadScanDocument(slProgressMonitorWrapper);
        } catch (final IllegalStateException e) {
            if (e.getCause() instanceof SQLException
                    && e.getMessage().contains("No current connection")) {
                // Try again and see if we can get through
                loadScanDocument(slProgressMonitorWrapper);
            }
        }
        BuglinkData.getInstance().refresh();
        if (slProgressMonitorWrapper.isCanceled()) {
            return Status.CANCEL_STATUS;
        } else {
            if (runAfter != null) {
                runAfter.run();
            }
            SLUIJob job = new SLUIJob() {

                @Override
                public IStatus runInUIThread(IProgressMonitor monitor) {
                    MessageDialog.openInformation(EclipseUIUtility.getShell(),
                            I18N.msg("sierra.dialog.importScan.success.title"),
                            I18N.msg("sierra.dialog.importScan.success.msg",
                                    projectName));
                    return Status.OK_STATUS;
                }
            };
            job.schedule();

            return Status.OK_STATUS;
        }
    }

    private void loadScanDocument(final SLProgressMonitor wrapper) {
        ScanDocumentUtility.loadScanDocument(f_scanDocument, wrapper,
                projectName);
    }
}
