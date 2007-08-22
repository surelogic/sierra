package com.surelogic.sierra.client.eclipse.jobs;

import java.io.File;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import com.surelogic.adhoc.DatabaseJob;

public final class LoadRunDocumentJob extends DatabaseJob {

	private final File f_runDocument;

	public LoadRunDocumentJob(File runDocument) {
		super("Loading " + runDocument.getName());
		f_runDocument = runDocument;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return Status.OK_STATUS;
	}

}
