package com.surelogic.sierra.client.eclipse.actions;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.UIJob;

import com.surelogic.common.eclipse.jobs.SLUIJob;
import com.surelogic.common.i18n.I18N;
import com.surelogic.sierra.client.eclipse.dialogs.TimeseriesSelectionDialog;

/**
 * Used to prompt the user from a running job for a set of timeseries.
 */
public final class TimeseriesPromptFromJob {

	private final Set<String> f_timeseries;
	private final Set<String> f_selectedTimeseries = new HashSet<String>();
	private final String f_projectName;
	private final String f_serverLabel;

	/**
	 * Constructs this object.
	 * 
	 * @param server
	 *            the mutable server configuration to be fixed.
	 */
	public TimeseriesPromptFromJob(Set<String> timeseries, String projectName,
			String serverLabel) {
		if (timeseries == null)
			throw new IllegalArgumentException(I18N.err(44, "timeseries"));
		f_timeseries = new HashSet<String>(timeseries);
		if (projectName == null)
			throw new IllegalArgumentException(I18N.err(44, "projectName"));
		f_projectName = projectName;
		if (serverLabel == null)
			throw new IllegalArgumentException(I18N.err(44, "serverLabel"));
		f_serverLabel = serverLabel;
	}

	public void open() {
		final UIJob job = new SLUIJob() {
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				if (!f_timeseries.isEmpty()) {
					TimeseriesSelectionDialog dialog = new TimeseriesSelectionDialog(
							PlatformUI.getWorkbench()
									.getActiveWorkbenchWindow().getShell(),
							f_timeseries, f_projectName, f_serverLabel);
					if (dialog.open() != Window.CANCEL) {
						f_selectedTimeseries.addAll(dialog
								.getSelectedTimeseries());
						f_useForAllOnSameServer = dialog
								.useForAllOnSameServer();
					} else {
						f_canceled = true;
					}
				}
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}

	/**
	 * Gets the set of timeseries selected by the user.
	 * <p>
	 * The results of this method are only valid after a call to {@link #open()}
	 * when {@link #isCanceled()} is <code>false</code>.
	 * 
	 * @return the set of timeseries selected by the user.
	 */
	public Set<String> getSelectedTimeseries() {
		return new HashSet<String>(f_selectedTimeseries);
	}

	private boolean f_canceled = false;

	/**
	 * Indicates that the user didn't try to fix the server location and
	 * authentication data.
	 * 
	 * @return <code>false</code> if the user fixed the server location and
	 *         authentication data, <code>false</code> otherwise.
	 */
	public boolean isCanceled() {
		return f_canceled;
	}

	private boolean f_useForAllOnSameServer = false;

	/**
	 * Indicates that the resulting set of timeseries should be used for all
	 * other projects sharing runs to the same Sierra server.
	 * <p>
	 * The results of this method are only valid after a call to {@link #open()}
	 * when {@link #isCanceled()} is <code>false</code>.
	 * 
	 * @return <code>true</code> if the timeseries should be used for all
	 *         other projects sharing runs to the same Sierra server,
	 *         <code>false</code> otherwise.
	 */
	public boolean useForAllOnSameServer() {
		return f_useForAllOnSameServer;
	}
}
