package com.surelogic.sierra.client.eclipse.jobs;

import java.io.File;
import java.sql.SQLException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import com.surelogic.common.eclipse.SLProgressMonitorWrapper;
import com.surelogic.common.eclipse.jobs.DatabaseJob;
import com.surelogic.common.jobs.SLProgressMonitor;

public final class ImportScanDocumentJob extends DatabaseJob {

	private final File f_scanDocument;
	private final String projectName;
  private final Runnable runAfter;
	
	public ImportScanDocumentJob(File scanDocument) {
	  this(scanDocument, null, null);
	}

	public ImportScanDocumentJob(File scanDocument, String proj, Runnable r) {
		super("Loading " + (proj != null ? "scan document for "+proj : scanDocument.getName()));
		f_scanDocument = scanDocument;
		if (proj == null)  {
	    final String fileName = f_scanDocument.getName();
	    projectName = fileName.substring(0, fileName.indexOf(".sierra"));
		} else {
		  projectName = proj;
		}
		runAfter = r;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		SLProgressMonitorWrapper slProgressMonitorWrapper = new SLProgressMonitorWrapper(
				monitor);
	  try {
      loadScanDocument(slProgressMonitorWrapper);
    } catch (IllegalStateException e) {
      if (e.getCause() instanceof SQLException && 
          e.getMessage().contains("No current connection")) {
        // Try again and see if we can get through
        loadScanDocument(slProgressMonitorWrapper);
      }
    }
    
		if (slProgressMonitorWrapper.isCanceled()) {
			return Status.CANCEL_STATUS;
		} else {
		  if (runAfter != null) {
		    runAfter.run();
		  }
			return Status.OK_STATUS;
		}
	}

	private void loadScanDocument(final SLProgressMonitor wrapper) {
	   ScanDocumentUtility.loadScanDocument(f_scanDocument, wrapper, projectName);
	}
}
