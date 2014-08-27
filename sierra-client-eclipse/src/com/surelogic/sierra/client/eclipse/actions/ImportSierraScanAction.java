package com.surelogic.sierra.client.eclipse.actions;

import java.io.File;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;

import com.surelogic.common.FileUtility;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.ui.EclipseUIUtility;
import com.surelogic.sierra.client.eclipse.jobs.ImportScanDocumentJob;
import com.surelogic.sierra.client.eclipse.model.DatabaseHub;
import com.surelogic.sierra.client.eclipse.preferences.SierraPreferencesUtility;

public class ImportSierraScanAction implements IWorkbenchWindowActionDelegate {

    @Override
    public void run(IAction action) {
        Shell shell = EclipseUIUtility.getShell();
        FileDialog fd = new FileDialog(PlatformUI.getWorkbench()
                .getActiveWorkbenchWindow().getShell(), SWT.OPEN);
        fd.setText("Import Scan");
        fd.setFilterExtensions(new String[] { "*.sierra.gz", "*.sierra", "*.*" });
        fd.setFilterNames(new String[] { "Scan Documents (*.sierra)",
                "Compressed Scan Documents (*.sierra.gz)", "All Files (*.*)" });
        String fileName = fd.open();
        if (fileName != null) {
            File f = new File(fileName);
            if (f.exists() && !f.isDirectory()) {
                File to = new File(
                        SierraPreferencesUtility.getSierraScanDirectory(),
                        f.getName());
                if (!f.equals(to)) {
                    FileUtility.copy(f, to);
                }
                final Runnable runAfterImport = new Runnable() {
                    @Override
                    public void run() {
                        /* Notify that scan was completed */
                        DatabaseHub.getInstance().notifyScanLoaded();
                    }
                };
                ImportScanDocumentJob job = new ImportScanDocumentJob(f, null,
                        runAfterImport);
                job.schedule();
            } else {
                MessageDialog.openError(
                        shell,
                        I18N.msg("sierra.dialog.importScan.error.title"),
                        I18N.msg("sierra.dialog.importScan.error.title",
                                f.getAbsolutePath()));
            }
        }
    }

    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        // Nothing to do
    }

    @Override
    public void dispose() {
        // Nothing to do
    }

    @Override
    public void init(IWorkbenchWindow window) {
        // Nothing to do
    }
}
