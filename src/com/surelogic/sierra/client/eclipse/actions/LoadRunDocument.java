package com.surelogic.sierra.client.eclipse.actions;

import java.io.File;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;

import com.surelogic.sierra.client.eclipse.jobs.LoadRunDocumentJob;

public final class LoadRunDocument implements IWorkbenchWindowActionDelegate {

	public void dispose() {
		System.out.println("dispose");

	}

	public void init(IWorkbenchWindow window) {
		System.out.println("init");

	}

	private FileDialog fd = null;

	public void run(IAction action) {
		if (fd == null) {
			fd = new FileDialog(PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow().getShell(), SWT.OPEN);
			fd.setText("Load Analysis Run Document");
			fd.setFilterExtensions(new String[] { "*.xml", "*.*" });
			fd.setFilterNames(new String[] { "XML Files (*.xml)",
					"All Files (*.*)" });
		}
		final String selectedFilename = fd.open();
		if (selectedFilename != null) {
			File runDocument = new File(selectedFilename);
			if (runDocument.canRead()) {
				LoadRunDocumentJob job = new LoadRunDocumentJob(runDocument);
				job.setUser(true);
				job.schedule();
			}
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {
		System.out.println("selectionChanged");

	}

}
