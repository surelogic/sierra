package com.surelogic.sierra.client.eclipse.jobs;

import java.sql.SQLException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import com.surelogic.common.eclipse.jobs.DatabaseJob;
import com.surelogic.common.eclipse.jobs.SLProgressMonitorWrapper;
import com.surelogic.common.eclipse.logging.SLEclipseStatusUtility;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.jdbc.DBQuery;
import com.surelogic.common.jobs.SLProgressMonitor;
import com.surelogic.sierra.client.eclipse.Data;
import com.surelogic.sierra.jdbc.settings.SettingQueries;
import com.surelogic.sierra.tool.message.ScanFilter;

public final class OverwriteLocalScanFilterJob extends DatabaseJob {
	private static final int ERROR_NUM = 48; // FIX
	private final ScanFilter f_filter;
	
	public OverwriteLocalScanFilterJob(ScanFilter f) {
		super("Overwriting local scan filter with filter '"+f.getName()+"'");
		f_filter = f;	
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		final String msg = "Overwriting local scan filter with filter '"+f_filter.getName()+"'";
		final SLProgressMonitor slMonitor = new SLProgressMonitorWrapper(
				monitor, msg);
		slMonitor.begin(5);
		IStatus status = null;
		try {
			status = overwriteFilter(slMonitor);
		} catch (final Throwable e) {
			final int errNo = ERROR_NUM; 
			final String errMsg = I18N.err(errNo, f_filter.getName());
			status = SLEclipseStatusUtility.createWarningStatus(errNo, errMsg,
					e);
		}
		return status;
	}

	private IStatus overwriteFilter(SLProgressMonitor slMonitor) throws SQLException {
		final DBQuery<?> query = SettingQueries.updateDefaultScanFilter(f_filter.getUid());			
		Data.getInstance().withTransaction(query);

		return Status.OK_STATUS;
	}
}
