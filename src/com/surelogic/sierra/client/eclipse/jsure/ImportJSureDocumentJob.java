package com.surelogic.sierra.client.eclipse.jsure;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import com.surelogic.common.eclipse.jobs.SLProgressMonitorWrapper;
import com.surelogic.common.jobs.SLProgressMonitor;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.jsure.xml.JSureXMLReader;
import com.surelogic.sierra.client.eclipse.jobs.AbstractSierraDatabaseJob;
import com.surelogic.sierra.client.eclipse.jobs.ScanDocumentUtility;
import com.surelogic.sierra.client.eclipse.model.DatabaseHub;
import com.surelogic.sierra.jdbc.finding.ClientFindingManager;
import com.surelogic.sierra.jdbc.scan.ScanManager;
import com.surelogic.sierra.jdbc.tool.FindingFilter;
import com.surelogic.sierra.tool.message.ArtifactGenerator;
import com.surelogic.sierra.tool.message.ScanGenerator;

public class ImportJSureDocumentJob extends AbstractSierraDatabaseJob {
	private static final Logger log = SLLogger
			.getLoggerFor(ImportJSureDocumentJob.class);

	final String project;
	final File location;

	public ImportJSureDocumentJob(final String p, final File loc) {
		super("Importing JSure document for " + p + " to Sierra");
		project = p;
		location = loc;
	}

	@Override
	protected IStatus run(final IProgressMonitor monitor) {
		monitor.beginTask("Importing " + location + "...",
				IProgressMonitor.UNKNOWN);
		final SLProgressMonitor wrapper = new SLProgressMonitorWrapper(monitor,
				getName());
		try {
			loadScanDocument(wrapper);
		} catch (final IllegalStateException e) {
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

	private static final List<String> tools = Collections
			.singletonList("JSure");

	private void loadScanDocument(final SLProgressMonitor wrapper) {
		final ScanDocumentUtility.Parser parser = new ScanDocumentUtility.Parser() {
			public String parse(final File scanDocument, final Connection conn,
					final ScanManager sMan, final FindingFilter filter,
					final Set<Long> findingIds, final SLProgressMonitor mon) {
				final ScanGenerator generator = sMan.getPartialScanGenerator(
						project, filter, tools, findingIds);
				/*
				 * builder.javaVendor(config.getJavaVendor());
				 * builder.javaVersion(config.getJavaVersion());
				 * builder.project(config.getProject());
				 * builder.timeseries(config.getTimeseries());
				 */
				final JSureDocumentListener l = new JSureDocumentListener(
						generator, mon);
				try {
					JSureXMLReader.readSnapshot(scanDocument, l);
				} catch (final Exception e) {
					final ArtifactGenerator aGenerator = l
							.getArtifactGenerator();
					if (aGenerator != null) {
						aGenerator.rollback();
					}
					log
							.log(Level.SEVERE,
									"Exception while reading snapshot", e);
				}
				return generator.finished();
			}

			public void updateOverview(final ClientFindingManager fm,
					final String uid, final FindingFilter filter,
					final Set<Long> findingIds, final SLProgressMonitor monitor) {
				fm.updateScanFindings(project, uid, tools, filter, findingIds,
						monitor);
			}
		};
		ScanDocumentUtility.loadPartialScanDocument(location, wrapper, project,
				parser);
		/*
		 * // Delete partial scan when done location.delete();
		 */
	}
}
