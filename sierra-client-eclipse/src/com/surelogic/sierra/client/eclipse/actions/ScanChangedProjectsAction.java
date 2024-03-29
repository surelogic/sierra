package com.surelogic.sierra.client.eclipse.actions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;

import com.surelogic.common.core.JDTUtility;
import com.surelogic.common.core.logging.SLEclipseStatusUtility;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.jdbc.DBQuery;
import com.surelogic.common.jdbc.Query;
import com.surelogic.common.jdbc.TransactionException;
import com.surelogic.common.jobs.NullSLProgressMonitor;
import com.surelogic.common.jobs.SLStatus;
import com.surelogic.common.license.SLLicenseProduct;
import com.surelogic.common.license.SLLicenseUtility;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.common.ui.BalloonUtility;
import com.surelogic.common.ui.actions.AbstractProjectSelectedMenuAction;
import com.surelogic.common.ui.dialogs.ErrorDialogUtility;
import com.surelogic.sierra.client.eclipse.Activator;
import com.surelogic.sierra.client.eclipse.Data;
import com.surelogic.sierra.client.eclipse.jobs.AbstractSierraDatabaseJob;
import com.surelogic.sierra.jdbc.scan.ScanInfo;
import com.surelogic.sierra.jdbc.scan.Scans;

/**
 * Scan the changes in the selected projects
 * 
 * @author Edwin.Chan
 */
public class ScanChangedProjectsAction extends
		AbstractProjectSelectedMenuAction {
	protected static final Logger LOG = SLLogger.getLogger();

	@Override
	protected void runActionOn(final List<IJavaProject> projects) {
		run(projects);
	}

	@Override
	public void run(final List<IJavaProject> projects) {
		if (projects.size() <= 0) {
			return;
		}

		/*
		 * License check: A hack because Sierra is not using SLJobs yet.
		 */
		final SLStatus failed = SLLicenseUtility.validateSLJob(
				SLLicenseProduct.SIERRA, new NullSLProgressMonitor());
		if (failed != null) {
			IStatus status = SLEclipseStatusUtility.convert(failed, Activator
					.getDefault());
			ErrorDialogUtility.open(null, null, status, true);
			return;
		}

		new AbstractSierraDatabaseJob("Checking last scan times") {
			@Override
			protected IStatus run(final IProgressMonitor monitor) {
				monitor.beginTask(getName(), projects.size() + 1);
				try {
					Data.getInstance().withReadOnly(new DBQuery<Void>() {
						@Override
            public Void perform(final Query q) {
							final Map<IJavaProject, Date> times = new HashMap<IJavaProject, Date>(
									projects.size());
							List<IJavaProject> noScanYet = null;
							final Scans scans = new Scans(q);
							for (final IJavaProject p : projects) {
								final ScanInfo info = scans.getLatestScanInfo(p
										.getElementName());
								if (info != null) {
									times.put(p, info.getScanTime());
								} else {
									// No scan on the project yet
									if (noScanYet == null) {
										noScanYet = new ArrayList<IJavaProject>();
									}
									noScanYet.add(p);
								}
							}

							monitor.worked(1);

							final Collection<ICompilationUnit> selectedCompilationUnits = JDTUtility
									.modifiedCompUnits(times, monitor);

							boolean startedScan = false;
							if (!selectedCompilationUnits.isEmpty()) {
								new NewPartialScan()
										.scan(selectedCompilationUnits);
								startedScan = true;
							}
							if (noScanYet != null) {
								new NewScan().scan(noScanYet);
								startedScan = true;
							}

							if (!startedScan) {
								BalloonUtility
										.showMessage("Nothing changed",
												"Sierra did not detect any files that changed since your last scan(s)");
							}
							return null;
						}
					});
				} catch (final TransactionException e) {
					final int errNo = 46;
					final String msg = I18N.err(errNo, getName());
					return SLEclipseStatusUtility.createErrorStatus(errNo, msg,
							e);
				}
				return Status.OK_STATUS;
			}
		}.schedule();
	}
}
