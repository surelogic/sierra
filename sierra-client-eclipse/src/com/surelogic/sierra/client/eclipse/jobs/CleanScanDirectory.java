package com.surelogic.sierra.client.eclipse.jobs;

import java.io.File;
import java.io.FileFilter;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import com.surelogic.common.FileUtility;
import com.surelogic.common.core.jobs.SLProgressMonitorWrapper;
import com.surelogic.common.jobs.SLProgressMonitor;
import com.surelogic.sierra.client.eclipse.Data;
import com.surelogic.sierra.client.eclipse.preferences.SierraPreferencesUtility;
import com.surelogic.sierra.jdbc.scan.ScanQueries;

public class CleanScanDirectory extends AbstractSierraDatabaseJob {

    public CleanScanDirectory() {
        super("Removing unfinished scans and cleaning up scan directory.");
    }

    @Override
    protected IStatus run(final IProgressMonitor monitor) {
        final SLProgressMonitor mon = new SLProgressMonitorWrapper(monitor,
                "Removing any scans that may have not finished prepping.");
        mon.begin();
        for (File f : SierraPreferencesUtility.getSierraDataDirectory()
                .listFiles(new FileFilter() {
                    @Override
                    public boolean accept(File pathname) {
                        String name = pathname.getName();
                        return name.endsWith(".log.txt")
                                || name.endsWith(".sierra.gz");
                    }
                })) {
            f.delete();
        }
        for (File f : SierraPreferencesUtility.getSierraScanDirectory()
                .listFiles()) {
            if (f.isDirectory()) {
                // This should be an orphaned scan
                FileUtility.recursiveDelete(f);
            }
        }
        Data.getInstance().withTransaction(
                ScanQueries.deleteUnfinishedScans(mon));
        mon.done();
        return Status.OK_STATUS;
    }
}
