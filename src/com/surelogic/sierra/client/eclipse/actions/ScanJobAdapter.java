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
 * The adapter for various scan jobs, handles all the possible cases for status
 * messages from the job and displays and logs appropriate message.
 * 
 * @author Tanmay.Sinha
 * @author Edwin.Chan
 */
class ScanJobAdapter extends JobChangeAdapter {
	/** The logger */
	private static final Logger LOG = SLLogger.getLogger();

	private final String f_scanName;
	private final String scan;

	/**
	 * 
	 * @param name
	 *            The name of the project/comp unit being scanned
	 */
	public ScanJobAdapter(String name) {
		this(name, false);
	}

	public ScanJobAdapter(String name, boolean isRescan) {
		super();
		f_scanName = name;
		scan = isRescan ? "re-scan" : "scan";
	}

	@Override
	public void running(IJobChangeEvent event) {
		if (LOG.isLoggable(Level.FINE)) {
			LOG.fine("Starting to load " + scan + " on " + f_scanName);
		}
	}

	@Override
	public void done(IJobChangeEvent event) {
		final boolean fineIsLoggable = LOG.isLoggable(Level.FINE);
		if (event.getResult().equals(Status.OK_STATUS)) {
			if (fineIsLoggable) {
				LOG.fine("Completed " + scan + " for " + f_scanName);
			}
			if (PreferenceConstants.showBalloonNotifications())
				BalloonUtility.showMessage("Sierra " + scan + " completed on "
						+ f_scanName, "You may now examine the results.");

		} else if (event.getResult().equals(Status.CANCEL_STATUS)) {
			if (fineIsLoggable) {
				LOG.fine("Canceled " + scan + " on " + f_scanName);
			}
		} else {
			Throwable t = event.getResult().getException();
			LOG.log(Level.SEVERE, "(top-level) Error while trying to run "
					+ scan + " on " + f_scanName, t);
			if (event.getResult().isMultiStatus()) {
				for (IStatus s : event.getResult().getChildren()) {
					Throwable t1 = s.getException();
					LOG.log(Level.SEVERE,
							"(multi-status) Error while trying to run " + scan
									+ " on " + f_scanName, t1);
				}
			}
		}
	}
}