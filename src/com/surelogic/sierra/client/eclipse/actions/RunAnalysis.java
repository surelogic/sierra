package com.surelogic.sierra.client.eclipse.actions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import com.surelogic.common.eclipse.BalloonUtility;
import com.surelogic.sierra.client.eclipse.Activator;
import com.surelogic.sierra.client.eclipse.SLog;
import com.surelogic.sierra.client.eclipse.data.RunDocumentUtility;
import com.surelogic.sierra.jdbc.run.RunPersistenceException;
import com.surelogic.sierra.tool.SierraLogger;
import com.surelogic.sierra.tool.analyzer.BuildFileGenerator;
import com.surelogic.sierra.tool.ant.SierraAnalysis;
import com.surelogic.sierra.tool.config.Config;

public final class RunAnalysis implements IObjectActionDelegate {

	/** The status for properly terminated task */
	private static final Status PROPER_TERMINATION = new Status(IStatus.OK,
			Activator.PLUGIN_ID, 0, "Proper termination", null);

	/** The status for task that completed with errors */
	private static final Status IMPROPER_TERMINATION = new Status(
			IStatus.ERROR, Activator.PLUGIN_ID, 0, "Error in execution", null);

	/** The status for cancelled task */
	private static final Status TASK_CANCELLED = new Status(IStatus.CANCEL,
			Activator.PLUGIN_ID, 0, "Task cancelled", null);

	/** The status for already running task - currently uses cancel */
	private static final Status TASK_ALREADY_RUNNING = new Status(
			IStatus.CANCEL, Activator.PLUGIN_ID, 0, "Task cancelled", null);

	/** The Sierra Logger */
	private static final Logger log = SierraLogger.getLogger("Sierra");

	/** The default location for storing results */
	private static final String SIERRA_RESULTS = ".SierraResults";

	/** The default extension for run document */
	private static final String PARSED_FILE_SUFFIX = ".xml.parsed";

	// /** The log file for ant task results */
	// private static final String ANT_LOG_FILE = "sierra-ant.log";
	//
	// /** The default logger being used */
	// private static final String ANT_LOGGER_DEFAULT =
	// "org.apache.tools.ant.DefaultLogger";

	/** The location of tools folder */
	private static final String TOOLS_FOLDER = "Tools";

	private IStructuredSelection currentSelection = null;

	private File resultRoot;

	private File buildFile;

	private BuildFileGenerator bfg;

	private Stack<File> runDocuments;

	private Config config;

	public RunAnalysis() {

		String tmpDir = System.getProperty("java.io.tmpdir");
		String resultsFolder = tmpDir + File.separator + SIERRA_RESULTS;
		resultRoot = new File(resultsFolder);

		if ((!resultRoot.exists()) || (resultRoot.exists())
				&& (!resultRoot.isDirectory())) {

			resultRoot.mkdir();
		}

	}

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		// Nothing to do
	}

	public void run(IAction action) {
		List<IJavaProject> selectedProjects = new ArrayList<IJavaProject>();
		if (currentSelection != null) {
			for (Object selection : currentSelection.toArray()) {
				if (selection instanceof IJavaProject) {
					selectedProjects.add((IJavaProject) selection);
				}
			}

			List<File> buildFiles = new ArrayList<File>();
			runDocuments = new Stack<File>();

			for (IJavaProject project : selectedProjects) {
				// log.info("Generating XML...");

				String projectPath = project.getResource().getLocation()
						.toString();

				File baseDir = new File(projectPath);
				File runDocument = new File(resultRoot + File.separator
						+ project.getProject().getName() + PARSED_FILE_SUFFIX);
				runDocuments.push(runDocument);

				config = new Config();

				config.setBaseDirectory(baseDir);
				config.setProject(project.getProject().getName());
				config.setDestDirectory(resultRoot);
				config.setRunDocument(runDocument);

				// Get the plugin directory that has tools folder and append the
				// directory
				String tools = BuildFileGenerator.getToolsDirectory();
				tools = tools + TOOLS_FOLDER;

				config.setToolsDirectory(new File(tools));

				bfg = new BuildFileGenerator(config);
				buildFile = bfg.writeBuildFile();
				buildFiles.add(buildFile);

			}

			if (buildFiles.size() > 1) {
				buildFile = bfg.writeMultipleProjectBuildFile(buildFiles);
			}

			// TODO: FIX THIS - Currently progress monitors are not handled
			// properly, output from ant task is also lost
			if (buildFile != null) {

				Job runSierraAnalysis = new RunSierraJob("Running Sierra...");
				runSierraAnalysis.setPriority(Job.SHORT);
				runSierraAnalysis.addJobChangeListener(new RunSierraAdapter());
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

			}

		}

	}

	public void selectionChanged(IAction action, ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			action.setEnabled(true);
			currentSelection = (IStructuredSelection) selection;
		} else {
			currentSelection = null;
			action.setEnabled(false);
			SLog.logWarning("Selection is not an IStructuredSelection",
					new Exception());
		}
	}

	private class AntRunnable implements Runnable {

		private boolean complete;
		SierraAnalysis sa;

		public void run() {

			sa = new SierraAnalysis(config);
			complete = false;
			sa.execute();
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

				monitor.beginTask("Starting analysis...",
						IProgressMonitor.UNKNOWN);

				AntRunnable antRunnable = new AntRunnable();
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
					return TASK_CANCELLED;
				} else {
					monitor.done();
					return PROPER_TERMINATION;
				}

			} catch (Exception e) {
				log.log(Level.SEVERE,
						"Exception while trying to excute ant build task" + e);

			}

			monitor.done();
			return IMPROPER_TERMINATION;

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
		@Override
		public void done(IJobChangeEvent event) {
			if (event.getResult().equals(PROPER_TERMINATION)) {

				Job databaseEntryJob = new DatabaseEntryJob("Finshing analysis");
				databaseEntryJob.setPriority(Job.SHORT);
				databaseEntryJob
						.addJobChangeListener(new DatabaseEntryJobAdapter());
				databaseEntryJob.schedule();

			} else if (event.getResult().equals(TASK_CANCELLED)) {

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

	private class DatabaseEntryJob extends Job {

		public DatabaseEntryJob(String name) {
			super(name);
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			log.info("Starting database entry...");
			while (!runDocuments.isEmpty()) {

				File runDocumentHolder = runDocuments.pop();
				monitor.beginTask("Loading run document...", runDocuments
						.size());
				try {
					monitor.subTask(runDocumentHolder.getName());
					monitor.worked(1);

					// TODO: Add feedback here, add cancel
					RunDocumentUtility.loadRunDocument(runDocumentHolder, null);

				} catch (RunPersistenceException rpe) {
					log.severe(rpe.getMessage());
					BalloonUtility.showMessage(
							"Sierra Analysis Completed with errors",
							"Check the logs.");
					monitor.done();
					return IMPROPER_TERMINATION;
				}
			}

			log.info("Finished everything");
			monitor.done();
			return PROPER_TERMINATION;
		}

	}

	private class DatabaseEntryJobAdapter extends JobChangeAdapter {

		@Override
		public void done(IJobChangeEvent event) {
			if (event.getResult().equals(PROPER_TERMINATION)) {
				BalloonUtility.showMessage("Sierra Analysis Completed",
						"You may now examine the analysis results.");

			} else if (event.getResult().equals(TASK_CANCELLED)) {
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
