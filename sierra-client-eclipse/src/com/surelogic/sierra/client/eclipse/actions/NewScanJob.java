package com.surelogic.sierra.client.eclipse.actions;

import java.io.File;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.surelogic.common.core.jobs.SLProgressMonitorWrapper;
import com.surelogic.common.core.jobs.WorkspaceLockingJob;
import com.surelogic.common.core.logging.SLEclipseStatusUtility;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.jobs.SLProgressMonitor;
import com.surelogic.common.jobs.SLSeverity;
import com.surelogic.common.jobs.SLStatus;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.common.serviceability.scan.SierraScanCrashReport;
import com.surelogic.sierra.client.eclipse.Activator;
import com.surelogic.sierra.client.eclipse.jobs.AbstractSierraDatabaseJob;
import com.surelogic.sierra.tool.ToolException;
import com.surelogic.sierra.tool.ToolUtil;
import com.surelogic.sierra.tool.message.Config;

public class NewScanJob extends WorkspaceLockingJob {

	final Config config;

	final AbstractSierraDatabaseJob afterJob;

	NewScanJob(String name, Config cfg, AbstractSierraDatabaseJob after) {
		super(name);
		config = cfg;
		afterJob = after;
		setPriority(Job.DECORATE);		
		//setRule(new ResourceRuleFactory() {/* Nothing to do here */
		//}.buildRule());
		afterJob.setPriority(Job.DECORATE);
	}

	@Override
	public IStatus runInWorkspace(IProgressMonitor monitor) {
		SLLogger.getLogger().fine(this.getName());
		final SLProgressMonitor wrapper = new SLProgressMonitorWrapper(monitor,
				this.getName());
		SLStatus status = null;
		try {
			status = ToolUtil.scan(System.out, config, wrapper, true);

			if (afterJob != null && !monitor.isCanceled() && status.getSeverity() != SLSeverity.ERROR) {
				afterJob.schedule();
			}
		} catch (Throwable ex) {
			if (!monitor.isCanceled()) {
				return dealWithException(ex);
			}
		}
		if (!monitor.isCanceled()) {
			if (status.getSeverity() == SLSeverity.ERROR/* && status.getException() != null*/) {
				//return dealWithException(status.getException());
				return dealWithException(status);
			}
			if (status != SLStatus.OK_STATUS) {
				return SLEclipseStatusUtility.convert(status, Activator.getDefault());
			}
		}
		return Status.OK_STATUS;
	}

	private IStatus dealWithException(final Throwable t) {
		Throwable e = unwrapException(t);
		final int errNo;
		final String msg;
		if (e instanceof ToolException) {
			ToolException te = (ToolException) e;
			errNo = te.getErrorNum();
			msg = te.getToolMessage(getName());
			if (te.getCause() != null) {
				e = te.getCause();
			}
		} else {
			errNo = 46;
			msg = I18N.err(errNo, getName());
		}
		SLStatus status = SLStatus.createErrorStatus(errNo, msg, e);
		return dealWithException(status);
	}
	
	private IStatus dealWithException(SLStatus status) { 
		SierraScanCrashReport.getInstance().getReporter().reportScanCrash(status, new File(config.getLogPath()));
		return Status.CANCEL_STATUS;
	}

	private Throwable unwrapException(Throwable e) {
		while (e instanceof RuntimeException) {
			Throwable cause = e.getCause();
			if (cause == null) {
				break;
			}
			e = cause;
		}
		return e;
	}
}
