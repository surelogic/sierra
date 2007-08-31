package com.surelogic.sierra.client.eclipse.jobs;

import java.io.File;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import com.surelogic.adhoc.DatabaseJob;
import com.surelogic.common.eclipse.SLProgressMonitorWrapper;

public final class LoadRunDocumentJob extends DatabaseJob {

	private final File f_runDocument;

	// private static final Logger log = SierraLogger.getLogger("Sierra");

	public LoadRunDocumentJob(File runDocument) {
		super("Loading " + runDocument.getName());
		f_runDocument = runDocument;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		SLProgressMonitorWrapper slProgressMonitorWrapper = new SLProgressMonitorWrapper(
				monitor);

		ScanDocumentUtility.loadRunDocument(f_runDocument,
				slProgressMonitorWrapper);
		if (slProgressMonitorWrapper.isCanceled()) {
			return Status.CANCEL_STATUS;
		} else {
			return Status.OK_STATUS;
		}
	}

}
