package com.surelogic.sierra.client.eclipse.actions;

import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.resources.team.ResourceRuleFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.surelogic.common.SLProgressMonitor;
import com.surelogic.common.eclipse.SLProgressMonitorWrapper;
import com.surelogic.common.eclipse.jobs.DatabaseJob;
import com.surelogic.common.eclipse.logging.SLStatus;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.tool.ToolUtil;
import com.surelogic.sierra.tool.message.Config;

public class NewScanJob extends WorkspaceJob {

	final Config config;

	final DatabaseJob afterJob;

	NewScanJob(String name, Config cfg, DatabaseJob after) {
		super(name);
		config = cfg;
		afterJob = after;
		setPriority(Job.DECORATE);
		setRule(new ResourceRuleFactory() {/* Nothing to do here */
		}.buildRule());
		afterJob.setPriority(Job.DECORATE);
	}

	@Override
	public IStatus runInWorkspace(IProgressMonitor monitor) {
		SLLogger.getLogger().fine(this.getName());
		final SLProgressMonitor wrapper = new SLProgressMonitorWrapper(monitor);
		try {
			ToolUtil.scan(config, wrapper, true);

			if (afterJob != null) {
				afterJob.schedule();
			}
		} catch (Throwable ex) {
			if (!monitor.isCanceled()) {
				wrapper.failed("Caught exception while " + getName(), ex);
			}
		}
		if (wrapper.getFailureTrace() != null && !monitor.isCanceled()) {
			final Throwable e = wrapper.getFailureTrace();
			final int errNo = 46;
			final String msg = I18N.err(errNo, getName());
			return SLStatus.createErrorStatus(errNo, msg, e);
		}
		return Status.OK_STATUS;
	}
}
