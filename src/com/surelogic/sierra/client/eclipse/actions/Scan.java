package com.surelogic.sierra.client.eclipse.actions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jdt.core.IJavaProject;

import com.surelogic.common.SLProgressMonitor;
import com.surelogic.common.eclipse.BalloonUtility;
import com.surelogic.common.eclipse.SLProgressMonitorWrapper;
import com.surelogic.common.eclipse.logging.SLStatus;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.client.eclipse.Activator;
import com.surelogic.sierra.client.eclipse.jobs.ScanDocumentUtility;
import com.surelogic.sierra.client.eclipse.jobs.SierraSchedulingRule;
import com.surelogic.sierra.client.eclipse.model.DatabaseHub;
import com.surelogic.sierra.client.eclipse.preferences.PreferenceConstants;
import com.surelogic.sierra.jdbc.scan.ScanPersistenceException;
import com.surelogic.sierra.tool.SierraConstants;
import com.surelogic.sierra.tool.analyzer.BuildFileGenerator;
import com.surelogic.sierra.tool.ant.SierraAnalysis;
import com.surelogic.sierra.tool.config.Config;

/**
 * Runs Sierra Scan on an Eclipse project.
 * 
 * @author Tanmay.Sinha
 */
public final class Scan {

	private static final Logger LOG = SLLogger.getLogger("sierra");

	/** The status for properly terminated task */
	public static final IStatus PROPER_TERMINATION = SLStatus.createStatus(
			IStatus.OK, 0, "Proper termination", null);

	/** The status for task that completed with errors */
	public static final IStatus IMPROPER_TERMINATION = SLStatus
			.createErrorStatus("Improper termination");

	/** The status for canceled task */
	public static final IStatus TASK_CANCELED = SLStatus.createStatus(
			IStatus.CANCEL, 0, "Task canceled", null);

	/** The status for already running task - currently uses cancel */
	public static final IStatus TASK_ALREADY_RUNNING = SLStatus.createStatus(
			IStatus.CANCEL, 0, "Task cancelled", null);

	private static final String SIERRA_JOB = "sierra";

	private final List<IJavaProject> f_selectedProjects = new ArrayList<IJavaProject>();

	private File f_resultRoot;

	private List<Config> f_configs;

	public Scan(List<IJavaProject> selectedProjects) {
		// Get the plug-in directory that has tools folder and append the
		// directory
		f_selectedProjects.addAll(selectedProjects);
		f_resultRoot = new File(SierraConstants.SIERRA_RESULTS_PATH);

		if ((!f_resultRoot.exists()) || (f_resultRoot.exists())
				&& (!f_resultRoot.isDirectory())) {

			f_resultRoot.mkdir();
		}
	}

	public void execute() {
		f_configs = new ArrayList<Config>();

		// Get from preference page
		final String sierraPath = Activator.getDefault().getPluginPreferences()
				.getString(PreferenceConstants.P_SIERRA_PATH);
		final File sierraFolder = new File(sierraPath);

		final StringBuilder projectList = new StringBuilder();

		for (IJavaProject project : f_selectedProjects) {
			projectList.append(" ").append(project.getProject().getName());

			String projectPath = project.getResource().getLocation().toString();

			File baseDir = new File(projectPath);
			File runDocument = new File(sierraPath + File.separator
					+ project.getProject().getName()
					+ SierraConstants.PARSED_FILE_SUFFIX);

			Config config = new Config();

			config.setBaseDirectory(baseDir);
			config.setProject(project.getProject().getName());
			config.setDestDirectory(f_resultRoot);
			config.setRunDocument(runDocument);
			config.setJavaVendor(System.getProperty("java.vendor"));

			// Get the plug-in directory that has tools folder and append the
			// directory
			String tools = BuildFileGenerator.getToolsDirectory();
			tools = tools + SierraConstants.TOOLS_FOLDER;
			config.setToolsDirectory(new File(tools));
			File runDocumentHolder = new File(sierraFolder + File.separator
					+ config.getProject() + SierraConstants.PARSED_FILE_SUFFIX);
			config.setRunDocument(runDocumentHolder);
			f_configs.add(config);
		}

		if (f_configs.size() > 0) {

			for (Config c : f_configs) {

				final Job runSingleSierraScan = new RunSingleSierraJob(
						"Running Sierra on " + c.getProject(), c, SIERRA_JOB);
				runSingleSierraScan.setPriority(Job.SHORT);
				runSingleSierraScan.belongsTo(c.getProject());
				runSingleSierraScan
						.addJobChangeListener(new RunSierraAdapter());
				runSingleSierraScan.schedule();
			}

			showStartBalloon();
			LOG.info("Started scan on projects:" + projectList);

		}
	}

	private static class RunSingleSierraJob extends Job {

		private final Config f_config;
		private final String f_familyName;

		@Override
		public boolean belongsTo(Object family) {
			return f_familyName.equals(family);
		}

		public RunSingleSierraJob(String name, Config config, String familyName) {
			super(name);
			f_config = config;
			f_familyName = familyName;
			setRule(SierraSchedulingRule.getInstance());

		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			try {

				// This is an estimate for the number of files in a project
				int scale = 1000;
				int total = 5;
				if (monitor != null) {
					monitor.beginTask("Scanning project", total * scale);
				}

				SLProgressMonitorWrapper slProgressMonitorWrapper = new SLProgressMonitorWrapper(
						monitor);
				final AntSingleRunnable antRunnable = new AntSingleRunnable(
						f_config, slProgressMonitorWrapper, scale);
				final Thread antThread = new Thread(antRunnable);
				antThread.start();

				showStartBalloon();

				while (!monitor.isCanceled()) {
					Thread.sleep(500);
					if (antRunnable.isCompleted()) {
						break;
					}
				}

				if (monitor.isCanceled()) {
					monitor.done();
					antRunnable.stopAll();
					return TASK_CANCELED;
				} else {
					monitor.done();
					LOG.info("Completed scan");
					return PROPER_TERMINATION;
				}

			} catch (Exception e) {
				LOG.log(Level.SEVERE,
						"Exception while trying to excute ant build task" + e);
			}

			monitor.done();
			return IMPROPER_TERMINATION;
		}
	}

	private static class AntSingleRunnable implements Runnable {

		private boolean f_complete;
		private SierraAnalysis f_sierraAnalysis;
		private final Config f_config;
		private SLProgressMonitor f_monitor;
		private int f_scale = 1;

		public AntSingleRunnable(Config config, SLProgressMonitor monitor,
				int scale) {
			f_config = config;
			f_monitor = monitor;
			f_scale = scale;
		}

		public void run() {

			f_complete = false;
			f_sierraAnalysis = new SierraAnalysis(f_config, f_monitor, f_scale);
			f_sierraAnalysis.execute();

			if (!f_monitor.isCanceled()) {
				try {
					ScanDocumentUtility.loadScanDocument(f_config
							.getRunDocument(), f_monitor);

					// Notify that scan was completed
					DatabaseHub.getInstance().notifyScanLoaded();
				} catch (ScanPersistenceException rpe) {
					LOG.severe(rpe.getMessage());
					BalloonUtility.showMessage(
							"Sierra Scan Completed with Errors",
							"Check the logs.");
				}
			}
			f_complete = true;
		}

		public void stopAll() {
			f_sierraAnalysis.stop();
		}

		public boolean isCompleted() {
			return f_complete;
		}
	}

	private static class RunSierraAdapter extends JobChangeAdapter {

		@Override
		public void done(IJobChangeEvent event) {
			if (event.getResult().equals(PROPER_TERMINATION)) {
				BalloonUtility.showMessage("Sierra Scan Completed",
						"You may now examine the results.");

			} else if (event.getResult().equals(TASK_CANCELED)) {
				LOG.info("Canceled scan");
				BalloonUtility.showMessage("Sierra Scan was cancelled",
						"Check the logs.");
			} else if (event.getResult().equals(TASK_ALREADY_RUNNING)) {
				BalloonUtility.showMessage("Sierra Scan Already Running",
						"Please wait for it to finish before restarting.");

			} else {
				BalloonUtility.showMessage("Sierra Scan Completed with Errors",
						"Check the logs.");
			}
		}
	}

	private static void showStartBalloon() {
		BalloonUtility.showMessage("Sierra Scan Started",
				"You may continue your work. "
						+ "You will be notified when the scan has completed.");
	}
}
