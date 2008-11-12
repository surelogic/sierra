package com.surelogic.sierra.client.eclipse.actions;

import java.io.File;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;

import com.surelogic.common.FileUtility;
import com.surelogic.sierra.client.eclipse.jobs.ImportScanDocumentJob;

/**
 * TODO: This action got removed from the main menu it needs to be deleted once
 * it is moved to the File | Import... menu.
 */
public final class ImportScanAction implements IWorkbenchWindowActionDelegate {

	public void dispose() {
		// Nothing to do
	}

	public void init(IWorkbenchWindow window) {
		// Nothing to do
	}

	private FileDialog fd = null;

	public void run(IAction action) {
		if (fd == null) {
			fd = new FileDialog(PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow().getShell(), SWT.OPEN);
			fd.setText("Import Scan");
			fd.setFilterPath(FileUtility.getSierraDataDirectory()
					.getAbsolutePath());
			fd.setFilterExtensions(new String[] { "*.sierra", "*.sierra.gz",
					"*.*" });
			fd.setFilterNames(new String[] { "Scan Documents (*.sierra)",
					"Compressed Scan Documents (*.sierra.gz)",
					"All Files (*.*)" });
		}
		final String selectedFilename = fd.open();
		if (selectedFilename != null) {
			File runDocument = new File(selectedFilename);
			if (runDocument.canRead()) {
				final ImportScanDocumentJob job = new ImportScanDocumentJob(
						runDocument);
				job.setUser(true);
				job.schedule();
			}
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {
		// Nothing to do
	}
}
