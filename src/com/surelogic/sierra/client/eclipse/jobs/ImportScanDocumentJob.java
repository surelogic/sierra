package com.surelogic.sierra.client.eclipse.jobs;

import java.io.File;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import com.surelogic.adhoc.DatabaseJob;
import com.surelogic.common.eclipse.SLProgressMonitorWrapper;

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

		ScanDocumentUtility.loadScanDocument(f_scanDocument,
				slProgressMonitorWrapper, null);
		if (slProgressMonitorWrapper.isCanceled()) {
			return Status.CANCEL_STATUS;
		} else {
			return Status.OK_STATUS;
		}
	}

}
