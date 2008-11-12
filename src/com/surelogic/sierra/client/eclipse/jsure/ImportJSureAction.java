package com.surelogic.sierra.client.eclipse.jsure;

import java.io.File;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.*;

import com.surelogic.common.FileUtility;

public final class ImportJSureAction implements IWorkbenchWindowActionDelegate {
	private static final boolean debug = true;

	public void dispose() {
		// Nothing to do
	}

	public void init(IWorkbenchWindow window) {
		// Nothing to do
	}

	private FileDialog fd = null;

	public void run(IAction action) {
		if (fd == null && !debug) {
			fd = new FileDialog(PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow().getShell(), SWT.OPEN);
			fd.setText("Import Scan");
			fd.setFilterPath(FileUtility.getSierraDataDirectory()
					.getAbsolutePath());
			fd.setFilterExtensions(new String[] { "*.sea.xml", "*.sea.xml.gz",
					"*.*" });
			fd.setFilterNames(new String[] { "Scan Documents (*.sea.xml)",
					"Compressed Scan Documents (*.sea.xml.gz)",
					"All Files (*.*)" });
		}
		final String selectedFilename;
		if (debug) {
			File userDir = new File(System.getProperty("user.home"));
			File desktop = new File(userDir, "Desktop");
			String first = null;
			for (String file : desktop.list()) {
				if (file.endsWith(".sea.xml")) {
					first = new File(desktop, file).getAbsolutePath();
					break;
				}
			}
			selectedFilename = first;
		} else {
			selectedFilename = fd.open();
		}
		if (selectedFilename != null) {
			File runDocument = new File(selectedFilename);
			asyncImportJSureDocument(runDocument);
		}
	}

	public static void asyncImportJSureDocument(File runDocument) {
		if (runDocument.canRead()) {
			final String name = runDocument.getName();
			final int last = name.length() - ".sea.xml".length();
			final String proj = name.substring(0, last);
			final ImportJSureDocumentJob job = new ImportJSureDocumentJob(proj,
					runDocument);
			job.setUser(true);
			job.schedule();
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {
		// Nothing to do
	}
}
