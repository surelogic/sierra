package com.surelogic.sierra.client.eclipse.actions;

import java.io.File;
import java.io.FilenameFilter;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.surelogic.common.FileUtility;
import com.surelogic.common.XUtil;
import com.surelogic.common.core.jobs.SLProgressMonitorWrapper;
import com.surelogic.common.core.logging.SLEclipseStatusUtility;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.jobs.SLProgressMonitor;
import com.surelogic.common.jobs.SLSeverity;
import com.surelogic.common.jobs.SLStatus;
import com.surelogic.common.jobs.remote.AbstractRemoteSLJob;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.common.serviceability.scan.SierraScanCrashReport;
import com.surelogic.sierra.client.eclipse.Activator;
import com.surelogic.sierra.client.eclipse.jobs.AbstractSierraDatabaseJob;
import com.surelogic.sierra.client.eclipse.model.ConfigGenerator;
import com.surelogic.sierra.client.eclipse.preferences.SierraPreferencesUtility;
import com.surelogic.sierra.tool.ToolException;
import com.surelogic.sierra.tool.ToolUtil;
import com.surelogic.sierra.tool.message.Config;

public class NewScanJob extends WorkspaceJob {

    final Config config;

    final AbstractSierraDatabaseJob afterJob;

    NewScanJob(String name, Config cfg, AbstractSierraDatabaseJob after) {
        super(name);
        config = cfg;
        afterJob = after;
        setPriority(Job.DECORATE);
        // one per workspace
        setRule(ResourcesPlugin.getWorkspace().getRoot());
        afterJob.setPriority(Job.DECORATE);
    }

    @Override
    public IStatus runInWorkspace(IProgressMonitor monitor) {

        SLLogger.getLogger().fine(getName());
        final SLProgressMonitor wrapper = new SLProgressMonitorWrapper(monitor,
                getName());
        SLStatus status = null;
        try {
            for (File l : SierraPreferencesUtility.getSierraScanDirectory()
                    .listFiles(new FilenameFilter() {
                        @Override
                        public boolean accept(File dir, String name) {
                            return name.startsWith(config.getProject() + '.')
                                    && name.endsWith(AbstractRemoteSLJob.LOG_SUFFIX)
                                    && !config.getLogPath().endsWith(name);
                        }
                    })) {
                l.delete();
            }
            status = ToolUtil.scan(System.out, config, wrapper,
                    !XUtil.runJSureInMemory);

            if (afterJob != null && !monitor.isCanceled()
                    && status.getSeverity() != SLSeverity.ERROR) {
                afterJob.schedule();
            }

            // Clean up any copied files if successful
            if (status.getSeverity() == SLSeverity.OK
                    && ConfigGenerator.getInstance().copyBeforeScan()) {
                final String log = config.getLogPath();
                if (log.endsWith(AbstractRemoteSLJob.LOG_SUFFIX)) {
                    final String prefix = log.substring(0, log.length()
                            - AbstractRemoteSLJob.LOG_SUFFIX.length());
                    final File dir = new File(prefix);
                    if (dir.isDirectory()) {
                        FileUtility.recursiveDelete(dir);
                    }
                }
            }
        } catch (Throwable ex) {
            if (!monitor.isCanceled()) {
                return dealWithException(ex);
            }
        }
        if (!monitor.isCanceled()) {
            if (status.getSeverity() == SLSeverity.ERROR/*
                                                         * &&
                                                         * status.getException()
                                                         * != null
                                                         */) {
                // return dealWithException(status.getException());
                return dealWithException(status);
            }
            if (status != SLStatus.OK_STATUS) {
                return SLEclipseStatusUtility.convert(status,
                        Activator.getDefault());
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
        SierraScanCrashReport.getInstance().getReporter()
                .reportScanCrash(status, new File(config.getLogPath()));
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
