package com.surelogic.sierra.client.eclipse.jobs;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.zip.GZIPInputStream;

import javax.xml.ws.WebServiceException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import com.surelogic.common.SLProgressMonitor;
import com.surelogic.common.eclipse.SLProgressMonitorWrapper;
import com.surelogic.common.eclipse.job.DatabaseJob;
import com.surelogic.common.eclipse.logging.SLStatus;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.client.eclipse.actions.QualifierPromptFromJob;
import com.surelogic.sierra.client.eclipse.actions.TroubleshootConnection;
import com.surelogic.sierra.client.eclipse.actions.TroubleshootNoSuchServer;
import com.surelogic.sierra.client.eclipse.actions.TroubleshootWrongAuthentication;
import com.surelogic.sierra.client.eclipse.model.SierraServer;
import com.surelogic.sierra.tool.message.MessageWarehouse;
import com.surelogic.sierra.tool.message.QualifierRequest;
import com.surelogic.sierra.tool.message.Scan;
import com.surelogic.sierra.tool.message.SierraServiceClient;

public class ShareScanJob extends DatabaseJob {

	private final String f_projectName;
	private final SierraServer f_server;
	private final File f_scanFile;

	public ShareScanJob(String projectName, SierraServer server, File scanFile) {
		super("Sharing scan of project '" + projectName + "'");
		f_projectName = projectName;
		f_server = server;
		f_scanFile = scanFile;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {

		SLProgressMonitor slMonitor = new SLProgressMonitorWrapper(monitor);
		slMonitor.beginTask("Sharing scan of project " + f_projectName + " to "
				+ f_server.getLabel() + ".", 5);
		try {
			FileInputStream in = new FileInputStream(f_scanFile);
			Scan scan = MessageWarehouse.getInstance().fetchScan(
					new GZIPInputStream(in));
			in.close();
			if (monitor.isCanceled()) {
				return Status.CANCEL_STATUS;
			} else {

				Set<String> qualifiers = getQualifiersOnTheServer(slMonitor);
				if (qualifiers == null) {
					return Status.CANCEL_STATUS;
				}
				if (slMonitor.isCanceled()) {
					return null;
				} else {
					QualifierPromptFromJob prompt = new QualifierPromptFromJob(
							qualifiers, f_projectName, f_server.getLabel());
					prompt.open();
					if (prompt.isCanceled()) {
						slMonitor.setCanceled(true);
						return Status.CANCEL_STATUS;
					} else {
						qualifiers = prompt.getSelectedQualifiers();
					}
				}
				if (slMonitor.isCanceled()) {
					return Status.CANCEL_STATUS;
				}
				scan.getConfig().setQualifiers(
						new ArrayList<String>(qualifiers));
				return publishRun(scan, slMonitor);
			}
		} catch (Exception e) {
			final String msg = "Sharing scan of project '" + f_projectName
					+ "' to Sierra server '" + f_server + "' failed.";
			SLLogger.getLogger().log(Level.SEVERE, msg, e);
			return SLStatus.createErrorStatus(msg, e);
		}
	}

	private Set<String> getQualifiersOnTheServer(SLProgressMonitor slMonitor) {
		TroubleshootConnection troubleshoot;
		try {
			return new TreeSet<String>(new SierraServiceClient(f_server
					.getServer()).getSierraServicePort().getQualifiers(
					new QualifierRequest()).getQualifier());
		} catch (WebServiceException e) {
			if ("request requires HTTP authentication: Unauthorized".equals(e
					.getMessage())) {
				troubleshoot = new TroubleshootWrongAuthentication(f_server,
						f_projectName);
			} else {
				troubleshoot = new TroubleshootNoSuchServer(f_server,
						f_projectName);
			}
			// We had a recoverable error. Rollback, run the appropriate
			// troubleshoot, and try again.
			troubleshoot.fix();
			if (troubleshoot.retry()) {
				return getQualifiersOnTheServer(slMonitor);
			} else {
				SLLogger.getLogger().log(Level.WARNING,
						"Failed to get qualifiers from " + f_server, e);
				return null;
			}
		}

	}

	private IStatus publishRun(Scan scan, SLProgressMonitor slMonitor) {
		TroubleshootConnection troubleshoot;
		try {
			new SierraServiceClient(f_server.getServer())
					.getSierraServicePort().publishRun(scan);
			return Status.OK_STATUS;
		} catch (WebServiceException e) {
			if ("request requires HTTP authentication: Unauthorized".equals(e
					.getMessage())) {
				troubleshoot = new TroubleshootWrongAuthentication(f_server,
						f_projectName);
			} else {
				troubleshoot = new TroubleshootNoSuchServer(f_server,
						f_projectName);
			}
			// We had a recoverable error. Rollback, run the appropriate
			// troubleshoot, and try again.
			troubleshoot.fix();
			if (troubleshoot.retry()) {
				return publishRun(scan, slMonitor);
			} else {
				SLLogger.getLogger().log(
						Level.WARNING,
						"Failed to get publish run about " + f_projectName
								+ " to " + f_server, e);
				return Status.CANCEL_STATUS;
			}
		}
	}
}
