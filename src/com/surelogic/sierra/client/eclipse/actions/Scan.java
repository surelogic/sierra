package com.surelogic.sierra.client.eclipse.actions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jdt.core.IJavaProject;

import com.surelogic.common.SLProgressMonitor;
import com.surelogic.common.eclipse.BalloonUtility;
import com.surelogic.common.eclipse.SLProgressMonitorWrapper;
import com.surelogic.common.eclipse.logging.SLStatus;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.client.eclipse.jobs.ScanDocumentUtility;
import com.surelogic.sierra.client.eclipse.jobs.SierraSchedulingRule;
import com.surelogic.sierra.client.eclipse.model.ConfigGenerator;
import com.surelogic.sierra.client.eclipse.model.DatabaseHub;
import com.surelogic.sierra.client.eclipse.preferences.PreferenceConstants;
import com.surelogic.sierra.jdbc.scan.ScanPersistenceException;
import com.surelogic.sierra.tool.SierraConstants;
import com.surelogic.sierra.tool.ant.SierraAnalysis;
import com.surelogic.sierra.tool.config.Config;

/**
 * Runs Sierra Scan on an Eclipse project.
 * 
 * @author Tanmay.Sinha
 */
public final class Scan {

	/** The logger */
	private static final Logger LOG = SLLogger.getLogger("sierra");

	/** The Sierra job family name */
	private static final String SIERRA = "sierra";

	/** The list of selected projects */
	private final List<IJavaProject> f_selectedProjects = new ArrayList<IJavaProject>();

	/** The location to store tool results */
	private final File f_resultRoot;

	/** The list of configurations to run scan on */
	private List<Config> f_configs;

	/**
	 * The constructor for the Scan
	 * 
	 * @param selectedProjects
	 *            the list of IJavaProjects for scan
	 * 
	 */
	public Scan(List<IJavaProject> selectedProjects) {
		/*
		 * Get the plug-in directory that has tools folder and append the
		 * directory
		 */
		f_selectedProjects.addAll(selectedProjects);
		f_resultRoot = new File(SierraConstants.SIERRA_RESULTS_PATH);

		if ((!f_resultRoot.exists()) || (f_resultRoot.exists())
				&& (!f_resultRoot.isDirectory())) {

			f_resultRoot.mkdir();
		}
	}

	public void execute() {
		f_configs = new ArrayList<Config>();

		final StringBuilder projectList = new StringBuilder();

		f_configs = ConfigGenerator.getInstance()
				.getConfigs(f_selectedProjects);
		if (f_configs.size() > 0) {

			/* Run the scan on the all the configs */
			for (Config c : f_configs) {
				projectList.append(" ").append(c.getProject());
				final Job runSingleSierraScan = new ScanProjectJob(
						"Running Sierra on " + c.getProject(), c, SIERRA);
				runSingleSierraScan.setPriority(Job.SHORT);
				runSingleSierraScan.belongsTo(c.getProject());
				runSingleSierraScan
						.addJobChangeListener(new ScanProjectJobAdapter(c
								.getProject()));
				runSingleSierraScan.schedule();

			}
			if (PreferenceConstants.showBalloonNotifications())
				BalloonUtility.showMessage("Sierra Scan Started on"
						+ projectList, "You may continue your work. "
						+ "You will be notified when the scan has completed.");
		}
	}

	/**
	 * The job to scan a project. There is one job per project.
	 * 
	 * @author Tanmay.Sinha
	 * 
	 */
	private static class ScanProjectJob extends Job {

		private final Config f_config;
		private final String f_familyName;

		/**
		 * 
		 * The constructor for a Sierra scan
		 * 
		 * @param name
		 *            the name of the job.
		 * @param config
		 *            the config object for tool run.
		 * @param familyName
		 *            the family name.
		 */
		public ScanProjectJob(String name, Config config, String familyName) {
			super(name);
			f_config = config;
			f_familyName = familyName;
			setRule(SierraSchedulingRule.getInstance());
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {

			// ADDED: Dynamic adjustment of the total and scale depending on the
			// options selected for tool to be run. Current we assume maximum 6
			// units of work in total. 4 units for tools (1 per tool) 1 for
			// generating scan document and 1 for storing in the database

			int scale = 5000;
			int total = 6 - ConfigGenerator.getInstance()
					.getNumberofExcludedTools();
			SLProgressMonitorWrapper slProgressMonitorWrapper = null;
			if (monitor != null) {
				slProgressMonitorWrapper = new SLProgressMonitorWrapper(monitor);
				slProgressMonitorWrapper.beginTask("Scanning project", total
						* scale);
			}

			try {
				final AntRunnable antRunnable = new AntRunnable(f_config,
						slProgressMonitorWrapper, scale);
				final Thread antThread = new Thread(antRunnable);
				antThread.start();

				while (!slProgressMonitorWrapper.isCanceled()) {
					Thread.sleep(500);
					if (antRunnable.isCompleted() || antRunnable.hasError()) {
						break;
					}
				}

				if (antRunnable.hasError()) {
					slProgressMonitorWrapper.done();
					String message = antRunnable.getErrorMessage();
					return SLStatus.createErrorStatus(message);
				} else if (slProgressMonitorWrapper.isCanceled()) {
					slProgressMonitorWrapper.done();
					antRunnable.stopAll();
					return Status.CANCEL_STATUS;
				} else {
					try {
						/* Start database entry */
						ScanDocumentUtility.loadScanDocument(f_config
								.getScanDocument(), slProgressMonitorWrapper,
								f_config.getProject());
						/* Notify that scan was completed */
						DatabaseHub.getInstance().notifyScanLoaded();

						/* Rename the scan document */
						File scanDocument = f_config.getScanDocument();
						File newScanDocument = new File(PreferenceConstants
								.getSierraPath()
								+ File.separator
								+ f_config.getProject()
								+ SierraConstants.PARSED_FILE_SUFFIX);
						/*
						 * This approach assures that the scan document
						 * generation will not crash. The tool will simply
						 * override the existing scan document no matter how
						 * recent it is.
						 * 
						 */
						if (newScanDocument.exists()) {
							newScanDocument.delete();
						}
						scanDocument.renameTo(newScanDocument);
					} catch (ScanPersistenceException rpe) {
						LOG.severe(rpe.getMessage());
						slProgressMonitorWrapper.done();
						return SLStatus.createErrorStatus("Scan failed", rpe);
					} catch (IllegalStateException ise) {
						LOG.severe(ise.getMessage());
						slProgressMonitorWrapper.done();
						return SLStatus.createErrorStatus("Scan failed", ise);
					}
					slProgressMonitorWrapper.done();
					return Status.OK_STATUS;
				}

			} catch (Exception e) {
				LOG.log(Level.SEVERE,
						"Exception while trying to execute ant build task" + e);
				return SLStatus.createErrorStatus("Scan failed", e);
			}
		}

		@Override
		public boolean belongsTo(Object family) {
			return f_familyName.equals(family);
		}

	}

	/**
	 * The thread to run the ant task, it allows polling to check for
	 * completion. Also provides method to stop the tool runs and determines any
	 * exception that occured
	 * 
	 * @author Tanmay.Sinha
	 * 
	 */
	private static class AntRunnable implements Runnable {

		private boolean f_complete;
		private SierraAnalysis f_sierraAnalysis;
		private final Config f_config;
		private final SLProgressMonitor f_monitor;
		private int f_scale = 1;
		private boolean f_error = false;
		private String f_errorMessage;

		/**
		 * The constructor for thread
		 * 
		 * @param config
		 *            the config object for tool run.
		 * @param monitor
		 *            the monitor to allow progress indication
		 * @param scale
		 *            the scale for monitor
		 */
		public AntRunnable(Config config, SLProgressMonitor monitor, int scale) {
			f_config = config;
			f_monitor = monitor;
			f_scale = scale;
		}

		public void run() {

			try {
				f_complete = false;
				f_sierraAnalysis = new SierraAnalysis(f_config, f_monitor,
						f_scale);
				f_sierraAnalysis.execute();
				f_complete = true;
			} catch (RuntimeException re) {
				f_error = true;
				f_errorMessage = re.getMessage();
			}
		}

		public void stopAll() {
			f_sierraAnalysis.stop();
		}

		public boolean hasError() {
			return f_error;
		}

		public boolean isCompleted() {
			return f_complete;
		}

		public String getErrorMessage() {
			return f_errorMessage;
		}

	}

	/**
	 * The adapter for the {@link ScanProjectJob}, handles all the possible
	 * cases for status messages from the job and displays and logs appropriate
	 * message.
	 * 
	 * @author Tanmay.Sinha
	 * 
	 */
	private static class ScanProjectJobAdapter extends JobChangeAdapter {

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
				LOG
						.severe("Error while trying to run scan on "
								+ f_projectName);
			}
		}
	}

}
