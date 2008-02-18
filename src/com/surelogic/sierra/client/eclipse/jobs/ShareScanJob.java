package com.surelogic.sierra.client.eclipse.jobs;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.zip.GZIPInputStream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import com.surelogic.common.SLProgressMonitor;
import com.surelogic.common.eclipse.SLProgressMonitorWrapper;
import com.surelogic.common.eclipse.jobs.DatabaseJob;
import com.surelogic.common.eclipse.logging.SLStatus;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.client.eclipse.actions.QualifierPromptFromJob;
import com.surelogic.sierra.client.eclipse.actions.TroubleshootConnection;
import com.surelogic.sierra.client.eclipse.actions.TroubleshootNoSuchServer;
import com.surelogic.sierra.client.eclipse.actions.TroubleshootWrongAuthentication;
import com.surelogic.sierra.client.eclipse.model.SierraServer;
import com.surelogic.sierra.tool.message.InvalidLoginException;
import com.surelogic.sierra.tool.message.MessageWarehouse;
import com.surelogic.sierra.tool.message.QualifierRequest;
import com.surelogic.sierra.tool.message.Scan;
import com.surelogic.sierra.tool.message.SierraServiceClient;
import com.surelogic.sierra.tool.message.SierraServiceClientException;

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
			final int errNo = 50;
			final String msg = I18N.err(errNo, f_projectName, f_server);
			return SLStatus.createErrorStatus(errNo, msg, e);
		}
	}

	private Set<String> getQualifiersOnTheServer(SLProgressMonitor slMonitor) {
		TroubleshootConnection troubleshoot;
		try {
			List<String> qualifiers = SierraServiceClient.create(
					f_server.getServer()).getQualifiers(new QualifierRequest())
					.getQualifier();
			if (qualifiers == null) {
				qualifiers = Collections.emptyList();
			}
			return new TreeSet<String>(qualifiers);
		} catch (SierraServiceClientException e) {
			if (e instanceof InvalidLoginException) {
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
			SierraServiceClient.create(f_server.getServer()).publishRun(scan);
			return Status.OK_STATUS;
		} catch (SierraServiceClientException e) {
			if (e instanceof InvalidLoginException) {
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
