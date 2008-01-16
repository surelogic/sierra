package com.surelogic.sierra.client.eclipse.actions;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;

import com.surelogic.common.eclipse.BalloonUtility;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.client.eclipse.preferences.PreferenceConstants;

/**
 * The adapter for the {@link ScanProjectJob}, handles all the possible
 * cases for status messages from the job and displays and logs appropriate
 * message.
 * 
 * @author Tanmay.Sinha
 * 
 */
class ScanProjectJobAdapter extends JobChangeAdapter {
  /** The logger */
  private static final Logger LOG = SLLogger.getLogger("sierra");
  
	private final String f_projectName;

	/**
	 * 
	 * @param projectName
	 *            the name of the project
	 */
	public ScanProjectJobAdapter(String projectName) {
		super();
		this.f_projectName = projectName;
	}

	@Override
	public void running(IJobChangeEvent event) {
		LOG.info("Starting scan on " + f_projectName);
	}

	@Override
	public void done(IJobChangeEvent event) {
		if (event.getResult().equals(Status.OK_STATUS)) {
			LOG.info("Completed scan for " + f_projectName);
			if (PreferenceConstants.showBalloonNotifications())
				BalloonUtility
						.showMessage("Sierra Scan Completed on "
								+ f_projectName,
								"You may now examine the results.");

		} else if (event.getResult().equals(Status.CANCEL_STATUS)) {
			LOG.info("Canceled scan on " + f_projectName);
		} else {
			Throwable t = event.getResult().getException();
			LOG.log(Level.SEVERE,
					"(top-level) Error while trying to run scan on "
							+ f_projectName, t);
			if (event.getResult().isMultiStatus()) {
				for (IStatus s : event.getResult().getChildren()) {
					Throwable t1 = s.getException();
					LOG.log(Level.SEVERE,
							"(multi-status) Error while trying to run scan on "
									+ f_projectName, t1);
				}
			}
		}
	}
}