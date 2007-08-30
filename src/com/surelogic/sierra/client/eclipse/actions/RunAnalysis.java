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
import org.eclipse.jface.viewers.IStructuredSelection;

import com.surelogic.common.eclipse.BalloonUtility;
import com.surelogic.common.eclipse.SLProgressMonitorWrapper;
import com.surelogic.common.eclipse.SierraConstants;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.client.eclipse.data.RunDocumentUtility;
import com.surelogic.sierra.jdbc.run.RunPersistenceException;
import com.surelogic.sierra.tool.analyzer.BuildFileGenerator;
import com.surelogic.sierra.tool.analyzer.Parser;
import com.surelogic.sierra.tool.ant.SierraAnalysis;
import com.surelogic.sierra.tool.config.Config;

/**
 * Runs Sierra Analysis on an Eclipse project.
 * 
 * @author Tanmay.Sinha
 */
public final class RunAnalysis {

	private static final Logger LOG = SLLogger.getLogger("sierra");

	private IStructuredSelection f_currentSelection = null;

	private File f_resultRoot;

	private File f_buildFile;

	private BuildFileGenerator f_buildFileGenerator;

	private Config f_config;

	private List<File> f_buildFiles;

	private final File f_toolsDirectory;

	public RunAnalysis(IStructuredSelection currentSelection) {
		// Get the plugin directory that has tools folder and append the
		// directory
		f_currentSelection = currentSelection;
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
		List<IJavaProject> selectedProjects = new ArrayList<IJavaProject>();
		if (f_currentSelection != null) {
			for (Object selection : f_currentSelection.toArray()) {
				if (selection instanceof IJavaProject) {
					selectedProjects.add((IJavaProject) selection);
				}
			}

			f_buildFiles = new ArrayList<File>();
			Stack<File> runDocuments = new Stack<File>();

			for (IJavaProject project : selectedProjects) {
				// log.info("Generating XML...");

				String projectPath = project.getResource().getLocation()
						.toString();

				File baseDir = new File(projectPath);
				File runDocument = new File(f_resultRoot + File.separator
						+ project.getProject().getName()
						+ SierraConstants.PARSED_FILE_SUFFIX);
				runDocuments.push(runDocument);

				f_config = new Config();

				f_config.setBaseDirectory(baseDir);
				f_config.setProject(project.getProject().getName());
				f_config.setDestDirectory(f_resultRoot);
				f_config.setRunDocument(runDocument);

				// Get the plugin directory that has tools folder and append the
				// directory
				String tools = BuildFileGenerator.getToolsDirectory();
				tools = tools + SierraConstants.TOOLS_FOLDER;
				f_config.setToolsDirectory(new File(tools));

				f_buildFileGenerator = new BuildFileGenerator(f_config);
				f_buildFile = f_buildFileGenerator.writeBuildFile();
				f_buildFiles.add(f_buildFile);

			}

			// if (buildFiles.size() > 1) {
			// buildFile = bfg.writeMultipleProjectBuildFile(buildFiles);
			// }

			if (f_buildFile != null) {

				Job runSierraAnalysis = new RunSierraJob("Running Sierra...",
						f_buildFiles, f_toolsDirectory);
				runSierraAnalysis.setPriority(Job.SHORT);
				runSierraAnalysis.addJobChangeListener(new RunSierraAdapter(
						runDocuments));
				runSierraAnalysis.schedule();

				// Job runBuildfile = new RunSierraAntJob("Running Sierra...");
				// runBuildfile.setPriority(Job.SHORT);
				// runBuildfile.addJobChangeListener(new RunSierraAdapter());
				// runBuildfile.schedule();
				BalloonUtility
						.showMessage(
								"Sierra Analysis Started",
								"You may continue to work as the analysis runs. "
										+ "You will be notified when the analysis has been completed.");
				LOG.info("Started analysis..");

			}

		}

	}

	private static class AntRunnable implements Runnable {

		private boolean f_complete;
		private SierraAnalysis f_sierraAnalysis;
		private final List<File> f_buildFileList;
		private final File f_toolsDirectory;

		public AntRunnable(List<File> buildFiles, File toolsDirectory) {
			f_buildFileList = buildFiles;
			f_toolsDirectory = toolsDirectory;
		}

		public void run() {

			f_complete = false;
			for (File f : f_buildFileList) {
				Parser buildFileParser = new Parser();
				List<Config> configs = buildFileParser.parseBuildFile(f
						.getAbsolutePath());
				for (Config c : configs) {
					c.setToolsDirectory(f_toolsDirectory);
					f_sierraAnalysis = new SierraAnalysis(c);
					f_sierraAnalysis.execute();
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

	private static class RunSierraJob extends Job {

		private final List<File> f_buildFiles;
		private final File f_toolsDirectory;

		public RunSierraJob(String name, List<File> buildFiles,
				File toolsDirectory) {
			super(name);
			f_buildFiles = buildFiles;
			f_toolsDirectory = toolsDirectory;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			try {

				monitor.beginTask("Running tools...", IProgressMonitor.UNKNOWN);

				final AntRunnable antRunnable = new AntRunnable(f_buildFiles,
						f_toolsDirectory);
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
					return SierraConstants.TASK_CANCELLED;
				} else {
					monitor.done();
					LOG.info("Completed analysis");
					return SierraConstants.PROPER_TERMINATION;
				}

			} catch (Exception e) {
				LOG.log(Level.SEVERE,
						"Exception while trying to excute ant build task" + e);
			}

			monitor.done();
			return SierraConstants.IMPROPER_TERMINATION;
		}
	}

	private static class RunSierraAdapter extends JobChangeAdapter {

		private Stack<File> f_runDocs;

		public RunSierraAdapter(Stack<File> runDocs) {
			f_runDocs = runDocs;
		}

		@Override
		public void done(IJobChangeEvent event) {
			if (event.getResult().equals(SierraConstants.PROPER_TERMINATION)) {

				Job databaseEntryJob = new DatabaseEntryJob(
						"Finshing analysis", f_runDocs);
				databaseEntryJob.setPriority(Job.SHORT);
				databaseEntryJob
						.addJobChangeListener(new DatabaseEntryJobAdapter());
				databaseEntryJob.schedule();

			} else if (event.getResult().equals(SierraConstants.TASK_CANCELLED)) {
				LOG.info("Cancelled analysis");
				BalloonUtility.showMessage("Sierra Analysis was cancelled",
						"Check the logs.");
			} else if (event.getResult().equals(
					SierraConstants.TASK_ALREADY_RUNNING)) {
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
					return SierraConstants.IMPROPER_TERMINATION;
				}
			}

			if (slProgressMonitorWrapper.isCanceled()) {
				return SierraConstants.TASK_CANCELLED;
			} else {
				LOG.info("Finished everything");
				slProgressMonitorWrapper.done();
				return SierraConstants.PROPER_TERMINATION;
			}
		}
	}

	private static class DatabaseEntryJobAdapter extends JobChangeAdapter {

		@Override
		public void done(IJobChangeEvent event) {
			if (event.getResult().equals(SierraConstants.PROPER_TERMINATION)) {
				BalloonUtility.showMessage("Sierra Analysis Completed",
						"You may now examine the analysis results.");

			} else if (event.getResult().equals(SierraConstants.TASK_CANCELLED)) {
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
