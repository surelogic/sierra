package com.surelogic.sierra.client.eclipse.jobs;

import java.io.File;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import com.surelogic.common.eclipse.SLProgressMonitorWrapper;
import com.surelogic.common.eclipse.job.DatabaseJob;

public final class ImportScanDocumentJob extends DatabaseJob {

	private final File f_scanDocument;

	public ImportScanDocumentJob(File scanDocument) {
		super("Loading " + scanDocument.getName());
		f_scanDocument = scanDocument;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		SLProgressMonitorWrapper slProgressMonitorWrapper = new SLProgressMonitorWrapper(
				monitor);
		final String fileName = f_scanDocument.getName();
		final String projectName = fileName.substring(0, fileName
				.indexOf(".sierra.gz"));
		ScanDocumentUtility.loadScanDocument(f_scanDocument,
				slProgressMonitorWrapper, projectName);
		if (slProgressMonitorWrapper.isCanceled()) {
			return Status.CANCEL_STATUS;
		} else {
			return Status.OK_STATUS;
		}
	}

}
