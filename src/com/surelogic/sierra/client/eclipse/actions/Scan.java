package com.surelogic.sierra.client.eclipse.actions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jdt.core.IJavaProject;

import com.surelogic.common.eclipse.BalloonUtility;
import com.surelogic.common.eclipse.SLProgressMonitorWrapper;
import com.surelogic.common.eclipse.logging.SLStatus;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.client.eclipse.Activator;
import com.surelogic.sierra.client.eclipse.jobs.RunDocumentUtility;
import com.surelogic.sierra.client.eclipse.preferences.PreferenceConstants;
import com.surelogic.sierra.jdbc.run.RunPersistenceException;
import com.surelogic.sierra.tool.SierraConstants;
import com.surelogic.sierra.tool.analyzer.BuildFileGenerator;
import com.surelogic.sierra.tool.analyzer.Parser;
import com.surelogic.sierra.tool.ant.SierraAnalysis;
import com.surelogic.sierra.tool.config.Config;

/**
 * Runs Sierra Analysis on an Eclipse project.
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

	private final List<IJavaProject> f_selectedProjects = new ArrayList<IJavaProject>();

	private File f_resultRoot;

	// private File f_buildFile;

	private BuildFileGenerator f_buildFileGenerator;

	private Config f_config;

	private List<File> f_buildFiles;

	private final File f_toolsDirectory;

	private List<Config> f_configs;

	public Scan(List<IJavaProject> selectedProjects) {
		// Get the plug-in directory that has tools folder and append the
		// directory
		f_selectedProjects.addAll(selectedProjects);
		String tools = BuildFileGenerator.getToolsDirectory()
				+ SierraConstants.TOOLS_FOLDER;
		f_toolsDirectory = new File(tools);
		f_resultRoot = new File(SierraConstants.SIERRA_RESULTS_PATH);

		if ((!f_resultRoot.exists()) || (f_resultRoot.exists())
				&& (!f_resultRoot.isDirectory())) {

			f_resultRoot.mkdir();
		}
	}

	public void execute() {

		f_buildFiles = new ArrayList<File>();

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

			f_config = new Config();

			f_config.setBaseDirectory(baseDir);
			f_config.setProject(project.getProject().getName());
			f_config.setDestDirectory(f_resultRoot);
			f_config.setRunDocument(runDocument);

			// Get the plug-in directory that has tools folder and append the
			// directory
			String tools = BuildFileGenerator.getToolsDirectory();
			tools = tools + SierraConstants.TOOLS_FOLDER;
			f_config.setToolsDirectory(new File(tools));

			f_buildFileGenerator = new BuildFileGenerator(f_config);
			File buildFile = f_buildFileGenerator.writeBuildFile();
			f_buildFiles.add(buildFile);

		}

		if (f_buildFiles.size() > 0) {

			f_configs = new ArrayList<Config>();

			Stack<File> runDocuments = new Stack<File>();
			// Generate configs from all build files
			for (File f : f_buildFiles) {
				Parser buildFileParser = new Parser();
				List<Config> configsHolder = buildFileParser.parseBuildFile(f
						.getAbsolutePath());
				for (Config c : configsHolder) {
					File runDocumentHolder = new File(sierraFolder
							+ File.separator + c.getProject()
							+ SierraConstants.PARSED_FILE_SUFFIX);
					c.setRunDocument(runDocumentHolder);
					// Change the run documents
					runDocuments.push(runDocumentHolder);
					c.setToolsDirectory(f_toolsDirectory);
					f_configs.add(c);
				}
			}
			Job runSierraAnalysis = new RunSierraJob("Running Sierra...",
					f_configs);
			runSierraAnalysis.setPriority(Job.SHORT);
			runSierraAnalysis.addJobChangeListener(new RunSierraAdapter(
					runDocuments));
			runSierraAnalysis.schedule();

			BalloonUtility
					.showMessage(
							"Sierra Analysis Started",
							"You may continue to work as the analysis runs. "
									+ "You will be notified when the analysis has been completed.");
			LOG.info("Started analysis on projects:" + projectList);
		}
	}

	private static class AntRunnable implements Runnable {

		private boolean f_complete;
		private SierraAnalysis f_sierraAnalysis;
		private final List<Config> f_configs;

		public AntRunnable(List<Config> configs) {
			f_configs = configs;
		}

		public void run() {

			f_complete = false;
			for (Config c : f_configs) {
				f_sierraAnalysis = new SierraAnalysis(c);
				f_sierraAnalysis.execute();

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

	private static class RunSierraJob extends Job {

		private final List<Config> f_configs;

		public RunSierraJob(String name, List<Config> configs) {
			super(name);
			f_configs = configs;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			try {

				monitor.beginTask("Running tools...", IProgressMonitor.UNKNOWN);

				final AntRunnable antRunnable = new AntRunnable(f_configs);
				final Thread antThread = new Thread(antRunnable);
				antThread.start();

				BalloonUtility
						.showMessage(
								"Sierra Analysis Started",
								"You may continue to work as the analysis runs. "
										+ "You will be notified when the analysis has been completed.");

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
					LOG.info("Completed analysis");
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

	private static class RunSierraAdapter extends JobChangeAdapter {

		private Stack<File> f_runDocs;

		public RunSierraAdapter(Stack<File> runDocs) {
			f_runDocs = runDocs;
		}

		@Override
		public void done(IJobChangeEvent event) {
			if (event.getResult().equals(PROPER_TERMINATION)) {

				Job databaseEntryJob = new DatabaseEntryJob(
						"Finshing analysis", f_runDocs);
				databaseEntryJob.setPriority(Job.SHORT);
				databaseEntryJob
						.addJobChangeListener(new DatabaseEntryJobAdapter());
				databaseEntryJob.schedule();

			} else if (event.getResult().equals(TASK_CANCELED)) {
				LOG.info("Cancelled analysis");
				BalloonUtility.showMessage("Sierra Analysis was cancelled",
						"Check the logs.");
			} else if (event.getResult().equals(TASK_ALREADY_RUNNING)) {
				BalloonUtility.showMessage(
						"An instance of Sierra analysis is already running",
						"Please wait for it to finish before restarting");

			} else {

				BalloonUtility.showMessage(
						"Sierra Analysis Completed with errors",
						"Check the logs.");
			}
		}
	}

	private static class DatabaseEntryJob extends Job {

		private final Stack<File> f_runDocs;

		public DatabaseEntryJob(String name, Stack<File> runDocs) {
			super(name);
			f_runDocs = runDocs;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			LOG.info("Starting database entry...");

			SLProgressMonitorWrapper slProgressMonitorWrapper = new SLProgressMonitorWrapper(
					monitor);

			while (!f_runDocs.isEmpty()) {

				File runDocumentHolder = f_runDocs.pop();
				LOG.info("Currently loading..."
						+ runDocumentHolder.getAbsolutePath());
				try {
					RunDocumentUtility.loadRunDocument(runDocumentHolder,
							slProgressMonitorWrapper);

				} catch (RunPersistenceException rpe) {
					LOG.severe(rpe.getMessage());
					BalloonUtility.showMessage(
							"Sierra Analysis Completed with errors",
							"Check the logs.");
					slProgressMonitorWrapper.done();
					return IMPROPER_TERMINATION;
				}
			}

			if (slProgressMonitorWrapper.isCanceled()) {
				return TASK_CANCELED;
			} else {
				LOG.info("Finished everything");
				slProgressMonitorWrapper.done();
				return PROPER_TERMINATION;
			}
		}
	}

	private static class DatabaseEntryJobAdapter extends JobChangeAdapter {

		@Override
		public void done(IJobChangeEvent event) {
			if (event.getResult().equals(PROPER_TERMINATION)) {
				BalloonUtility.showMessage("Sierra Analysis Completed",
						"You may now examine the analysis results.");

			} else if (event.getResult().equals(TASK_CANCELED)) {
				LOG.info("Cancelled analysis");
				BalloonUtility.showMessage("Sierra Analysis was cancelled",
						"Check the logs.");
			} else {
				BalloonUtility.showMessage(
						"Sierra Analysis Completed with errors",
						"Check the logs.");
			}
		}
	}
}
