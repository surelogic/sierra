package com.surelogic.sierra.client.eclipse.actions;

import java.sql.Connection;
import java.sql.SQLException;
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

import com.surelogic.common.eclipse.BalloonUtility;
import com.surelogic.common.eclipse.jdt.JavaUtil;
import com.surelogic.common.eclipse.jobs.DatabaseJob;
import com.surelogic.common.eclipse.logging.SLStatus;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.client.eclipse.Data;
import com.surelogic.sierra.jdbc.scan.ScanInfo;
import com.surelogic.sierra.jdbc.scan.ScanManager;

/**
 * Scan the changes in the selected projects
 * 
 * @author Edwin.Chan
 */
public class ScanChangedProjectsAction extends
		AbstractProjectSelectedMenuAction {
	protected static final Logger LOG = SLLogger.getLogger();

	@Override
	protected void run(final List<IJavaProject> projects,
			final List<String> projectNames) {
		run(projects);
	}

	@Override
	public void run(final List<IJavaProject> projects) {
		if (projects.size() <= 0) {
			return;
		}
		new DatabaseJob("Checking last scan times") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				monitor.beginTask(getName(), projects.size() + 1);
				try {
					Connection conn = Data.readOnlyConnection();
					Map<IJavaProject, Date> times = new HashMap<IJavaProject, Date>(
							projects.size());
					List<IJavaProject> noScanYet = null;
					List<String> projectNames = null;
					ScanManager manager = ScanManager.getInstance(conn);
					try {
						for (IJavaProject p : projects) {
							ScanInfo info = manager.getLatestScanInfo(p.getElementName());
							if (info != null) {
								times.put(p, info.getScanTime());
							} else {
								// No scan on the project yet
								if (noScanYet == null) {
									noScanYet = new ArrayList<IJavaProject>();
									projectNames = new ArrayList<String>();
								}
								noScanYet.add(p);
								projectNames.add(p.getElementName());
							}
						}
					} finally {
						conn.close();
					}
					monitor.worked(1);

					Collection<ICompilationUnit> selectedCompilationUnits = JavaUtil
							.modifiedCompUnits(times, monitor);

					boolean startedScan = false;
					if (!selectedCompilationUnits.isEmpty()) {
						new NewPartialScan().scan(selectedCompilationUnits);
						startedScan = true;
					}
					if (noScanYet != null) {
						new NewScan().scan(noScanYet, projectNames);
						startedScan = true;
					}

					if (!startedScan) {
						BalloonUtility
								.showMessage("Nothing changed",
										"Sierra did not detect any files that changed since your last scan(s)");
					}
				} catch (SQLException e) {
					final int errNo = 46;
					final String msg = I18N.err(errNo, getName());
					return SLStatus.createErrorStatus(errNo, msg, e);
				}
				return Status.OK_STATUS;
			}
		}.schedule();
	}
}
