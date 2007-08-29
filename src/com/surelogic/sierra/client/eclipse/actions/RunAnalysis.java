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
 * The action that runs the Sierra Analysis
 * 
 * @author Tanmay.Sinha
 * 
 */
public final class RunAnalysis {

	/** The Sierra Logger */
	private static final Logger log = SLLogger.getLogger("sierra");

	private IStructuredSelection currentSelection = null;

	private File resultRoot;

	private File buildFile;

	private BuildFileGenerator bfg;

	private Config config;

	private ArrayList<File> buildFiles;

	private final File toolsDirectory;

	public RunAnalysis(IStructuredSelection currentSelection) {
		// Get the plugin directory that has tools folder and append the
		// directory
		this.currentSelection = currentSelection;
		String tools = BuildFileGenerator.getToolsDirectory()
				+ SierraConstants.TOOLS_FOLDER;
		toolsDirectory = new File(tools);
		resultRoot = new File(SierraConstants.SIERRA_RESULTS_PATH);

		if ((!resultRoot.exists()) || (resultRoot.exists())
				&& (!resultRoot.isDirectory())) {

			resultRoot.mkdir();
		}
	}

	public void execute() {
		List<IJavaProject> selectedProjects = new ArrayList<IJavaProject>();
		if (currentSelection != null) {
			for (Object selection : currentSelection.toArray()) {
				if (selection instanceof IJavaProject) {
					selectedProjects.add((IJavaProject) selection);
				}
			}

			buildFiles = new ArrayList<File>();
			Stack<File> runDocuments = new Stack<File>();

			for (IJavaProject project : selectedProjects) {
				// log.info("Generating XML...");

				String projectPath = project.getResource().getLocation()
						.toString();

				File baseDir = new File(projectPath);
				File runDocument = new File(resultRoot + File.separator
						+ project.getProject().getName()
						+ SierraConstants.PARSED_FILE_SUFFIX);
				runDocuments.push(runDocument);

				config = new Config();

				config.setBaseDirectory(baseDir);
				config.setProject(project.getProject().getName());
				config.setDestDirectory(resultRoot);
				config.setRunDocument(runDocument);

				// Get the plugin directory that has tools folder and append the
				// directory
				String tools = BuildFileGenerator.getToolsDirectory();
				tools = tools + SierraConstants.TOOLS_FOLDER;
				config.setToolsDirectory(new File(tools));

				bfg = new BuildFileGenerator(config);
				buildFile = bfg.writeBuildFile();
				buildFiles.add(buildFile);

			}

			// if (buildFiles.size() > 1) {
			// buildFile = bfg.writeMultipleProjectBuildFile(buildFiles);
			// }

			if (buildFile != null) {

				Job runSierraAnalysis = new RunSierraJob("Running Sierra...");
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
				log.info("Started analysis..");

			}

		}

	}

	private class AntRunnable implements Runnable {

		private boolean complete;
		private SierraAnalysis sa;
		private List<File> buildFileList;

		public AntRunnable(List<File> buildFiles) {
			this.buildFileList = buildFiles;
		}

		public void run() {

			complete = false;
			for (File f : buildFileList) {
				Parser buildFileParser = new Parser();
				List<Config> configs = buildFileParser.parseBuildFile(f
						.getAbsolutePath());
				for (Config c : configs) {
					c.setToolsDirectory(toolsDirectory);
					sa = new SierraAnalysis(c);
					sa.execute();
				}
			}
			complete = true;
		}

		public void stopAll() {
			sa.stop();
		}

		public boolean isCompleted() {
			return complete;
		}
	}

	private class RunSierraJob extends Job {

		public RunSierraJob(String name) {
			super(name);
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {

			Thread antThread;

			try {

				monitor.beginTask("Running tools...", IProgressMonitor.UNKNOWN);

				AntRunnable antRunnable = new AntRunnable(buildFiles);
				antThread = new Thread(antRunnable);
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
					log.info("Completed analysis");
					return SierraConstants.PROPER_TERMINATION;
				}

			} catch (Exception e) {
				log.log(Level.SEVERE,
						"Exception while trying to excute ant build task" + e);

			}

			monitor.done();
			return SierraConstants.IMPROPER_TERMINATION;

		}
	}

	// Handles ANT run using antrunner
	// @SuppressWarnings("unused")
	// private class RunSierraAntJob extends Job {
	//
	// private AntRunner runner;
	//
	// public RunSierraAntJob(String name) {
	// super(name);
	// }
	//
	// @Override
	// protected IStatus run(IProgressMonitor monitor) {
	//
	// Thread antThread;
	//
	// try {
	//
	// monitor.beginTask("Running analysis...",
	// IProgressMonitor.UNKNOWN);
	//
	// if (!AntRunner.isBuildRunning()) {
	// antThread = new Thread(new Runnable() {
	//
	// public void run() {
	//
	// try {
	// runner = new AntRunner();
	// runner.setBuildFileLocation(buildFile
	// .getAbsolutePath());
	// runner.addBuildLogger(ANT_LOGGER_DEFAULT);
	//
	// runner.setArguments("-logfile " + resultRoot
	// + File.separator + ANT_LOG_FILE);
	// runner.run();
	// } catch (CoreException e) {
	// log.log(Level.SEVERE,
	// "Core exception while trying to excute ant build task"
	// + e);
	// }
	//
	// }
	//
	// });
	// antThread.start();
	// BalloonUtility
	// .showMessage(
	// "Sierra Analysis Started",
	// "You may continue to work as the analysis runs. "
	// + "You will be notified when the analysis has been completed.");
	//
	// } else {
	// monitor.done();
	// return TASK_ALREADY_RUNNING;
	//
	// }
	//
	// while (!monitor.isCanceled()) {
	// Thread.sleep(500);
	// if (!AntRunner.isBuildRunning()) {
	// break;
	// }
	// }
	//
	// if (monitor.isCanceled()) {
	// monitor.done();
	// antThread.interrupt();
	// return TASK_CANCELLED;
	// } else {
	// monitor.done();
	// return PROPER_TERMINATION;
	// }
	//
	// } catch (Exception e) {
	// log.log(Level.SEVERE,
	// "Exception while trying to excute ant build task" + e);
	//
	// }
	//
	// monitor.done();
	// return IMPROPER_TERMINATION;
	//
	// }
	//
	// }

	private class RunSierraAdapter extends JobChangeAdapter {

		private Stack<File> runDocs;

		public RunSierraAdapter(Stack<File> runDocs) {
			this.runDocs = runDocs;
		}

		@Override
		public void done(IJobChangeEvent event) {
			if (event.getResult().equals(SierraConstants.PROPER_TERMINATION)) {

				Job databaseEntryJob = new DatabaseEntryJob(
						"Finshing analysis", runDocs);
				databaseEntryJob.setPriority(Job.SHORT);
				databaseEntryJob
						.addJobChangeListener(new DatabaseEntryJobAdapter());
				databaseEntryJob.schedule();

			} else if (event.getResult().equals(SierraConstants.TASK_CANCELLED)) {
				log.info("Cancelled analysis");
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

	@SuppressWarnings("unused")
	private class DataEntryRunnable implements Runnable {
		private boolean complete = false;

		public void run() {
			// TODO Auto-generated method stub

		}

		public boolean isCompleted() {
			return complete;
		}
	}

	private class DatabaseEntryJob extends Job {

		private Stack<File> runDocs;

		public DatabaseEntryJob(String name, Stack<File> runDocs) {
			super(name);
			this.runDocs = runDocs;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			log.info("Starting database entry...");

			SLProgressMonitorWrapper slProgressMonitorWrapper = new SLProgressMonitorWrapper(
					monitor);

			while (!runDocs.isEmpty()) {

				File runDocumentHolder = runDocs.pop();
				log.info("Currently loading..."
						+ runDocumentHolder.getAbsolutePath());
				try {
					RunDocumentUtility.loadRunDocument(runDocumentHolder,
							slProgressMonitorWrapper);

				} catch (RunPersistenceException rpe) {
					log.severe(rpe.getMessage());
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
				log.info("Finished everything");
				slProgressMonitorWrapper.done();
				return SierraConstants.PROPER_TERMINATION;
			}
		}
	}

	private class DatabaseEntryJobAdapter extends JobChangeAdapter {

		@Override
		public void done(IJobChangeEvent event) {
			if (event.getResult().equals(SierraConstants.PROPER_TERMINATION)) {
				BalloonUtility.showMessage("Sierra Analysis Completed",
						"You may now examine the analysis results.");

			} else if (event.getResult().equals(SierraConstants.TASK_CANCELLED)) {
				log.info("Cancelled analysis");
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
