package com.surelogic.sierra.client.eclipse.jsure;

import java.io.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.*;

import com.surelogic.common.SLProgressMonitor;
import com.surelogic.common.eclipse.SLProgressMonitorWrapper;
import com.surelogic.common.eclipse.jobs.DatabaseJob;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.jsure.xml.JSureXMLReader;
import com.surelogic.sierra.client.eclipse.jobs.ScanDocumentUtility;
import com.surelogic.sierra.client.eclipse.model.*;
import com.surelogic.sierra.jdbc.scan.ScanManager;
import com.surelogic.sierra.jdbc.tool.FindingFilter;
import com.surelogic.sierra.tool.message.*;

public class ImportJSureDocumentJob extends DatabaseJob {
	private static final Logger log = 
		SLLogger.getLoggerFor(ImportJSureDocumentJob.class);
	
	final String project;
	final File location;

	public ImportJSureDocumentJob(String p, File loc) {
		super("Loading JSure document for "+ p + " from "+loc);
		project = p;
		location = loc;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		final SLProgressMonitor wrapper = new SLProgressMonitorWrapper(
				monitor);
		try {
			loadScanDocument(wrapper);
		} catch (IllegalStateException e) {
			if (e.getCause() instanceof SQLException
					&& e.getMessage().contains("No current connection")) {
				// Try again and see if we can get through
				loadScanDocument(wrapper);
			}
		}
		/* Notify that scan was completed */
		DatabaseHub.getInstance().notifyScanLoaded();

		if (wrapper.isCanceled()) {
			return Status.CANCEL_STATUS;
		} else {
			return Status.OK_STATUS;
		}
	}

	private void loadScanDocument(final SLProgressMonitor wrapper) {
		final ScanDocumentUtility.Parser parser = new ScanDocumentUtility.Parser() {
			public String parse(File scanDocument, ScanManager sMan, FindingFilter filter,
				                Set<Long> findingIds, SLProgressMonitor mon) {
				final ScanGenerator generator = 
					sMan.getPartialScanGenerator(project, 
							                     filter, Collections.singletonList("JSure"), 
							                     findingIds);				
				/*
				 builder.javaVendor(config.getJavaVendor());
		         builder.javaVersion(config.getJavaVersion());
		         builder.project(config.getProject());
		         builder.timeseries(config.getTimeseries());
				 */
				final JSureDocumentListener l = new JSureDocumentListener(generator, mon);
				try {
					JSureXMLReader.readSnapshot(scanDocument, l); 
				} catch(Exception e) {
					ArtifactGenerator aGenerator = l.getArtifactGenerator();
					if (aGenerator != null) {
						aGenerator.rollback();
					}
					log.log(Level.SEVERE, "Exception while reading snapshot", e);
				}
				return generator.finished();
			}
		};
		ScanDocumentUtility.loadPartialScanDocument(location, wrapper, 
				      project, null, parser); // FIX null
		/*
		// Delete partial scan when done
		location.delete();
		*/
	}
}
