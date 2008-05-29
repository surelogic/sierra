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
import com.surelogic.common.jdbc.ConnectionQuery;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.jsure.xml.JSureXMLReader;
import com.surelogic.sierra.client.eclipse.Data;
import com.surelogic.sierra.client.eclipse.jobs.ScanDocumentUtility;
import com.surelogic.sierra.client.eclipse.model.*;
import com.surelogic.sierra.jdbc.finding.ClientFindingManager;
import com.surelogic.sierra.jdbc.scan.ScanManager;
import com.surelogic.sierra.jdbc.scan.ScanPersistenceException;
import com.surelogic.sierra.jdbc.settings.SettingQueries;
import com.surelogic.sierra.jdbc.tool.FindingFilter;
import com.surelogic.sierra.tool.message.ArtifactGenerator;
import com.surelogic.sierra.tool.message.MessageWarehouse;
import com.surelogic.sierra.tool.message.ScanGenerator;
import com.surelogic.sierra.tool.message.ArtifactGenerator.ArtifactBuilder;

public class ImportJSureDocumentJob extends DatabaseJob {
	private static final Logger log = 
		SLLogger.getLoggerFor(ImportJSureDocumentJob.class);
	
	final ConfigCompilationUnit config;

	public ImportJSureDocumentJob(ConfigCompilationUnit ccu) {
		super("Loading JSure document for "
				+ ccu.getConfig().getProject());
		config = ccu;
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
					sMan.getPartialScanGenerator(config.getConfig().getProject(), 
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
		final File location = config.getConfig().getScanDocument();
		ScanDocumentUtility.loadPartialScanDocument(location, wrapper, 
				      config.getConfig().getProject(), 
				      config.getPackageCompilationUnitMap(), parser);
		/*
		// Delete partial scan when done
		location.delete();
		*/
	}
}
